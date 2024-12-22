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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
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
            var navigationContext: NavigationContext? by remember { mutableStateOf(null) }
            var irProcessor: IrProcessor by remember { mutableStateOf(IrProcessor()) }
            var loadingProjectSnapshot by remember { mutableStateOf(false) }
            var highlight: Pair<Int, Int>? by remember { mutableStateOf(null) }
            val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
            var projectSnapshot: ProjectSnapshot by remember {
                mutableStateOf(ProjectSnapshot(emptySet(), emptyMap()))
            }

            Column(modifier = modifier) {
                ToolBar(
                    modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                    panelsState = panelsState
                )
                HorizontalSplitter()
                Box(modifier = modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (panelsState.fileTreeVisible) {
                            FileTreePanel(
                                modifier = Modifier.weight(0.16f),
                                projectSnapshot = projectSnapshot,
                                loading = loadingProjectSnapshot,
                                onClickFileEntry = {
                                    panelsState.selectedIrFilePath = it
                                    panelsState.irViewerVisible = true
                                }
                            )
                        }
                        if (panelsState.irViewerVisible) {
                            if (panelsState.fileTreeVisible) {
                                VerticalSplitter()
                            }
                            IrPanel(
                                modifier = Modifier.weight(0.42f)
                                    .run {
                                        if (highlight != null) {
                                            this.clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                highlight = null
                                            }
                                        } else {
                                            this
                                        }
                                    },
                                session = sessionState.session,
                                irProcessor = irProcessor,
                                filePath = panelsState.selectedIrFilePath,
                                projectSnapshot = projectSnapshot,
                                highlight = highlight,
                                onShowPopup = { panelsState.currentPopup = it },
                                onShowWindow = { panelsState.addWindow(it) }
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
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                                color = Color.DarkGray,
                                elevation = 4.dp
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) { it() }
                            }
                        }
                    }
                    panelsState.currentWindows.forEach { window ->
                        val title = window.first
                        val content = window.second
                        key(window) {
                            Window(
                                onCloseRequest = { panelsState.removeWindow(window) },
                                title = title,
                                state = rememberWindowState(
                                    size = DpSize(1280.dp, 720.dp)
                                ),
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
            }

            LaunchedEffect(sessionState.session) {
                loadingProjectSnapshot = true
                irProcessor = IrProcessor()
                projectSnapshot = sessionState.session.getProjectSnapshot()
                coroutineScope.launch {
                    navigationContext = buildNavigationContext(
                        sessionState.session,
                        projectSnapshot,
                        irProcessor
                    )
                }
                loadingProjectSnapshot = false
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
    textAlign: TextAlign = TextAlign.Center,
    clickable: Boolean = false,
    onClick: () -> Unit = { }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val fontSize = AppSetting.fontSize
    Text(
        modifier = modifier.run {
            if (clickable) {
                this.hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onClick() }
            } else {
                this
            }
        },
        text = text,
        textAlign = textAlign,
        fontFamily = Fonts.jetbrainsMono(),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Light,
        lineHeight = (fontSize * 1.5).sp,
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
        val size = with(LocalDensity.current) {
            (LocalFontSize.current * 1.25).sp.toDp()
        }
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            Modifier
                .wrapContentSize()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.expand_all),
                contentDescription = "Expand all",
                modifier = Modifier.size(size)
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onExpandAll() }
            )
            Image(
                painter = painterResource(Res.drawable.fold_all),
                contentDescription = "Fold all",
                modifier = Modifier.size(size)
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
        if (currentWindows.size >= 5) {
            currentWindows.removeFirst()
        }
        currentWindows.add(window)
    }

    fun removeWindow(window: Pair<String, (@Composable () -> Unit)>) {
        currentWindows.remove(window)
    }
}
