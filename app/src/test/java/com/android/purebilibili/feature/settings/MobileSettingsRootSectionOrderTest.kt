package com.android.purebilibili.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class MobileSettingsRootSectionOrderTest {

    @Test
    fun shouldUseSceneBasedOrderForSettingsHome() {
        assertEquals(
            resolveSettingsRootCategoryOrder(),
            resolveTabletSettingsRootCategoryOrder()
        )
    }

    @Test
    fun rootSections_shouldUseSceneTitles() {
        assertEquals(
            listOf(
                "外观与交互",
                "内容与播放",
                "隐私与存储",
                "系统与关于"
            ),
            resolveSettingsRootCategoryOrder().map { it.title }
        )
    }
}
