@file:OptIn(ExperimentalCompilerApi::class)

package com.composexy.decomposer.compile

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

@Suppress("DEPRECATION")
internal class JvmComposeCompiler : ComposeCompiler {
    private val pluginRegistrar = DecomposerPluginRegistrar()

    override fun decompose(composeSource: String): DecomposeResult {
        val sourceFile = SourceFile.kotlin("ComposeSource.kt", composeSource)
        val result = KotlinCompilation().apply {
            sources = listOf(sourceFile)
            componentRegistrars = listOf(ComposePluginRegistrar())
            compilerPluginRegistrars = listOf(pluginRegistrar)
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()
        return if (result.exitCode == ExitCode.OK) {
            val irFile = pluginRegistrar.getInterceptedIrModule().files[0]
            DecomposeResult(true, irFile.dumpSrc(false))
        } else {
            DecomposeResult(false, null)
        }
    }
}

internal class DecomposerPluginRegistrar() : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    private lateinit var irModule: IrModuleFragment

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration
    ) {
        IrGenerationExtension.registerExtension(IrModuleInterceptor { irModule = it })
    }

    fun getInterceptedIrModule() = irModule
}

internal class IrModuleInterceptor(private val interceptor: (IrModuleFragment) -> Unit) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        interceptor(moduleFragment)
    }
}
