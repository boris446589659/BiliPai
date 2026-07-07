package com.android.purebilibili.feature.video.ui.section

import com.android.purebilibili.feature.video.screen.VideoFullscreenTransitionPhase
import com.android.purebilibili.feature.video.screen.resolveVideoFullscreenMorphMotionSpec
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoPlayerPresentationPolicyTest {

    @Test
    fun morphShutter_visibleOnlyDuringTransition() {
        val spec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = true)
        assertTrue(
            shouldShowVideoPlayerMorphShutter(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.ExitingLandscapeToInline
            )
        )
        assertFalse(
            shouldShowVideoPlayerMorphShutter(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.Idle
            )
        )
    }

    @Test
    fun presentationCover_visibleWhenMorphOrMedia3CoverActive() {
        assertTrue(
            resolveVideoPlayerPresentationCoverVisible(
                morphShutterVisible = true,
                presentationCoverSurface = false
            )
        )
        assertTrue(
            resolveVideoPlayerPresentationCoverVisible(
                morphShutterVisible = false,
                presentationCoverSurface = true
            )
        )
        assertFalse(
            resolveVideoPlayerPresentationCoverVisible(
                morphShutterVisible = false,
                presentationCoverSurface = false
            )
        )
    }
}
