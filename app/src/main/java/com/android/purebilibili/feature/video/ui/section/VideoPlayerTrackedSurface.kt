package com.android.purebilibili.feature.video.ui.section

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.view.SurfaceControl
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.window.SurfaceSyncGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.SurfaceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Media3 [androidx.media3.ui.compose.PlayerSurface] 的 tracked 变体：
 * 暴露底层 [SurfaceView]/[TextureView] 供截图与前台 surface 重绑使用。
 */
@OptIn(UnstableApi::class)
@Composable
internal fun VideoPlayerTrackedSurface(
    player: Player?,
    modifier: Modifier = Modifier,
    surfaceType: @SurfaceType Int = SURFACE_TYPE_SURFACE_VIEW,
    onSurfaceViewChanged: (View?) -> Unit = {},
) {
    when (surfaceType) {
        SURFACE_TYPE_SURFACE_VIEW -> {
            var surfaceSyncGroup: SurfaceSyncGroup? by remember { mutableStateOf(null) }

            val createSurfaceView: (Context) -> SurfaceView = { context ->
                object : SurfaceView(context) {
                    override fun dispatchDraw(canvas: Canvas) {
                        super.dispatchDraw(canvas)
                        if (Build.VERSION.SDK_INT == 34) {
                            surfaceSyncGroup?.markSyncReady()
                            surfaceSyncGroup = null
                        }
                    }
                }
            }

            val coroutineScope = rememberCoroutineScope()
            val onSurfaceSizeChanged: (SurfaceView) -> Unit = { surfaceView ->
                if (
                    Build.VERSION.SDK_INT == 34 &&
                    !Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
                ) {
                    coroutineScope.launch(Dispatchers.Main) {
                        surfaceView.rootSurfaceControl?.let { rootSurfaceControl ->
                            surfaceSyncGroup =
                                SurfaceSyncGroup("bilipai-exo-sync").apply {
                                    check(add(rootSurfaceControl) {}) {
                                        "Failed to add rootSurfaceControl to SurfaceSyncGroup"
                                    }
                                }
                            surfaceView.invalidate()
                            rootSurfaceControl.applyTransactionOnDraw(SurfaceControl.Transaction())
                        }
                    }
                }
            }

            VideoPlayerTrackedSurfaceInternal(
                player = player,
                modifier = modifier,
                createView = createSurfaceView,
                setVideoView = Player::setVideoSurfaceView,
                clearVideoView = Player::clearVideoSurfaceView,
                onSurfaceSizeChanged = onSurfaceSizeChanged,
                onSurfaceViewChanged = onSurfaceViewChanged,
            )
        }

        SURFACE_TYPE_TEXTURE_VIEW ->
            VideoPlayerTrackedSurfaceInternal(
                player = player,
                modifier = modifier,
                createView = ::TextureView,
                setVideoView = Player::setVideoTextureView,
                clearVideoView = Player::clearVideoTextureView,
                onSurfaceViewChanged = onSurfaceViewChanged,
            )

        else -> throw IllegalArgumentException("Unrecognized surface type: $surfaceType")
    }
}

@Composable
private fun <T : View> VideoPlayerTrackedSurfaceInternal(
    player: Player?,
    modifier: Modifier,
    createView: (Context) -> T,
    setVideoView: Player.(T) -> Unit,
    clearVideoView: Player.(T) -> Unit,
    onSurfaceSizeChanged: (T) -> Unit = {},
    onSurfaceViewChanged: (View?) -> Unit,
) {
    var view by remember { mutableStateOf<T?>(null) }

    DisposableEffect(Unit) {
        onDispose { onSurfaceViewChanged(null) }
    }

    AndroidView(
        modifier = modifier,
        factory = { createView(it) },
        onReset = {},
        update = {
            view = it
            onSurfaceViewChanged(it)
        },
    )

    view?.let { surfaceView ->
        DisposableEffect(surfaceView, player) {
            val listener =
                if (player != null) {
                    object : Player.Listener {
                        override fun onSurfaceSizeChanged(width: Int, height: Int) {
                            onSurfaceSizeChanged(surfaceView)
                        }
                    }.also { player.addListener(it) }
                } else {
                    null
                }

            onDispose { listener?.let { player?.removeListener(it) } }
        }

        LaunchedEffect(surfaceView, player) {
            if (player != null) {
                surfaceView.attachedExoPlayer?.let { previousPlayer ->
                    if (
                        previousPlayer != player &&
                        previousPlayer.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)
                    ) {
                        previousPlayer.clearVideoView(surfaceView)
                    }
                }
                if (player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                    player.setVideoView(surfaceView)
                    surfaceView.attachedExoPlayer = player
                }
            } else {
                withContext(Dispatchers.Main) {
                    surfaceView.attachedExoPlayer?.let { previousPlayer ->
                        if (previousPlayer.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                            previousPlayer.clearVideoView(surfaceView)
                        }
                        surfaceView.attachedExoPlayer = null
                    }
                }
            }
        }
    }
}

private var View.attachedExoPlayer: Player?
    get() = tag as? Player
    set(player) {
        tag = player
    }
