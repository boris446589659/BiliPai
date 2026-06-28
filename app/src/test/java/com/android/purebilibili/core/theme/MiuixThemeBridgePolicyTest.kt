package com.android.purebilibili.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class MiuixThemeBridgePolicyTest {

    @Test
    fun `material bridge preserves primary and surface roles from miuix colors`() {
        val materialScheme = resolveMaterialColorSchemeFromMiuixBridge(
            bridge = MiuixMaterialBridge(
                primary = Color(0xFF3482FF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE1ECFF),
                onPrimaryContainer = Color(0xFF001C3A),
                secondary = Color(0xFF5A5F71),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFDEE3F9),
                onSecondaryContainer = Color(0xFF171B2C),
                tertiary = Color(0xFF75546F),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFD7F5),
                onTertiaryContainer = Color(0xFF2C1229),
                error = Color(0xFFBA1A1A),
                onError = Color.White,
                background = Color(0xFFF8F9FF),
                onBackground = Color(0xFF191C20),
                surface = Color(0xFFF8F9FF),
                onSurface = Color(0xFF191C20),
                surfaceVariant = Color(0xFFE0E2EC),
                onSurfaceVariant = Color(0xFF44474E),
                surfaceContainer = Color(0xFFECEEF4),
                surfaceContainerHigh = Color(0xFFE6E8EE),
                outline = Color(0xFF74777F),
                outlineVariant = Color(0xFFC4C6D0)
            ),
            amoledDarkTheme = false
        )

        assertEquals(Color(0xFF3482FF), materialScheme.primary)
        assertEquals(Color(0xFFF8F9FF), materialScheme.background)
        assertEquals(Color(0xFFF8F9FF), materialScheme.surface)
        assertEquals(Color(0xFFECEEF4), materialScheme.surfaceContainer)
        assertEquals(Color(0xFFE6E8EE), materialScheme.surfaceContainerHigh)
    }

    @Test
    fun `miuix colors follow amoled material surfaces from bridge`() {
        val amoledScheme = applyAmoledSurfaceOverrides(
            darkColorScheme(
                primary = Color(0xFF84F2A4),
                background = Color(0xFF101414),
                surface = Color(0xFF161B1A),
                surfaceContainer = Color(0xFF1E2523)
            )
        )
        val bridge = createMiuixMaterialBridge(amoledScheme)
        val miuixColors = resolveMiuixColorsFromMaterialBridge(
            bridge = bridge,
            darkTheme = true
        )

        assertEquals(Color(0xFF84F2A4), miuixColors.primary)
        assertEquals(Color.Black, miuixColors.background)
        assertEquals(Color.Black, miuixColors.surface)
        assertEquals(Color(0xFF090909), miuixColors.surfaceContainer)
    }

    @Test
    fun `miuix colors track custom seed primary from material bridge`() {
        val seedPrimary = Color(0xFFFF5722)
        val bridge = createMiuixMaterialBridge(
            lightColorScheme(
                primary = seedPrimary,
                onPrimary = Color.White,
                background = Color(0xFFFFF8F6),
                surface = Color(0xFFFFF8F6),
                surfaceContainer = Color(0xFFFFEDE8)
            )
        )
        val miuixColors = resolveMiuixColorsFromMaterialBridge(
            bridge = bridge,
            darkTheme = false
        )

        assertEquals(seedPrimary, miuixColors.primary)
        assertEquals(Color(0xFFFFF8F6), miuixColors.background)
    }
}
