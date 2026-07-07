package com.android.purebilibili.feature.video.screen

import android.content.pm.ActivityInfo
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_STANDARD_DURATION_MILLIS
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionFullscreenDurationMillis

internal enum class VideoFullscreenPresentationMode {
    Inline,
    LandscapeFullscreen,
    PortraitPagerFullscreen
}

internal enum class VideoFullscreenTransitionPhase {
    Idle,
    EnteringLandscape,
    ExitingLandscapeToInline,
    EnteringPortraitPager,
    ExitingPortraitPagerToInline
}

internal enum class VideoFullscreenMorphTarget {
    Inline,
    LandscapeFullscreen,
    PortraitPagerFullscreen
}

internal data class VideoFullscreenMorphMotionSpec(
    val enabled: Boolean,
    val durationMillis: Int,
    val portraitPagerEnterDurationMillis: Int,
    val portraitPagerExitDurationMillis: Int,
    val portraitPagerExitScaleTarget: Float,
    val portraitPagerExitTranslateUpFraction: Float,
    val deferOrientationChange: Boolean,
    val coverSurfaceDuringMorph: Boolean
)

internal data class VideoFullscreenTogglePlan(
    val userRequestedFullscreen: Boolean? = null,
    val enterPortraitFullscreen: Boolean = false,
    val manualPortraitHoldActive: Boolean? = null,
    val immediateOrientation: Int? = null,
    val deferredOrientation: Int? = null
)

internal fun resolveVideoFullscreenPresentationMode(
    isFullscreenMode: Boolean,
    isPortraitFullscreen: Boolean
): VideoFullscreenPresentationMode {
    return when {
        isPortraitFullscreen -> VideoFullscreenPresentationMode.PortraitPagerFullscreen
        isFullscreenMode -> VideoFullscreenPresentationMode.LandscapeFullscreen
        else -> VideoFullscreenPresentationMode.Inline
    }
}

internal fun resolveVideoFullscreenMorphTarget(
    presentationMode: VideoFullscreenPresentationMode
): VideoFullscreenMorphTarget {
    return when (presentationMode) {
        VideoFullscreenPresentationMode.Inline -> VideoFullscreenMorphTarget.Inline
        VideoFullscreenPresentationMode.LandscapeFullscreen -> VideoFullscreenMorphTarget.LandscapeFullscreen
        VideoFullscreenPresentationMode.PortraitPagerFullscreen -> VideoFullscreenMorphTarget.PortraitPagerFullscreen
    }
}

internal fun resolveVideoFullscreenTransitionPhase(
    previousMode: VideoFullscreenPresentationMode?,
    currentMode: VideoFullscreenPresentationMode,
    isMorphAnimating: Boolean
): VideoFullscreenTransitionPhase {
    if (!isMorphAnimating || previousMode == null || previousMode == currentMode) {
        return VideoFullscreenTransitionPhase.Idle
    }
    return when {
        previousMode == VideoFullscreenPresentationMode.Inline &&
            currentMode == VideoFullscreenPresentationMode.LandscapeFullscreen ->
            VideoFullscreenTransitionPhase.EnteringLandscape
        previousMode == VideoFullscreenPresentationMode.LandscapeFullscreen &&
            currentMode == VideoFullscreenPresentationMode.Inline ->
            VideoFullscreenTransitionPhase.ExitingLandscapeToInline
        previousMode == VideoFullscreenPresentationMode.Inline &&
            currentMode == VideoFullscreenPresentationMode.PortraitPagerFullscreen ->
            VideoFullscreenTransitionPhase.EnteringPortraitPager
        previousMode == VideoFullscreenPresentationMode.PortraitPagerFullscreen &&
            currentMode == VideoFullscreenPresentationMode.Inline ->
            VideoFullscreenTransitionPhase.ExitingPortraitPagerToInline
        else -> VideoFullscreenTransitionPhase.Idle
    }
}

internal fun shouldShowLandscapeFullscreenLayout(
    presentationMode: VideoFullscreenPresentationMode,
    transitionPhase: VideoFullscreenTransitionPhase
): Boolean {
    if (presentationMode == VideoFullscreenPresentationMode.LandscapeFullscreen) return true
    return transitionPhase == VideoFullscreenTransitionPhase.EnteringLandscape ||
        transitionPhase == VideoFullscreenTransitionPhase.ExitingLandscapeToInline
}

internal fun shouldShowInlineDetailLayout(
    presentationMode: VideoFullscreenPresentationMode,
    transitionPhase: VideoFullscreenTransitionPhase
): Boolean {
    if (presentationMode == VideoFullscreenPresentationMode.Inline) return true
    return transitionPhase == VideoFullscreenTransitionPhase.EnteringLandscape ||
        transitionPhase == VideoFullscreenTransitionPhase.ExitingLandscapeToInline ||
        transitionPhase == VideoFullscreenTransitionPhase.EnteringPortraitPager ||
        transitionPhase == VideoFullscreenTransitionPhase.ExitingPortraitPagerToInline
}

internal fun shouldDeferOrientationChange(
    morphMotionSpec: VideoFullscreenMorphMotionSpec,
    transitionPhase: VideoFullscreenTransitionPhase
): Boolean {
    if (!morphMotionSpec.deferOrientationChange) return false
    return transitionPhase == VideoFullscreenTransitionPhase.EnteringLandscape ||
        transitionPhase == VideoFullscreenTransitionPhase.ExitingLandscapeToInline
}

