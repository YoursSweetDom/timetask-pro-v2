package com.timetask.pro.v2.presentation.components

import androidx.compose.ui.unit.sp

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified

/**
 * A standard `Text` composable that automatically scales down the `fontSize`
 * if the text overflows its horizontal bounds, ensuring it stays on a single line.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = false,
    maxLines: Int = 1,
    minFontSize: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    var scaledTextStyle by remember(style, fontSize) {
        mutableStateOf(style.copy(fontSize = if (fontSize.isUnspecified) style.fontSize else fontSize))
    }
    
    var readyToDraw by remember(style, fontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        color = color,
        fontSize = scaledTextStyle.fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                // Determine new font size
                val currentFontSize = scaledTextStyle.fontSize
                val reduceBy = currentFontSize.value * 0.1f // reduce by 10%
                
                val nextFontSize = (currentFontSize.value - reduceBy).sp
                
                if (minFontSize.isUnspecified || nextFontSize.value >= minFontSize.value) {
                    scaledTextStyle = scaledTextStyle.copy(fontSize = nextFontSize)
                } else {
                    scaledTextStyle = scaledTextStyle.copy(fontSize = minFontSize)
                    readyToDraw = true // Reached minimum, must draw
                }
            } else {
                readyToDraw = true // No overflow, safe to draw
            }
        },
        style = style
    )
}
