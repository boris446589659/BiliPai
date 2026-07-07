package com.android.purebilibili.feature.video.ui.section

import android.view.View
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.state.rememberPresentationState

@OptIn(UnstableApi::class)
@Composable
internal fun VideoPlayerSurfacePresentationHost(
    player: Player?,
    modifier: Modifier = Modifier,
    forceCoverSurface: Boolean = false,
    keepContentOnReset: Boolean = false,
    useTextureSurface: Boolean = false,
    surfaceVisible: Boolean = true,
    surfaceRevealAlpha: Float = 1f,
    surfaceRevealScale: Float = 1f,
    isFlippedHorizontal: Boolean = false,
    isFlippedVertical: Boolean = false,
    panX: Float = 0f,
    panY: Float = 0f,
    zoomScale: Float = 1f,
    onSurfaceViewChanged: (View?) -> Unit = {},
    content: @Composable (presentationCoverSurface: Boolean) -> Unit = {},
) {
    val presentationState = rememberPresentationState(
        player = player,
        keepContentOnReset = keepContentOnReset,
    )
    val presentationCoverSurface = forceCoverSurface || presentationState.coverSurface
    val surfaceAlpha = if (surfaceVisible) surfaceRevealAlpha else 0f

    Box(
        modifier = modifier.graphicsLayer {
            val revealAwareScaleX = zoomScale * surfaceRevealScale
            val revealAwareScaleY = zoomScale * surfaceRevealScale
            scaleX = if (isFlippedHorizontal) -revealAwareScaleX else revealAwareScaleX
            scaleY = if (isFlippedVertical) -revealAwareScaleY else revealAwareScaleY
            translationX = panX
            translationY = panY
            alpha = surfaceAlpha
        },
    ) {
        VideoPlayerTrackedSurface(
            player = player,
            surfaceType = if (useTextureSurface) {
                SURFACE_TYPE_TEXTURE_VIEW
            } else {
                SURFACE_TYPE_SURFACE_VIEW
            },
            modifier = Modifier.fillMaxSize(),
            onSurfaceViewChanged = onSurfaceViewChanged,
        )
        if (presentationCoverSurface) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
        content(presentationCoverSurface)
    }
}
