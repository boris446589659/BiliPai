package com.android.purebilibili.navigation3

import com.android.purebilibili.core.store.PredictiveBackAnimationStyle
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEvent.Companion.EDGE_LEFT
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BiliPaiPredictiveBackMotionPolicyTest {

    @Test
    fun predictiveBackMotionClasses_preserveInstallerXStyleMapping() {
        val handlers: List<Any> = listOf(
            BiliPaiNoPredictiveBackMotion(),
            BiliPaiAospPredictiveBackMotion(),
            BiliPaiMiuixPredictiveBackMotion(),
            BiliPaiScalePredictiveBackMotion(),
            BiliPaiClassicPredictiveBackMotion()
        )

        handlers.forEach { handler ->
            assertTrue(handler is BiliPaiPredictiveBackMotionHandler)
        }
    }

    @Test
    fun predictiveBackStyleValues_matchPersistedWireValues() {
        assertEquals("none", PredictiveBackAnimationStyle.NONE.value)
        assertEquals("aosp", PredictiveBackAnimationStyle.AOSP.value)
        assertEquals("miuix", PredictiveBackAnimationStyle.MIUIX.value)
        assertEquals("scale", PredictiveBackAnimationStyle.SCALE.value)
        assertEquals("ksu_classic", PredictiveBackAnimationStyle.CLASSIC.value)
    }

    @Test
    fun screenshotAlignedStyles_exitToRightByDefault() {
        assertEquals(1f, resolveBiliPaiPredictiveBackExitDirectionMultiplier(PredictiveBackAnimationStyle.AOSP))
        assertEquals(1f, resolveBiliPaiPredictiveBackExitDirectionMultiplier(PredictiveBackAnimationStyle.SCALE))
    }

    @Test
    fun gestureEdgeOnlyChangesPivotNotExitDirection() {
        val leftPivot = resolveBiliPaiPredictiveBackPivot(
            swipeEdge = EDGE_LEFT,
            touchY = 600f,
            containerHeightPx = 1200
        )
        val rightPivot = resolveBiliPaiPredictiveBackPivot(
            swipeEdge = EDGE_RIGHT,
            touchY = 600f,
            containerHeightPx = 1200
        )

        assertEquals(0.8f, leftPivot.x)
        assertEquals(0.2f, rightPivot.x)
        assertEquals(0.5f, leftPivot.y)
        assertEquals(0.5f, rightPivot.y)
        assertEquals(1f, resolveBiliPaiPredictiveBackExitDirectionMultiplier(PredictiveBackAnimationStyle.AOSP))
    }

    @Test
    fun cornerRadiusFallsBackToTwentyEightDpWhenDeviceRadiusMissing() {
        assertEquals(28.dp, resolveBiliPaiPredictiveBackCornerRadius(deviceCornerRadius = null))
    }

    @Test
    fun cornerRadiusUsesDeviceRadiusWhenAvailable() {
        assertEquals(36.dp, resolveBiliPaiPredictiveBackCornerRadius(deviceCornerRadius = 36.dp))
    }

    @Test
    fun classicStyleUsesLayeredPopMotionWithoutFadeOrScale() {
        val motion = resolveBiliPaiClassicLayeredPopMotion()

        assertEquals(-0.25f, motion.targetEnterInitialOffsetFraction)
        assertEquals(1f, motion.initialExitTargetOffsetFraction)
        assertEquals(false, motion.fadeExitingPage)
        assertEquals(false, motion.scaleExitingPage)
    }
}
