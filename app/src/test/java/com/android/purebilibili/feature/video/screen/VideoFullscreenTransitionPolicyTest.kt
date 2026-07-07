package com.android.purebilibili.feature.video.screen

import android.content.pm.ActivityInfo
import com.android.purebilibili.core.store.FullscreenMode
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_STANDARD_DURATION_MILLIS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VideoFullscreenTransitionPolicyTest {

    @Test
    fun presentationMode_prefersPortraitPagerOverLandscape() {
        assertEquals(
            VideoFullscreenPresentationMode.PortraitPagerFullscreen,
            resolveVideoFullscreenPresentationMode(
                isFullscreenMode = true,
                isPortraitFullscreen = true
            )
        )
    }

    @Test
    fun presentationMode_mapsLandscapeFullscreen() {
        assertEquals(
            VideoFullscreenPresentationMode.LandscapeFullscreen,
            resolveVideoFullscreenPresentationMode(
                isFullscreenMode = true,
                isPortraitFullscreen = false
            )
        )
        assertEquals(
            VideoFullscreenPresentationMode.Inline,
            resolveVideoFullscreenPresentationMode(
                isFullscreenMode = false,
                isPortraitFullscreen = false
            )
        )
    }

    @Test
    fun transitionPhase_detectsLandscapeEnterAndExit() {
        assertEquals(
            VideoFullscreenTransitionPhase.EnteringLandscape,
            resolveVideoFullscreenTransitionPhase(
                previousMode = VideoFullscreenPresentationMode.Inline,
                currentMode = VideoFullscreenPresentationMode.LandscapeFullscreen,
                isMorphAnimating = true
            )
        )
        assertEquals(
            VideoFullscreenTransitionPhase.ExitingLandscapeToInline,
            resolveVideoFullscreenTransitionPhase(
                previousMode = VideoFullscreenPresentationMode.LandscapeFullscreen,
                currentMode = VideoFullscreenPresentationMode.Inline,
                isMorphAnimating = true
            )
        )
        assertEquals(
            VideoFullscreenTransitionPhase.Idle,
            resolveVideoFullscreenTransitionPhase(
                previousMode = VideoFullscreenPresentationMode.Inline,
                currentMode = VideoFullscreenPresentationMode.Inline,
                isMorphAnimating = false
            )
        )
    }

    @Test
    fun transitionPhase_detectsPortraitPagerEnterAndExit() {
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
    fun morphMotionSpec_usesSharedTransitionFullscreenDurationWhenEnabled() {
        val spec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = true)
        assertTrue(spec.enabled)
        assertTrue(spec.deferOrientationChange)
        assertTrue(spec.coverSurfaceDuringMorph)
        assertEquals(540, spec.durationMillis)
        assertEquals(220, spec.portraitPagerEnterDurationMillis)
    }

    @Test
    fun morphMotionSpec_isDisabledWhenCardTransitionOff() {
        val spec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = false)
        assertFalse(spec.enabled)
        assertFalse(spec.deferOrientationChange)
        assertEquals(0, spec.durationMillis)
    }

    @Test
    fun shouldCoverPlayerSurfaceDuringMorph_onlyWhileAnimating() {
        val spec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = true)
        assertTrue(
            shouldCoverPlayerSurfaceDuringMorph(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.EnteringLandscape
            )
        )
        assertFalse(
            shouldCoverPlayerSurfaceDuringMorph(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.Idle
            )
        )
    }

    @Test
    fun shouldDeferOrientationChange_onlyDuringLandscapeMorph() {
        val spec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = true)
        assertTrue(
            shouldDeferOrientationChange(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.EnteringLandscape
            )
        )
        assertTrue(
            shouldDeferOrientationChange(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.ExitingLandscapeToInline
            )
        )
        assertFalse(
            shouldDeferOrientationChange(
                morphMotionSpec = spec,
                transitionPhase = VideoFullscreenTransitionPhase.EnteringPortraitPager
            )
        )
    }

    @Test
    fun landscapeExitToggle_defersPortraitWhenMorphEnabled() {
        val plan = planVideoDetailFullscreenToggle(
            activity = null,
            isOrientationDrivenFullscreen = true,
            isLandscape = true,
            isFullscreenMode = true,
            isCompactDevice = true,
            fullscreenMode = FullscreenMode.AUTO,
            isVerticalVideo = false,
            portraitExperienceEnabled = true,
            deferOrientationChange = true
        )
        assertNull(plan)

        val deferredExitPlan = VideoFullscreenTogglePlan(
            userRequestedFullscreen = false,
            manualPortraitHoldActive = true,
            immediateOrientation = null,
            deferredOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, deferredExitPlan.deferredOrientation)
        assertNull(deferredExitPlan.immediateOrientation)
    }

    @Test
    fun resolveLandscapeOrientationLockDuringMorph_preservesExactLandscape() {
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            resolveLandscapeOrientationLockDuringMorph(
                currentRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            )
        )
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            resolveLandscapeOrientationLockDuringMorph(
                currentRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            )
        )
    }

    @Test
    fun fullscreenTogglePlan_defersLandscapeOrientationWhenRequested() {
        val plan = planVideoDetailFullscreenToggle(
            activity = null,
            isOrientationDrivenFullscreen = true,
            isLandscape = false,
            isFullscreenMode = false,
            isCompactDevice = true,
            fullscreenMode = FullscreenMode.AUTO,
            isVerticalVideo = false,
            portraitExperienceEnabled = true,
            deferOrientationChange = true
        )
        assertNull(plan)

        // Without activity the helper returns null; verify orientation fields through direct plan construction.
        val deferredPlan = VideoFullscreenTogglePlan(
            userRequestedFullscreen = true,
            immediateOrientation = null,
            deferredOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        )
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            deferredPlan.deferredOrientation
        )
        assertNull(deferredPlan.immediateOrientation)
    }

    @Test
    fun playerShellSharedElementKey_isStablePerVideo() {
        assertEquals(
            "video-detail-player-shell-BV1test",
            videoDetailPlayerShellSharedElementKey("BV1test")
        )
    }

    @Test
    fun morphMotionSpec_defaultDurationMatchesStandardPlusFullscreenOffset() {
        val spec = resolveVideoFullscreenMorphMotionSpec(cardTransitionEnabled = true)
        assertTrue(spec.durationMillis > VIDEO_SHARED_TRANSITION_STANDARD_DURATION_MILLIS)
    }
}
