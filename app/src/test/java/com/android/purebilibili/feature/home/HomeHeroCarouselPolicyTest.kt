package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.math.abs

class HomeHeroCarouselPolicyTest {

    @Test
    fun `carousel only shows on recommend page with items when enabled`() {
        assertTrue(
            shouldShowHomeHeroCarousel(
                enabled = true,
                category = HomeCategory.RECOMMEND,
                itemCount = 1
            )
        )
        assertFalse(
            shouldShowHomeHeroCarousel(
                enabled = false,
                category = HomeCategory.RECOMMEND,
                itemCount = 1
            )
        )
        assertFalse(
            shouldShowHomeHeroCarousel(
                enabled = true,
                category = HomeCategory.POPULAR,
                itemCount = 1
            )
        )
        assertFalse(
            shouldShowHomeHeroCarousel(
                enabled = true,
                category = HomeCategory.RECOMMEND,
                itemCount = 0
            )
        )
    }

    @Test
    fun `carousel uses bounded leading feed items`() {
        assertEquals(
            listOf(1, 2, 3),
            selectHomeHeroCarouselItems(listOf(1, 2, 3), maxItems = 8)
        )
        assertEquals(
            (1..8).toList(),
            selectHomeHeroCarouselItems((1..20).toList(), maxItems = 8)
        )
        assertEquals(
            emptyList(),
            selectHomeHeroCarouselItems((1..20).toList(), maxItems = 0)
        )
    }

    @Test
    fun `carousel transform rotates outgoing card away from drag direction`() {
        val centered = resolveHomeHeroCarouselCardTransform(0f)
        assertTrue(abs(centered.rotationY) < 0.001f)
        assertTrue(abs(centered.scale - 1f) < 0.001f)
        assertTrue(abs(centered.alpha - 1f) < 0.001f)
        assertTrue(abs(centered.translationXFraction) < 0.001f)
        assertTrue(abs(centered.pivotFractionX - 0.5f) < 0.001f)

        val left = resolveHomeHeroCarouselCardTransform(-1f)
        val right = resolveHomeHeroCarouselCardTransform(1f)
        assertTrue(left.rotationY > 0f)
        assertTrue(right.rotationY < 0f)
        assertTrue(left.translationXFraction < 0f)
        assertTrue(right.translationXFraction > 0f)
        assertEquals(0f, left.pivotFractionX)
        assertEquals(1f, right.pivotFractionX)
        assertEquals(left.scale, right.scale)
        assertEquals(left.alpha, right.alpha)
        assertTrue(left.scale < centered.scale)
        assertTrue(left.alpha < centered.alpha)
    }

    @Test
    fun `carousel preview stays hidden until first frame is rendered`() {
        assertEquals(0f, resolveHomeHeroCarouselPreviewAlpha(hasRenderedFirstFrame = false))
        assertEquals(1f, resolveHomeHeroCarouselPreviewAlpha(hasRenderedFirstFrame = true))
    }
}
