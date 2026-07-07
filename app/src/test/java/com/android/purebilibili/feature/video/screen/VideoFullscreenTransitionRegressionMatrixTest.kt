package com.android.purebilibili.feature.video.screen

import com.android.purebilibili.feature.video.ui.section.resolveVideoPlayerPresentationCoverVisible
import com.android.purebilibili.feature.video.ui.section.shouldShowVideoPlayerMorphShutter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 回归矩阵：覆盖竖/横屏进出、分屏、预测返回与 PIP 相关策略门控。
 * 设备级验证仍建议在真机执行对应场景。
 */
class VideoFullscreenTransitionRegressionMatrixTest {

    private val enabledMorphSpec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = true)
    private val disabledMorphSpec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = false)

    @Test
    fun landscape_inlineEnter_showsLandscapeLayoutDuringMorph() {
        assertTrue(
            shouldShowLandscapeFullscreenLayout(
                presentationMode = VideoFullscreenPresentationMode.LandscapeFullscreen,
                transitionPhase = VideoFullscreenTransitionPhase.Idle
            )
        )
        assertTrue(
            shouldShowLandscapeFullscreenLayout(
                presentationMode = VideoFullscreenPresentationMode.Inline,
                transitionPhase = VideoFullscreenTransitionPhase.EnteringLandscape
            )
        )
    }

    @Test
    fun landscape_inlineExit_keepsInlineLayoutAvailableDuringMorph() {
        assertTrue(
            shouldShowInlineDetailLayout(
                presentationMode = VideoFullscreenPresentationMode.Inline,
                transitionPhase = VideoFullscreenTransitionPhase.ExitingLandscapeToInline
            )
        )
    }

    @Test
    fun portraitPager_enterAndExit_phasesAreDistinct() {
        assertEquals(
            VideoFullscreenTransitionPhase.EnteringPortraitPager,
            resolveVideoFullscreenTransitionPhase(
                previousMode = VideoFullscreenPresentationMode.Inline,
                currentMode = VideoFullscreenPresentationMode.PortraitPagerFullscreen,
                isMorphAnimating = true
            )
        )
        assertEquals(
            VideoFullscreenTransitionPhase.ExitingPortraitPagerToInline,
            resolveVideoFullscreenTransitionPhase(
                previousMode = VideoFullscreenPresentationMode.PortraitPagerFullscreen,
                currentMode = VideoFullscreenPresentationMode.Inline,
                isMorphAnimating = true
            )
        )
    }

    @Test
    fun multiWindowFullscreenPolicy_stillBypassesDeferredOrientationPlan() {
        assertTrue(
            shouldUseInWindowFullscreenForSystemMultiWindow(
                isInMultiWindowMode = true,
                isInPictureInPictureMode = false,
                isOrientationDrivenFullscreen = true,
                isFullscreenMode = false
            )
        )
    }

    @Test
    fun morphDisabled_fallsBackToImmediateLayoutSwitchWithoutShutter() {
        assertFalse(
            shouldShowVideoPlayerMorphShutter(
                morphMotionSpec = disabledMorphSpec,
                transitionPhase = VideoFullscreenTransitionPhase.EnteringLandscape
            )
        )
        assertFalse(disabledMorphSpec.deferOrientationChange)
    }

    @Test
    fun morphEnabled_coversSurfaceDuringLandscapeTransition() {
        assertTrue(
            shouldShowVideoPlayerMorphShutter(
                morphMotionSpec = enabledMorphSpec,
                transitionPhase = VideoFullscreenTransitionPhase.EnteringLandscape
            )
        )
        assertTrue(
            resolveVideoPlayerPresentationCoverVisible(
                morphShutterVisible = true,
                presentationCoverSurface = false
            )
        )
    }

    @Test
    fun predictiveBackAndPip_paths_keepInlinePresentationWhenPortraitPagerInactive() {
        assertEquals(
            VideoFullscreenPresentationMode.Inline,
            resolveVideoFullscreenPresentationMode(
                isFullscreenMode = false,
                isPortraitFullscreen = false
            )
        )
    }

    @Test
    fun danmakuAndPlayerSurface_shutterPolicy_allowsPresentationLayerCover() {
        assertTrue(
            resolveVideoPlayerPresentationCoverVisible(
                morphShutterVisible = false,
                presentationCoverSurface = true
            )
        )
    }
}
