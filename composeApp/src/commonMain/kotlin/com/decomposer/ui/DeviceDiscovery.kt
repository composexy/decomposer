package com.decomposer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.server.AdbConnectResult
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource

@Composable
fun DeviceDiscovery(
    modifier: Modifier,
    versions: Versions,
    adbState: AdbConnectResult,
    onConnect: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = modifier.wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.heightIn(80.dp, 120.dp).widthIn(80.dp, 120.dp),
            painter = painterResource(Res.drawable.ic_launcher),
            contentDescription = "Launcher logo",
            contentScale = ContentScale.Fit
        )
        DefaultText(
            text = """
                Version: ${versions.DECOMPOSER_VERSION}
                Target compose runtime: ${versions.TARGET_COMPOSE_RUNTIME_VERSION}
                Target kotlin: ${versions.TARGET_KOTLIN_VERSION}
            """.trimIndent(),
        )
        when (adbState) {
            is AdbConnectResult.Failure -> {
                Spacer(modifier = Modifier.height(32.dp))
                DefaultText(
                    text = """
                        Connection failed:
                        ${adbState.errorMessage}
                    """.trimIndent(),
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        onConnect()
                    }
                ) {
                    DefaultText(
                        text = "Retry"
                    )
                }
            }
            AdbConnectResult.Idle -> {
                Spacer(modifier = Modifier.height(32.dp))
                DefaultText(
                    text = """
                        Please connect one and only one android device to this PC then click "Connect".
                        The server runs on port 9801. Or you can run "adb reverse tcp:9801 tcp:9801"
                        manually then click the "Skip" button.
                        If you cannot make this port available, set DECOMPOSER_SERVER_PORT to override
                        the port number.
                    """.trimIndent()
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { onConnect() }) {
                    DefaultText(text = "Connect")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { onSkip() }) {
                    DefaultText(text = "Skip")
                }
            }
            AdbConnectResult.Success -> {
                Spacer(modifier = Modifier.height(32.dp))
                DefaultText(
                    text = buildString {
                        append("Connected!")
                    }
                )
            }
            AdbConnectResult.Skipped -> {
                Spacer(modifier = Modifier.height(32.dp))
                DefaultText(
                    text = buildString {
                        append("Skipped!")
                    }
                )
            }
        }
    }
}

@Composable
private fun DefaultText(text: String) {
    val fontSize = AppSetting.fontSize
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontFamily = Fonts.jetbrainsMono(),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Light,
        lineHeight = (fontSize * 1.5f).sp
    )
}
