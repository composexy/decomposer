package com.composexy.decomposer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.composexy.decomposer.compile.getComposeCompiler

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "decomposer",
    ) {
        App()
        val source = """
            class KClass {
                fun foo() {
                    val string = "a string"
                }
            }
        """
        val compiler = getComposeCompiler()
        val result = compiler.decompose(source)
        println(result.decomposedSource)
    }
}