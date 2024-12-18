package com.decomposer.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle

@Immutable
data class Theme(
    val materialColors: Colors,
    val code: CodeStyle
) {
    @Immutable
    data class CodeStyle(
        val simple: SpanStyle,
        val value: SpanStyle,
        val keyword: SpanStyle,
        val punctuation: SpanStyle,
        val annotation: SpanStyle,
        val comment: SpanStyle,
        val function: SpanStyle,
        val highlight: SpanStyle
    )

    companion object {
        val dark = Theme(
            materialColors = darkColors(
                background = Color(0xFF2B2B2B),
                surface = Color(0xFF3C3F41)
            ),
            code = CodeStyle(
                simple = SpanStyle(Color(0xFFC9D7E6)),
                value = SpanStyle(Color(0xFF6897BB)),
                keyword = SpanStyle(Color(0xFFCC7832)),
                punctuation = SpanStyle(Color(0xFFA1C17E)),
                annotation = SpanStyle(Color(0xFFBBB529)),
                comment = SpanStyle(Color(0xFF808080)),
                function = SpanStyle(Color(0xFFC9D7E6), fontStyle = FontStyle.Italic),
                highlight = SpanStyle(background = Color(0xAA569CD6))
            )
        )

        val light = Theme(
            materialColors = lightColors(
                background = Color(0xFFF5F5F5),
                surface = Color(0xFFFFFFFF)
            ),
            code = CodeStyle(
                simple = SpanStyle(Color(0xFF000000)),
                value = SpanStyle(Color(0xFF4A86E8)),
                keyword = SpanStyle(Color(0xFF000080)),
                punctuation = SpanStyle(Color(0xFFA1A1A1)),
                annotation = SpanStyle(Color(0xFFBBB529)),
                comment = SpanStyle(Color(0xFF808080)),
                function = SpanStyle(Color(0xFF000000), fontStyle = FontStyle.Italic),
                highlight = SpanStyle(background = Color(0xAA569CD6))
            )
        )
    }
}

@Composable
fun DecomposerTheme(content: @Composable () -> Unit) {
    val theme = LocalTheme.current
    MaterialTheme(colors = theme.materialColors) {
        content()
    }
}

val LocalTheme = compositionLocalOf { Theme.dark }
val LocalFontSize = compositionLocalOf { 16 }
