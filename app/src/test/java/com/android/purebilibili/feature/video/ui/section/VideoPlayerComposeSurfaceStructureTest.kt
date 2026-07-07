package com.android.purebilibili.feature.video.ui.section

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class VideoPlayerComposeSurfaceStructureTest {

    @Test
    fun composeSurfaceHost_replacesInlinePlayerViewInVideoPlayerSection() {
        val source = readSource(
            "com/android/purebilibili/feature/video/ui/section/VideoPlayerSection.kt"
        )

        assertTrue(source.contains("VideoPlayerSurfacePresentationHost"))
        assertTrue(!source.contains("PlayerView(ctx)"))
        assertTrue(source.contains("videoSurfaceRef"))
    }

    @Test
    fun composeSurfaceHost_replacesPlayerViewInPortraitVideoPager() {
        val source = readSource(
            "com/android/purebilibili/feature/video/ui/pager/PortraitVideoPager.kt"
        )

        assertTrue(source.contains("VideoPlayerSurfacePresentationHost"))
        assertTrue(!source.contains("PlayerView(ctx)"))
        assertTrue(source.contains("videoSurfaceRef"))
    }

    @Test
    fun composeSurfaceHost_replacesPlayerViewInFullscreenPlayerOverlay() {
        val source = readSource(
            "com/android/purebilibili/feature/video/ui/overlay/FullscreenPlayerOverlay.kt"
        )

        assertTrue(source.contains("VideoPlayerSurfacePresentationHost"))
        assertTrue(!source.contains("PlayerView(ctx)"))
        assertTrue(source.contains("videoSurfaceRef"))
    }

    @Test
    fun composeSurfaceHost_replacesPlayerViewInLivePlayerScreen() {
        val source = readSource(
            "com/android/purebilibili/feature/live/LivePlayerScreen.kt"
        )

        assertTrue(source.contains("VideoPlayerSurfacePresentationHost"))
        assertTrue(!source.contains("PlayerView(ctx)"))
        assertTrue(source.contains("videoSurfaceRef"))
    }

    private fun readSource(relativePath: String): String {
        return listOf(
            File("app/src/main/java/$relativePath"),
            File("src/main/java/$relativePath"),
        ).first { it.exists() }.readText()
    }
}
