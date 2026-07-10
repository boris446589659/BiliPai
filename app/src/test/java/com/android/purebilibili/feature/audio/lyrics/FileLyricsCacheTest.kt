package com.android.purebilibili.feature.audio.lyrics

import java.nio.file.Files
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FileLyricsCacheTest {

    @Test
    fun `cache round trips lyric document with stable key`() = runTest {
        val directory = Files.createTempDirectory("bilipai-lyrics").toFile()
        val cache = FileLyricsCache(directory)
        val document = parseSplLyrics("[00:01.00]Cached")
            .copy(source = LyricSource.QQ_MUSIC, remoteId = "qq-1", manuallySelected = true)

        cache.write("video:BV1/test:2", document)

        assertEquals(document, cache.read("video:BV1/test:2"))
    }
}
