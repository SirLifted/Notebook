package com.example.notebook.theme

import androidx.compose.ui.graphics.Color

data class NoteThemeColor(
    val name: String,
    val lightBg: Color,
    val darkBg: Color,
    val lightBorder: Color,
    val darkBorder: Color
)

object NoteColors {
    val PALETTE = listOf(
        NoteThemeColor(
            name = "Default",
            lightBg = Color(0xFFFFFFFF),
            darkBg = Color(0xFF1E1F22),
            lightBorder = Color(0xFFE0E2EC),
            darkBorder = Color(0xFF44474E)
        ),
        NoteThemeColor(
            name = "Lemon",
            lightBg = Color(0xFFFFF9C4),
            darkBg = Color(0xFF33301B),
            lightBorder = Color(0xFFFFEE58),
            darkBorder = Color(0xFF5A522A)
        ),
        NoteThemeColor(
            name = "Mint",
            lightBg = Color(0xFFE8F5E9),
            darkBg = Color(0xFF1B2F21),
            lightBorder = Color(0xFFA5D6A7),
            darkBorder = Color(0xFF2E4E36)
        ),
        NoteThemeColor(
            name = "Sky",
            lightBg = Color(0xFFE1F5FE),
            darkBg = Color(0xFF162936),
            lightBorder = Color(0xFF81D4FA),
            darkBorder = Color(0xFF24465B)
        ),
        NoteThemeColor(
            name = "Lavender",
            lightBg = Color(0xFFF3E5F5),
            darkBg = Color(0xFF2D1F35),
            lightBorder = Color(0xFFCE93D8),
            darkBorder = Color(0xFF4E375C)
        ),
        NoteThemeColor(
            name = "Peach",
            lightBg = Color(0xFFFFE0B2),
            darkBg = Color(0xFF352618),
            lightBorder = Color(0xFFFFB74D),
            darkBorder = Color(0xFF5B3E25)
        ),
        NoteThemeColor(
            name = "Rose",
            lightBg = Color(0xFFFCE4EC),
            darkBg = Color(0xFF351A25),
            lightBorder = Color(0xFFF48FB1),
            darkBorder = Color(0xFF5B2E40)
        ),
        NoteThemeColor(
            name = "Almond",
            lightBg = Color(0xFFEFEBE9),
            darkBg = Color(0xFF292422),
            lightBorder = Color(0xFFBCAAA4),
            darkBorder = Color(0xFF4B423E)
        )
    )

    fun getColor(colorIndex: Int, isDark: Boolean): Color {
        val safeIndex = if (colorIndex in PALETTE.indices) colorIndex else 0
        val theme = PALETTE[safeIndex]
        return if (isDark) theme.darkBg else theme.lightBg
    }

    fun getBorderColor(colorIndex: Int, isDark: Boolean): Color {
        val safeIndex = if (colorIndex in PALETTE.indices) colorIndex else 0
        val theme = PALETTE[safeIndex]
        return if (isDark) theme.darkBorder else theme.lightBorder
    }
}
