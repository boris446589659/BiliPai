package com.android.purebilibili.navigation

internal data class PortraitStoryNavigationSeed(
    val bvid: String,
    val cid: Long,
    val coverUrl: String
)

internal fun resolvePortraitStoryNavigationSeed(
    directPortraitStoryEntry: Boolean,
    isVerticalVideo: Boolean,
    startAudio: Boolean,
    bvid: String,
    cid: Long = 0L,
    coverUrl: String = ""
): PortraitStoryNavigationSeed? {
    val normalizedBvid = bvid.trim()
    if (!directPortraitStoryEntry || !isVerticalVideo || startAudio || normalizedBvid.isEmpty()) {
        return null
    }
    return PortraitStoryNavigationSeed(
        bvid = normalizedBvid,
        cid = cid.coerceAtLeast(0L),
        coverUrl = coverUrl
    )
}