package com.android.purebilibili.feature.video.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VideoDetailReturnCoverPolicyTest {

    @Test
    fun `force cover becomes active when explicit return flag is true`() {
        assertTrue(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = true,
                isReturningFromDetail = false,
                isExitTransitionInProgress = false
            )
        )
    }

    @Test
    fun `force cover becomes active when global returning state is true`() {
        assertTrue(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false,
                isReturningFromDetail = true,
                isExitTransitionInProgress = false
            )
        )
    }

    @Test
    fun `detail shell shared bounds does not disable return cover visual`() {
        assertTrue(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = true,
                isReturningFromDetail = true,
                isExitTransitionInProgress = true
            )
        )
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreen.kt")
            .readText()
        val policyBlock = source
            .substringAfter("internal fun resolveForceCoverOnlyForReturn(")
            .substringBefore("internal fun shouldUseReturningVideoDetailVisualState")
        assertFalse(policyBlock.contains("detailShellSharedBoundsEnabled"))
    }

    @Test
    fun `force cover stays disabled when shared transition is disabled`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = true,
                isReturningFromDetail = true,
                isExitTransitionInProgress = false,
                transitionEnabled = false
            )
        )
    }

    @Test
    fun `force cover stays disabled when only exit transition is in progress`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false,
                isReturningFromDetail = false,
                isExitTransitionInProgress = true
            )
        )
    }

    @Test
    fun `force cover stays disabled when no return state is active`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false,
                isReturningFromDetail = false,
                isExitTransitionInProgress = false
            )
        )
    }

    @Test
    fun `predictive exit alone does not switch detail into returning visual state`() {
        assertFalse(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = false,
                isReturningFromDetail = false,
                isExitTransitionInProgress = true
            )
        )
    }

    @Test
    fun `explicit return state switches detail into returning visual state`() {
        assertTrue(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = true,
                isReturningFromDetail = false,
                isExitTransitionInProgress = false
            )
        )
        assertTrue(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = false,
                isReturningFromDetail = true,
                isExitTransitionInProgress = false
            )
        )
    }

    @Test
    fun `cover takeover delay keeps a one-frame budget before back navigation`() {
        assertEquals(16L, resolveCoverTakeoverDelayBeforeBackNavigationMillis())
    }
}
