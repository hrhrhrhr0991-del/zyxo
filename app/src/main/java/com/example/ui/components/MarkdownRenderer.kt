package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PeydaFontFamily
import com.example.ui.theme.VioletGlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false,
    isStreaming: Boolean = false
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    HeaderView(block = block, isUser = isUser)
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(language = block.language, code = block.code)
                }
                is MarkdownBlock.BulletItem -> {
                    BulletItemView(text = block.text, isUser = isUser)
                }
                is MarkdownBlock.NumberedItem -> {
                    NumberedItemView(number = block.number, text = block.text, isUser = isUser)
                }
                is MarkdownBlock.Quote -> {
                    QuoteView(text = block.text, isUser = isUser)
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    ParagraphView(text = block.text, isUser = isUser)
                }
            }
        }

        // ChatGPT-style blinking cursor when response is currently generating
        if (isStreaming) {
            val infiniteTransition = rememberInfiniteTransition(label = "cursor")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cursorAlpha"
            )
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .alpha(alpha)
                    .background(if (isUser) Color.White else CyanSpark)
            )
        }
    }
}

@Composable
private fun HeaderView(block: MarkdownBlock.Header, isUser: Boolean) {
    val baseColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary

    val (style, topPadding) = when (block.level) {
        1 -> MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ) to 6.dp
        2 -> MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ) to 5.dp
        else -> MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ) to 4.dp
    }

    Column(modifier = Modifier.padding(top = topPadding, bottom = 2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (block.level == 1) 16.dp else 13.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isUser) Color.White.copy(alpha = 0.7f) else CyanSpark)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = renderInlineStyles(block.text, isUser),
                style = style,
                color = baseColor
            )
        }
    }
}

@Composable
private fun ParagraphView(text: String, isUser: Boolean) {
    Text(
        text = renderInlineStyles(text, isUser),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 22.sp
        ),
        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BulletItemView(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isUser) Color.White else CyanSpark)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = renderInlineStyles(text, isUser),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NumberedItemView(number: String, text: String, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    if (isUser) Color.White.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = renderInlineStyles(text, isUser),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuoteView(text: String, isUser: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isUser) Color.White.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .border(
                width = 1.dp,
                color = if (isUser) Color.White.copy(alpha = 0.3f) else VioletGlow.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = renderInlineStyles(text, isUser),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CodeBlockView(language: String, code: String) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
    ) {
        Column {
            // Header bar with language tag and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language.isNotBlank()) language.uppercase() else "CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanSpark,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        coroutineScope.launch {
                            isCopied = true
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "کپی کد",
                        tint = if (isCopied) CyanSpark else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF1F5F9)
                )
            }
        }
    }
}

@Composable
fun renderInlineStyles(text: String, isUser: Boolean): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val regex = Regex("""(\*\*([^*]+)\*\*)|(`([^`]+)`)|(\*([^*]+)\*)""")
        val matches = regex.findAll(text)

        for (match in matches) {
            val range = match.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val fullMatch = match.value
            when {
                // Bold: **text**
                fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
                    val inner = fullMatch.removeSurrounding("**")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontFamily = PeydaFontFamily,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append(inner)
                    }
                }
                // Inline code: `code`
                fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
                    val inner = fullMatch.removeSurrounding("`")
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            background = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            color = if (isUser) Color.White else CyanSpark
                        )
                    ) {
                        append(" $inner ")
                    }
                }
                // Italic: *text*
                fullMatch.startsWith("*") && fullMatch.endsWith("*") -> {
                    val inner = fullMatch.removeSurrounding("*")
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(inner)
                    }
                }
                else -> {
                    append(fullMatch)
                }
            }
            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class Quote(val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        when {
            // Fenced code block start
            trimmed.startsWith("```") -> {
                val language = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
                i++
            }
            // Headers: #, ##, ###, ####
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceIn(1, 3)
                val headerText = trimmed.removePrefix("#".repeat(level)).trim()
                blocks.add(MarkdownBlock.Header(level, headerText))
                i++
            }
            // Divider: --- or ***
            trimmed == "---" || trimmed == "***" -> {
                blocks.add(MarkdownBlock.Divider)
                i++
            }
            // Blockquote: > text
            trimmed.startsWith(">") -> {
                val quoteText = trimmed.removePrefix(">").trim()
                blocks.add(MarkdownBlock.Quote(quoteText))
                i++
            }
            // Bullet items: * item, - item, • item
            trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
                val itemText = trimmed.substring(2).trim()
                blocks.add(MarkdownBlock.BulletItem(itemText))
                i++
            }
            // Numbered items: 1. item, 2. item
            trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                val dotIndex = trimmed.indexOf('.')
                val number = trimmed.substring(0, dotIndex)
                val itemText = trimmed.substring(dotIndex + 1).trim()
                blocks.add(MarkdownBlock.NumberedItem(number, itemText))
                i++
            }
            // Empty line
            trimmed.isEmpty() -> {
                i++
            }
            // Regular paragraph
            else -> {
                val paragraphLines = mutableListOf<String>()
                paragraphLines.add(line)
                i++
                while (i < lines.size) {
                    val nextTrimmed = lines[i].trim()
                    if (nextTrimmed.isEmpty() ||
                        nextTrimmed.startsWith("#") ||
                        nextTrimmed.startsWith("```") ||
                        nextTrimmed.startsWith("* ") ||
                        nextTrimmed.startsWith("- ") ||
                        nextTrimmed.startsWith("• ") ||
                        nextTrimmed.matches(Regex("""^\d+\.\s+.*""")) ||
                        nextTrimmed.startsWith(">") ||
                        nextTrimmed == "---"
                    ) {
                        break
                    }
                    paragraphLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString("\n")))
            }
        }
    }

    return blocks
}
