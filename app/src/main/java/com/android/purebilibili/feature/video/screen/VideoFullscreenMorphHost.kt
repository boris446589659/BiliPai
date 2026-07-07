package com.android.purebilibili.feature.video.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun VideoFullscreenMorphHost(
    presentationMode: VideoFullscreenPresentationMode,
    morphMotionSpec: VideoFullscreenMorphMotionSpec,
    bvid: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onTransitionPhaseChanged: (VideoFullscreenTransitionPhase) -> Unit,
    onMorphAnimatingChanged: (Boolean) -> Unit,
    onMorphFinished: (VideoFullscreenPresentationMode) -> Unit,
    modifier: Modifier = Modifier,
    inlineContent: @Composable () -> Unit,
    landscapeFullscreenContent: @Composable () -> Unit
) {
    val landscapeTarget = presentationMode == VideoFullscreenPresentationMode.LandscapeFullscreen
    var previousLandscapeTarget by remember { mutableStateOf(landscapeTarget) }
    var isMorphAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(landscapeTarget, morphMotionSpec.enabled, morphMotionSpec.durationMillis) {
        if (previousLandscapeTarget == landscapeTarget) return@LaunchedEffect
        if (!morphMotionSpec.enabled) {
            previousLandscapeTarget = landscapeTarget
            onMorphFinished(presentationMode)
            return@LaunchedEffect
        }
        isMorphAnimating = true
        onMorphAnimatingChanged(true)
        delay(morphMotionSpec.durationMillis.toLong())
        isMorphAnimating = false
        onMorphAnimatingChanged(false)
        previousLandscapeTarget = landscapeTarget
        onMorphFinished(presentationMode)
    }

    val transitionPhase = when {
        !isMorphAnimating -> VideoFullscreenTransitionPhase.Idle
        landscapeTarget -> VideoFullscreenTransitionPhase.EnteringLandscape
        else -> VideoFullscreenTransitionPhase.ExitingLandscapeToInline
    }

    LaunchedEffect(transitionPhase) {
        onTransitionPhaseChanged(transitionPhase)
    }

    if (!morphMotionSpec.enabled ||
        sharedTransitionScope == null ||
        animatedVisibilityScope == null
    ) {
        if (landscapeTarget) {
            landscapeFullscreenContent()
        } else {
            inlineContent()
        }
        return
    }

    val morphDuration = morphMotionSpec.durationMillis
    val morphEasing = resolveVideoFullscreenMorphEasing(entering = landscapeTarget)
    val morphTween = tween<Float>(durationMillis = morphDuration, easing = morphEasing)

    AnimatedContent(
        targetState = landscapeTarget,
        modifier = modifier,
        transitionSpec = {
            if (initialState == targetState) {
                EnterTransition.None togetherWith ExitTransition.None
            } else if (targetState) {
                fadeIn(animationSpec = morphTween) togetherWith fadeOut(animationSpec = morphTween)
            } else {
                // 退出横屏：弱化 crossfade，让 sharedBounds 主导落位，避免与系统旋转叠影
                (fadeIn(initialAlpha = 0.92f, animationSpec = morphTween) +
                    scaleIn(initialScale = 0.98f, animationSpec = morphTween)) togetherWith
                    (fadeOut(animationSpec = morphTween) +
                        scaleOut(targetScale = 0.98f, animationSpec = morphTween))
            }
        },
        label = "videoDetailFullscreenMorph",
        contentKey = { landscape -> if (landscape) "landscape" else "inline" }
    ) { landscapeActive ->
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            key = videoDetailPlayerShellSharedElementKey(bvid)
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = morphDuration, easing = morphEasing)
                        },
                        clipInOverlayDuringTransition = OverlayClip(
                            RoundedCornerShape(if (landscapeActive) 0.dp else 12.dp)
                        )
                    )
                    .clip(RoundedCornerShape(if (landscapeActive) 0.dp else 12.dp))
            ) {
                if (landscapeActive) {
                    landscapeFullscreenContent()
                } else {
                    inlineContent()
                }
            }
        }
    }
}
