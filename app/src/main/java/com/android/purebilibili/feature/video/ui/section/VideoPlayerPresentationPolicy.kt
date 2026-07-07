package com.android.purebilibili.feature.video.ui.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.feature.video.screen.VideoFullscreenTransitionPhase
import com.android.purebilibili.feature.video.screen.shouldCoverPlayerSurfaceDuringMorph
import com.android.purebilibili.feature.video.screen.VideoFullscreenMorphMotionSpec

internal fun shouldShowVideoPlayerMorphShutter(
    morphMotionSpec: VideoFullscreenMorphMotionSpec,
    transitionPhase: VideoFullscreenTransitionPhase,
    presentationCoverSurface: Boolean = false
): Boolean {
    return presentationCoverSurface ||
        shouldCoverPlayerSurfaceDuringMorph(
            morphMotionSpec = morphMotionSpec,
            transitionPhase = transitionPhase
        )
}

@Composable
internal fun VideoPlayerMorphShutterOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}

/**
 * Media3 Compose 迁移入口：当 [presentationCoverSurface] 为 true 时显示 shutter，
 * 与 [rememberPresentationState].coverSurface 语义对齐，可在 PlayerSurface 接入后复用。
 */
internal fun resolveVideoPlayerPresentationCoverVisible(
    morphShutterVisible: Boolean,
    presentationCoverSurface: Boolean
): Boolean = morphShutterVisible || presentationCoverSurface
