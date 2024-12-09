package com.decomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.ir.IrProcessor
import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.server.Session
import java.nio.file.Paths

@Composable
fun IrPanel(
    modifier: Modifier = Modifier,
    session: Session,
    irProcessor: IrProcessor,
    filePath: String?,
    highlight: Pair<Int, Int>?,
    onShowPopup: (@Composable () -> Unit) -> Unit
) {
    var compose by remember {
        mutableStateOf(true)
    }

    var kotlinLike by remember {
        mutableStateOf(true)
    }

    var kotlinLikeIr by remember {
        mutableStateOf<AnnotatedString?>(null)
    }

    var standardIr by remember {
        mutableStateOf<String?>(null)
    }

    Box(
        modifier = modifier
    ) {
        val kotlinLikeIrDump = kotlinLikeIr
        val standardIrDump = standardIr
        if (kotlinLikeIrDump == null || standardIrDump == null) {
            DefaultPanelText(
                text = """
                    Select a file to view ir!
                """.trimIndent(),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                ) {
                    ComposeToggle(
                        text = "Compose",
                        checked = compose,
                        onCheckedChanged = {
                            compose = !compose
                        }
                    )
                    ComposeToggle(
                        text = "Kotlin like",
                        checked = kotlinLike,
                        onCheckedChanged = {
                            kotlinLike = !kotlinLike
                        }
                    )
                }
                CodeContent(filePath, kotlinLikeIrDump, standardIrDump, kotlinLike, highlight)
            }
        }
    }

    val theme = if (isSystemInDarkTheme()) Theme.dark else Theme.light

    LaunchedEffect(filePath, compose, session.sessionId, highlight) {
        if (filePath != null) {
            val virtualFileIr = session.getVirtualFileIr(filePath)
            if (!virtualFileIr.isEmpty) {
                irProcessor.processVirtualFileIr(virtualFileIr)
                val kotlinFile = if (compose) {
                    irProcessor.composedFile(filePath)
                } else {
                    irProcessor.originalFile(filePath)
                }
                val irVisualBuilder = IrVisualBuilder(
                    kotlinFile = kotlinFile,
                    theme = theme,
                    highlights = highlight?.let { listOf(it) } ?: emptyList()
                ) {
                    onShowPopup @Composable { IrDescription(it.description) }
                }
                kotlinLikeIr = irVisualBuilder.visualize().annotatedString
                standardIr = kotlinFile.standardIrDump
            } else {
                kotlinLikeIr = null
                standardIr = null
            }
        }
    }
}

@Composable
private fun IrDescription(text: String) {
    DefaultPanelText(text)
}

private val VirtualFileIr.isEmpty: Boolean
    get() {
        return this.composedIrFile.isEmpty() &&
                this.composedTopLevelIrClasses.isEmpty() &&
                this.originalIrFile.isEmpty() &&
                this.originalTopLevelIrClasses.isEmpty()
    }

@Composable
fun CodeContent(
    filePath: String?,
    kotlinLikeIr: AnnotatedString,
    standardIr: String,
    kotlinLike: Boolean,
    highlight: Pair<Int, Int>?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        filePath?.let {
            DefaultPanelText(
                text = Paths.get(it).fileName.toString(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val verticalScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState),
            ) {
                LineNumbers(
                    length = if (kotlinLike) {
                        kotlinLikeIr.lines().size
                    } else {
                        standardIr.lines().size
                    }
                )
                SelectionContainer {
                    if (kotlinLike) {
                        var textLayoutResult: TextLayoutResult? by remember {
                            mutableStateOf(null)
                        }

                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            text = kotlinLikeIr,
                            fontFamily = Fonts.jetbrainsMono(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 36.sp,
                            onTextLayout = { textLayoutResult = it }
                        )

                        LaunchedEffect(highlight, kotlinLikeIr, textLayoutResult) {
                            val layoutResult = textLayoutResult ?: return@LaunchedEffect
                            highlight?.let {
                                val annotation = kotlinLikeIr.getStringAnnotations(
                                    tag = IrVisualBuilder.TAG_SOURCE_LOCATION,
                                    start = highlight.first,
                                    end = highlight.second
                                ).firstOrNull()

                                if (annotation != null) {
                                    val top = layoutResult.getBoundingBox(annotation.start).top
                                    verticalScrollState.animateScrollTo(top.toInt())
                                }
                            }
                        }
                    } else {
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            text = standardIr,
                            fontFamily = Fonts.jetbrainsMono(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 36.sp
                        )
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(verticalScrollState)
            )

            HorizontalScrollbar(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                adapter = rememberScrollbarAdapter(horizontalScrollState)
            )
        }
    }
}

@Composable
fun LineNumbers(
    length: Int
) {
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .fillMaxHeight()
            .padding(end = 8.dp),
        horizontalAlignment = Alignment.End
    ) {
        for (i in 1 .. length) {
            Text(
                text = "$i",
                fontFamily = Fonts.jetbrainsMono(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Thin,
                lineHeight = 36.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ComposeToggle(
    text: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .wrapContentSize()
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChanged(!checked) },
                role = Role.Checkbox
            )
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(4.dp)
        )
        DefaultPanelText(text = text)
    }
}
