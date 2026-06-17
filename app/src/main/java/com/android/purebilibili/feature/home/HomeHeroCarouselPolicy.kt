package com.android.purebilibili.feature.home

internal const val HOME_HERO_CAROUSEL_MAX_ITEMS = 8

internal data class HomeHeroCarouselCardTransform(
    val rotationY: Float,
    val scale: Float,
    val alpha: Float,
    val cameraDistanceMultiplier: Float,
    val translationXFraction: Float,
    val pivotFractionX: Float,
    val zIndex: Float
)

internal fun <T> selectHomeHeroCarouselItems(
    items: List<T>,
    maxItems: Int = HOME_HERO_CAROUSEL_MAX_ITEMS
): List<T> {
    if (maxItems <= 0) return emptyList()
    return items.take(maxItems)
}

internal fun shouldShowHomeHeroCarousel(
    enabled: Boolean,
    category: HomeCategory,
    itemCount: Int
): Boolean {
    return enabled && category == HomeCategory.RECOMMEND && itemCount > 0
}

internal fun resolveHomeHeroCarouselCardTransform(
    pageOffset: Float
): HomeHeroCarouselCardTransform {
    val clampedOffset = pageOffset.coerceIn(-1f, 1f)
    val distance = kotlin.math.abs(clampedOffset)
    val pivotFractionX = when {
        clampedOffset < -0.001f -> 0f
        clampedOffset > 0.001f -> 1f
        else -> 0.5f
    }
    return HomeHeroCarouselCardTransform(
        rotationY = -clampedOffset * 58f,
        scale = 1f - distance * 0.1f,
        alpha = 1f - distance * 0.18f,
        cameraDistanceMultiplier = 10f,
        translationXFraction = clampedOffset * 0.1f,
        pivotFractionX = pivotFractionX,
        zIndex = 1f - distance
    )
}

internal fun resolveHomeHeroCarouselPreviewAlpha(
    hasRenderedFirstFrame: Boolean
): Float = if (hasRenderedFirstFrame) 1f else 0f
