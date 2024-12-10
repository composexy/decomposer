package com.decomposer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.decomposer.ir.IrProcessor
import com.decomposer.ir.isEmpty
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.server.Session
import com.decomposer.server.SessionState
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.expand_all
import decomposer.composeapp.generated.resources.fold_all
import decomposer.composeapp.generated.resources.ic_launcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.nio.file.Paths
import kotlin.math.absoluteValue

@Composable
fun Panels(
    modifier: Modifier,
    sessionState: SessionState,
    panelsState: PanelsState = rememberPanelsState()
) {
    when (sessionState) {
        is SessionState.Disconnected -> {
            Box(modifier = Modifier.fillMaxSize()) {
                DefaultPanelText(
                    text = """
                        Session ${sessionState.sessionId} was disconnected!
                    """.trimIndent(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        SessionState.Idle -> {
            Box(modifier = Modifier.fillMaxSize()) {
                DefaultPanelText(
                    text = """
                        Waiting for server to boot!
                    """.trimIndent(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is SessionState.Started -> {
            Box(modifier = Modifier.fillMaxSize()) {
                DefaultPanelText(
                    text = """
                        Server boot up at port ${sessionState.port}!
                    """.trimIndent(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is SessionState.Connected -> {
            var navigationContext: NavigationContext? by remember {
                mutableStateOf(null)
            }

            var irProcessor: IrProcessor by remember {
                mutableStateOf(IrProcessor())
            }

            var projectSnapshot: ProjectSnapshot by remember {
                mutableStateOf(ProjectSnapshot(emptySet(), emptyMap()))
            }

            var highlight: Pair<Int, Int>? by remember {
                mutableStateOf(null)
            }

            val coroutineScope = rememberCoroutineScope { Dispatchers.Default }

            Column(modifier = modifier) {
                ToolBar(
                    modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                    panelsState = panelsState
                )
                HorizontalSplitter()
                Box(modifier = modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (panelsState.fileTreeVisible) {
                            FileTreePanel(
                                modifier = Modifier.weight(0.16f),
                                projectSnapshot = projectSnapshot,
                                onClickFileEntry = { panelsState.selectedIrFilePath = it }
                            )
                        }
                        if (panelsState.irViewerVisible) {
                            if (panelsState.fileTreeVisible) {
                                VerticalSplitter()
                            }
                            IrPanel(
                                modifier = Modifier.weight(0.42f).clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    highlight = null
                                },
                                session = sessionState.session,
                                irProcessor = irProcessor,
                                filePath = panelsState.selectedIrFilePath,
                                highlight = highlight,
                                onShowPopup = { panelsState.currentPopup = it },
                            )
                        }
                        if (panelsState.compositionViewerVisible) {
                            if (panelsState.irViewerVisible || panelsState.fileTreeVisible) {
                                VerticalSplitter()
                            }
                            CompositionPanel(
                                modifier = Modifier.weight(0.42f),
                                session = sessionState.session,
                                navigationContext = navigationContext,
                                onShowPopup = { panelsState.currentPopup = it },
                                onShowWindow = { panelsState.addWindow(it) },
                                onCodeNavigate = { filePath, startOffset, endOffset ->
                                    panelsState.fileTreeVisible = false
                                    panelsState.irViewerVisible = true
                                    panelsState.currentPopup = null
                                    highlight = Pair(startOffset, endOffset)
                                    panelsState.selectedIrFilePath = filePath
                                }
                            )
                        }
                    }
                    panelsState.currentPopup?.let {
                        Popup(
                            alignment = Alignment.Center,
                            onDismissRequest = { panelsState.currentPopup = null }
                        ) {
                            Surface(
                                modifier = Modifier.size(width = 1280.dp, height = 720.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                                color = Color.DarkGray,
                                elevation = 4.dp
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) { it() }
                            }
                        }
                    }
                    panelsState.currentWindows.forEach { window ->
                        val title = window.first
                        val content = window.second
                        Window(
                            onCloseRequest = { panelsState.removeWindow(window) },
                            title = title,
                            state = WindowState(width = 1920.dp, height = 1080.dp),
                            icon = painterResource(Res.drawable.ic_launcher)
                        ) {
                            DecomposerTheme {
                                Surface(modifier = Modifier.fillMaxSize()) {
                                    content()
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(sessionState.session) {
                irProcessor = IrProcessor()
                projectSnapshot = sessionState.session.getProjectSnapshot()
                coroutineScope.launch {
                    navigationContext = buildNavigationContext(
                        sessionState.session,
                        projectSnapshot,
                        irProcessor
                    )
                }
            }
        }
    }

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Disconnected) {
            panelsState.clear()
        }
    }
}

@Composable
fun VerticalSplitter(
    color: Color = MaterialTheme.colors.background
) {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}

@Composable
fun HorizontalSplitter(
    color: Color = MaterialTheme.colors.background
) {
    Box(
        Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(color)
    )
}

@Composable
fun rememberPanelsState(): PanelsState {
    return remember { PanelsState() }
}

@Composable
fun DefaultPanelText(
    text: String,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        fontFamily = Fonts.jetbrainsMono(),
        fontSize = 24.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 36.sp,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines
    )
}

@Composable
fun TreeExpander(
    onExpandAll: () -> Unit,
    onFoldAll: () -> Unit
) {
    Row(
        modifier = Modifier.wrapContentSize()
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.expand_all),
                contentDescription = "Expand all",
                modifier = Modifier.size(32.dp)
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onExpandAll() }
            )
            Image(
                painter = painterResource(Res.drawable.fold_all),
                contentDescription = "Fold all",
                modifier = Modifier.size(32.dp)
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onFoldAll() }
            )
        }
    }
}

private fun buildNavigationContext(
    session: Session,
    projectSnapshot: ProjectSnapshot,
    irProcessor: IrProcessor
): NavigationContext {
    val packagesByPath = projectSnapshot.packagesByPath
    val pathsByPackageHash = packagesByPath.map {
        val packageName = it.value
        val path = it.key
        val hash = packageName.fold(0) { hash, char ->
            hash * 31 + char.code
        }.absoluteValue.toString(36)
        val fileName = Paths.get(path).fileName.toString()
        PackageHashWithFileName(hash, fileName)  to path
    }.toMap()
    return NavigationContext(
        pathsByPackageHash = pathsByPackageHash,
        irProcessor = irProcessor,
        session = session
    )
}

data class PackageHashWithFileName(
    val packageHash: String,
    val fileName: String
)

class NavigationContext(
    private val pathsByPackageHash: Map<PackageHashWithFileName, String>,
    private val irProcessor: IrProcessor,
    private val session: Session
) {
    fun canNavigate(packageHashWithFileName: PackageHashWithFileName): Boolean {
        return pathsByPackageHash.containsKey(packageHashWithFileName)
    }

    fun filePath(packageHashWithFileName: PackageHashWithFileName): String? {
        return pathsByPackageHash[packageHashWithFileName]
    }

    suspend fun getCoordinates(
        invocationLocations: List<Int>,
        packageHashWithFileName: PackageHashWithFileName
    ): Pair<Int, Int>? {
        val filePath = pathsByPackageHash[packageHashWithFileName] ?: return null
        if (irProcessor.originalFile(filePath).isEmpty) {
            val virtualFileIr = session.getVirtualFileIr(filePath)
            irProcessor.processVirtualFileIr(virtualFileIr)
        }
        // composed or original should yield same result
        val kotlinFile = irProcessor.originalFile(filePath)
        val functions = kotlinFile.functions + kotlinFile.lambdas.map { it.function }
        val matches = functions.filter {
            invocationLocations.all { location ->
                val startOffset = it.base.base.coordinate.startOffset
                val endOffset = it.base.base.coordinate.endOffset
                location in startOffset..endOffset
            }
        }
        val best = matches.maxByOrNull { it.base.base.coordinate.startOffset }
        return best?.let {
            Pair(
                it.base.base.coordinate.startOffset,
                it.base.base.coordinate.endOffset
            )
        }
    }
}

class PanelsState {
    var fileTreeVisible by mutableStateOf(true)
    var irViewerVisible by mutableStateOf(true)
    var compositionViewerVisible by mutableStateOf(false)
    var selectedIrFilePath: String? by mutableStateOf(null)
    var currentPopup: (@Composable () -> Unit)? by mutableStateOf(null)
    var currentWindows: MutableList<Pair<String, (@Composable () -> Unit)>> = mutableStateListOf()

    fun clear() {
        fileTreeVisible = true
        irViewerVisible = true
        compositionViewerVisible = false
        selectedIrFilePath = null
        currentPopup = null
        currentWindows = mutableStateListOf()
    }

    fun addWindow(window: Pair<String, (@Composable () -> Unit)>) {
        if (currentWindows.size >= 3) {
            currentWindows.removeFirst()
        }
        currentWindows.add(window)
    }

    fun removeWindow(window: Pair<String, (@Composable () -> Unit)>) {
        currentWindows.remove(window)
    }
}
