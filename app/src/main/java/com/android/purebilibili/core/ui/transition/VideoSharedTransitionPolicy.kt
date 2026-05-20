package com.android.purebilibili.core.ui.transition

import com.android.purebilibili.core.ui.motion.resolveDetailVerticalContentRevealMotionSpec

internal enum class VideoSharedTransitionProfile {
    COVER_ONLY,
    COVER_AND_METADATA
}

internal const val VIDEO_SHARED_COVER_ASPECT_RATIO = 16f / 10f
private const val HOME_SOURCE_ROUTE = "home"
private const val HOME_SHARED_TRANSITION_DURATION_MILLIS = 360
private const val HOME_DETAIL_REVEAL_DELAY_MILLIS = 40
private const val HOME_DETAIL_REVEAL_DURATION_MILLIS = 220
private const val HOME_DETAIL_REVEAL_SLIDE_OFFSET_DP = 14
private const val HOME_DETAIL_REVEAL_INITIAL_SCALE = 0.985f
private const val HOME_SHARED_TRANSITION_CARD_CORNER_DP = 16
private const val HOME_SHARED_TRANSITION_PLAYER_CORNER_DP = 12

internal data class VideoSharedTransitionOwnership(
    val useCoverSharedBounds: Boolean,
    val useMetadataSharedBounds: Boolean
)

internal data class VideoDetailContentRevealMotion(
    val enabled: Boolean,
    val delayMillis: Int,
    val durationMillis: Int,
    val slideOffsetDp: Int,
    val initialScale: Float
)

internal data class VideoSharedTransitionMotionSpec(
    val enabled: Boolean,
    val durationMillis: Int,
    val contentDelayMillis: Int,
    val contentDurationMillis: Int,
    val contentSlideOffsetDp: Int,
    val contentInitialScale: Float
)

internal data class VideoSharedCornerSpec(
    val enabled: Boolean,
    val startCornerDp: Int,
    val endCornerDp: Int
)

internal fun resolveVideoSharedTransitionProfile(): VideoSharedTransitionProfile {
    return VideoSharedTransitionProfile.COVER_AND_METADATA
}

private fun resolveVideoSharedTransitionProfile(sourceRoute: String?): VideoSharedTransitionProfile {
    return if (sourceRoute?.substringBefore("?") == HOME_SOURCE_ROUTE) {
        VideoSharedTransitionProfile.COVER_ONLY
    } else {
        VideoSharedTransitionProfile.COVER_AND_METADATA
    }
}

internal fun shouldEnableVideoCoverSharedTransition(
    transitionEnabled: Boolean,
    hasSharedTransitionScope: Boolean,
    hasAnimatedVisibilityScope: Boolean
): Boolean {
    return transitionEnabled &&
        hasSharedTransitionScope &&
        hasAnimatedVisibilityScope
}

internal fun shouldEnableVideoMetadataSharedTransition(
    coverSharedEnabled: Boolean,
    isQuickReturnLimited: Boolean,
    useCardContainerSharedBounds: Boolean = false,
    profile: VideoSharedTransitionProfile = resolveVideoSharedTransitionProfile()
): Boolean {
    if (!coverSharedEnabled) return false
    // 卡片容器已经承载整体放大/回收时，标题、UP、统计等不要再各自抢独立 sharedBounds。
    if (useCardContainerSharedBounds) return false
    // Keep metadata linked during quick return to avoid cover-only snapback.
    if (isQuickReturnLimited && profile == VideoSharedTransitionProfile.COVER_ONLY) return false
    return profile == VideoSharedTransitionProfile.COVER_AND_METADATA
}

internal fun resolveVideoSharedTransitionOwnership(
    sourceRoute: String?,
    coverSharedEnabled: Boolean,
    isQuickReturnLimited: Boolean
): VideoSharedTransitionOwnership {
    if (!coverSharedEnabled) {
        return VideoSharedTransitionOwnership(
            useCoverSharedBounds = false,
            useMetadataSharedBounds = false
        )
    }

    val isHomeSource = sourceRoute?.substringBefore("?") == HOME_SOURCE_ROUTE
    return VideoSharedTransitionOwnership(
        useCoverSharedBounds = true,
        // 首页进入详情时封面是主锚点，元数据跟随普通内容入场，避免多个 sharedBounds 抢焦点。
        useMetadataSharedBounds = !isHomeSource &&
            shouldEnableVideoMetadataSharedTransition(
                coverSharedEnabled = true,
                isQuickReturnLimited = isQuickReturnLimited,
                profile = resolveVideoSharedTransitionProfile(sourceRoute)
            )
    )
}

internal fun resolveHomeVideoSharedTransitionMotionSpec(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoSharedTransitionMotionSpec {
    val enabled = transitionEnabled &&
        sourceRoute?.substringBefore("?") == HOME_SOURCE_ROUTE
    if (!enabled) {
        return VideoSharedTransitionMotionSpec(
            enabled = false,
            durationMillis = 0,
            contentDelayMillis = 0,
            contentDurationMillis = 0,
            contentSlideOffsetDp = 0,
            contentInitialScale = 1f
        )
    }

    return VideoSharedTransitionMotionSpec(
        enabled = true,
        durationMillis = HOME_SHARED_TRANSITION_DURATION_MILLIS,
        contentDelayMillis = HOME_DETAIL_REVEAL_DELAY_MILLIS,
        contentDurationMillis = HOME_DETAIL_REVEAL_DURATION_MILLIS,
        contentSlideOffsetDp = HOME_DETAIL_REVEAL_SLIDE_OFFSET_DP,
        contentInitialScale = HOME_DETAIL_REVEAL_INITIAL_SCALE
    )
}

internal fun resolveHomeVideoSharedTransitionCornerSpec(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoSharedCornerSpec {
    val enabled = transitionEnabled &&
        sourceRoute?.substringBefore("?") == HOME_SOURCE_ROUTE
    return if (enabled) {
        VideoSharedCornerSpec(
            enabled = true,
            startCornerDp = HOME_SHARED_TRANSITION_CARD_CORNER_DP,
            endCornerDp = HOME_SHARED_TRANSITION_PLAYER_CORNER_DP
        )
    } else {
        VideoSharedCornerSpec(
            enabled = false,
            startCornerDp = 0,
            endCornerDp = 0
        )
    }
}

internal fun resolveVideoDetailContentRevealMotion(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoDetailContentRevealMotion {
    val homeMotionSpec = resolveHomeVideoSharedTransitionMotionSpec(
        sourceRoute = sourceRoute,
        transitionEnabled = transitionEnabled
    )
    val isHomeSharedTransition = homeMotionSpec.enabled
    if (!isHomeSharedTransition) {
        return VideoDetailContentRevealMotion(
            enabled = false,
            delayMillis = 0,
            durationMillis = 0,
            slideOffsetDp = 0,
            initialScale = 1f
        )
    }

    val revealMotion = resolveDetailVerticalContentRevealMotionSpec(
        delayMillis = homeMotionSpec.contentDelayMillis,
        durationMillis = homeMotionSpec.contentDurationMillis,
        slideOffsetDp = homeMotionSpec.contentSlideOffsetDp.toFloat(),
        initialScale = homeMotionSpec.contentInitialScale
    )

    return VideoDetailContentRevealMotion(
        enabled = true,
        delayMillis = revealMotion.delayMillis,
        durationMillis = revealMotion.durationMillis,
        slideOffsetDp = revealMotion.slideOffsetDp.toInt(),
        initialScale = revealMotion.initialScale
    )
}