internal fun resolveLandscapeOrientationLockDuringMorph(
    currentRequestedOrientation: Int?
): Int {
    return when (currentRequestedOrientation) {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> currentRequestedOrientation
        else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
}

internal fun shouldCoverPlayerSurfaceDuringMorph(
    morphMotionSpec: VideoFullscreenMorphMotionSpec,
    transitionPhase: VideoFullscreenTransitionPhase
): Boolean {
    if (!morphMotionSpec.coverSurfaceDuringMorph) return false
    return transitionPhase != VideoFullscreenTransitionPhase.Idle
}

internal fun resolveVideoFullscreenMorphMotionSpec(
    cardTransitionEnabled: Boolean,
    portraitPagerMotionSpec: StandalonePortraitPagerMotionSpec = resolveStandalonePortraitPagerMotionSpec()
): VideoFullscreenMorphMotionSpec {
    val baseDuration = if (cardTransitionEnabled) {
        resolveVideoSharedTransitionFullscreenDurationMillis(
            VIDEO_SHARED_TRANSITION_STANDARD_DURATION_MILLIS
        )
    } else {
        0
    }
    return VideoFullscreenMorphMotionSpec(
        enabled = cardTransitionEnabled && baseDuration > 0,
        durationMillis = baseDuration,
        portraitPagerEnterDurationMillis = portraitPagerMotionSpec.enterDurationMillis,
        portraitPagerExitDurationMillis = portraitPagerMotionSpec.exitDurationMillis,
        portraitPagerExitScaleTarget = portraitPagerMotionSpec.exitScaleTarget,
        portraitPagerExitTranslateUpFraction = portraitPagerMotionSpec.exitTranslateUpFraction,
        deferOrientationChange = cardTransitionEnabled,
        coverSurfaceDuringMorph = cardTransitionEnabled
    )
}

internal fun resolveVideoFullscreenMorphEasing(
    entering: Boolean
) = if (entering) {
    AppMotionEasing.EmphasizedEnter
} else {
    AppMotionEasing.EmphasizedExit
}

internal fun videoDetailPlayerShellSharedElementKey(bvid: String): String = "video-detail-player-shell-$bvid"

internal fun planVideoDetailFullscreenToggle(
    activity: android.app.Activity?,
    isOrientationDrivenFullscreen: Boolean,
    isLandscape: Boolean,
    isFullscreenMode: Boolean,
    isCompactDevice: Boolean,
    fullscreenMode: com.android.purebilibili.core.store.FullscreenMode,
    isVerticalVideo: Boolean,
    portraitExperienceEnabled: Boolean,
    deferOrientationChange: Boolean
): VideoFullscreenTogglePlan? {
    if (activity == null) return null

    val isInMultiWindowMode = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N &&
        activity.isInMultiWindowMode
    val isInPictureInPictureMode = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
        activity.isInPictureInPictureMode

    if (shouldUseInWindowFullscreenForSystemMultiWindow(
            isInMultiWindowMode = isInMultiWindowMode,
            isInPictureInPictureMode = isInPictureInPictureMode,
            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
            isFullscreenMode = isFullscreenMode
        )
    ) {
        return VideoFullscreenTogglePlan(
            userRequestedFullscreen = true,
            manualPortraitHoldActive = false
        )
    }

    if (isOrientationDrivenFullscreen && isInMultiWindowMode && isFullscreenMode) {
        return VideoFullscreenTogglePlan(
            userRequestedFullscreen = false,
            manualPortraitHoldActive = false
        )
    }

    if (!isOrientationDrivenFullscreen) {
        val nextRequestedFullscreen = !isFullscreenMode
        val immediateOrientation = if (
            !nextRequestedFullscreen &&
            isCompactDevice &&
            fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.VERTICAL
        ) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            null
        }
        return VideoFullscreenTogglePlan(
            userRequestedFullscreen = nextRequestedFullscreen,
            immediateOrientation = immediateOrientation
        )
    }

    if (isLandscape) {
        return VideoFullscreenTogglePlan(
            userRequestedFullscreen = false,
            manualPortraitHoldActive = true,
            immediateOrientation = if (deferOrientationChange) {
                null
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            deferredOrientation = if (deferOrientationChange) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                null
            }
        )
    }

    val targetOrientation = resolvePhoneFullscreenEnterOrientation(
        fullscreenMode = fullscreenMode,
        isVerticalVideo = isVerticalVideo
    ) ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

    if (shouldEnterPortraitFullscreenOnFullscreenToggle(
            targetOrientation = targetOrientation,
            portraitExperienceEnabled = portraitExperienceEnabled
        )
    ) {
        return VideoFullscreenTogglePlan(
            userRequestedFullscreen = false,
            enterPortraitFullscreen = true,
            manualPortraitHoldActive = false
        )
    }

    return VideoFullscreenTogglePlan(
        userRequestedFullscreen = true,
        manualPortraitHoldActive = false,
        immediateOrientation = if (deferOrientationChange) null else targetOrientation,
        deferredOrientation = if (deferOrientationChange) targetOrientation else null
    )
}

internal fun applyVideoFullscreenTogglePlan(
    plan: VideoFullscreenTogglePlan,
    activity: android.app.Activity?,
    onEnterPortraitFullscreen: () -> Unit,
    onUserRequestedFullscreenChange: (Boolean) -> Unit,
    onManualPortraitHoldActiveChange: (Boolean) -> Unit
) {
    plan.userRequestedFullscreen?.let(onUserRequestedFullscreenChange)
    plan.manualPortraitHoldActive?.let(onManualPortraitHoldActiveChange)
    if (plan.enterPortraitFullscreen) {
        onEnterPortraitFullscreen()
    }
    plan.immediateOrientation?.let { orientation ->
        activity?.requestedOrientation = orientation
    }
}
