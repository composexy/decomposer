package com.decomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.ir.IrProcessor
import com.decomposer.server.Session

@Composable
fun IrPanel(
    modifier: Modifier = Modifier,
    session: Session,
    filePath: String?
) {
    val irProcessor: IrProcessor = remember {
        IrProcessor()
    }

    var compose by remember {
        mutableStateOf(true)
    }

    var irContent by remember {
        mutableStateOf<AnnotatedString?>(null)
    }

    Box(
        modifier = modifier
    ) {
        val ir = irContent
        if (ir == null) {
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
                        checked = compose,
                        onCheckedChanged = {
                            compose = !compose
                        }
                    )
                }
                CodeContent(ir)
            }
        }
    }

    LaunchedEffect(filePath, compose) {
        if (filePath != null) {
            val virtualFileIr = session.getVirtualFileIr(filePath)
            irProcessor.processVirtualFileIr(virtualFileIr)
            val kotlinFile = if (compose) {
                irProcessor.composedFile(filePath)
            } else {
                irProcessor.originalFile(filePath)
            }
            val visualData = IrVisualBuilder(kotlinFile).visualize()
            irContent = visualData.annotatedString
        }
    }
}

@Composable
fun CodeContent(
    ir: AnnotatedString
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState),
        ) {
            LineNumbers(ir.lines().size)
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    text = ir,
                    fontFamily = Fonts.jetbrainsMono(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 36.sp
                )
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
        DefaultPanelText(
            text = "Compose",
        )
    }
}