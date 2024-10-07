package com.composexy.decomposer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composexy.decomposer.compile.getComposeCompiler
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun Application() {
    MaterialTheme {
        var appState by remember {
            mutableStateOf(AppState())
        }

        Surface {
            Box(
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                Row {
                    SidePanel(
                        configs = appState.compilerConfigState,
                        onRunClick = {
                            val compiler = getComposeCompiler()
                            val source = appState.sourceState
                            source?.let {
                                val result = compiler.decompose(it, appState.compilerConfigState)
                                appState = appState.copy(
                                    resultState = if (result.isSuccessful) {
                                        result.decomposedSource
                                    } else {
                                        "Compiler error"
                                    }
                                )
                            }
                        },
                        onConfigChange = { config, checked ->
                            appState = appState.copy(
                                compilerConfigState = appState.compilerConfigState.toMutableMap().apply {
                                    this[config] = checked
                                }
                            )
                        }
                    )
                    SourceContainer(
                        modifier = Modifier.weight(1.0f),
                        source = appState.sourceState ?: "",
                        onSourceChanged = { appState = appState.copy(sourceState = it) }
                    )
                    ResultContainer(
                        modifier = Modifier.weight(1.0f),
                        result = appState.resultState ?: ""
                    )
                }
            }
        }
    }
}

@Composable
fun SourceContainer(
    modifier: Modifier = Modifier,
    source: String,
    onSourceChanged: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier.fillMaxHeight(),
        value = source,
        onValueChange = { value: String -> onSourceChanged(value) },
        textStyle = TextStyle(fontSize = 32.sp)
    )
}

@Composable
fun ResultContainer(
    modifier: Modifier = Modifier,
    result: String
) {
    OutlinedTextField(
        modifier = modifier.fillMaxHeight(),
        value = result,
        textStyle = TextStyle(fontSize = 32.sp),
        onValueChange = {},
        readOnly = true,
    )
}

@Composable
fun SidePanel(
    configs: Map<Config, Boolean>,
    onRunClick: () -> Unit,
    onConfigChange: (Config, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max).fillMaxHeight()
    ) {
        IconButton(
            onClick = { onRunClick() },
        ) {
            Icon(
                modifier = Modifier.size(72.dp, 72.dp),
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Run",
                tint = Color.Green
            )
        }
        configs.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = entry.value,
                    onCheckedChange = { onConfigChange(entry.key, it) }
                )
                Text(
                    fontSize = 32.sp,
                    text = entry.key.label
                )
            }
        }
    }
}

@Immutable
data class AppState(
    val compilerConfigState: Map<Config, Boolean> = defaultCompilerState(),
    val sourceState: String? = defaultSource(),
    val resultState: String? = null
)

enum class Config(val label: String) {
    LIVE_LITERALS("live literals"),
    LIVE_LITERALS_V2("live literals v2"),
    GENERATE_FUNCTION_KEY_META("generate function key meta"),
    SOURCE_INFORMATION("source information"),
    INTRINSIC_REMEMBER("intrinsic remember"),
    NON_SKIPPING_GROUP_OPTIMIZATION("non skipping group optimization"),
    STRONG_SKIPPING("strong skipping"),
    TRACE_MARKER("trace marker")
}

fun defaultCompilerState(): Map<Config, Boolean> {
    return mapOf(
        Config.LIVE_LITERALS to false,
        Config.LIVE_LITERALS_V2 to false,
        Config.GENERATE_FUNCTION_KEY_META to false,
        Config.SOURCE_INFORMATION to false,
        Config.INTRINSIC_REMEMBER to true,
        Config.NON_SKIPPING_GROUP_OPTIMIZATION to false,
        Config.STRONG_SKIPPING to true,
        Config.TRACE_MARKER to false
    )
}

fun defaultSource(): String {
    return """
        import androidx.compose.runtime.*

        @Immutable class Foo

        @Composable
        fun A(vararg values: Foo) {
            print(values)
        }

        @Composable
        fun B(vararg values: Int) {
            print(values)
        }
    """
}
