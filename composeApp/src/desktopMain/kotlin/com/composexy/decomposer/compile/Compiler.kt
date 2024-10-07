@file:OptIn(ExperimentalCompilerApi::class)
@file:Suppress("DEPRECATION")

package com.composexy.decomposer.compile

import androidx.compose.compiler.plugins.kotlin.ComposeConfiguration
import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import com.composexy.decomposer.Config
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class JvmComposeCompiler : ComposeCompiler {
    private val pluginRegistrar = DecomposerPluginRegistrar()

    override fun decompose(
        composeSource: String,
        options: Map<Config, Boolean>
    ): DecomposeResult {
        val sourceFile = SourceFile.kotlin("ComposeSource.kt", composeSource)
        val result = KotlinCompilation().apply {
            sources = listOf(sourceFile)
            componentRegistrars = listOf(ComposePluginRegistrar().overrideWith(options))
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

internal fun ComposePluginRegistrar.overrideWith(options: Map<Config, Boolean>): ComponentRegistrar {
    return object : ComponentRegistrar {
        override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
        ) {
            configuration.put(
                ComposeConfiguration.LIVE_LITERALS_ENABLED_KEY, options[Config.LIVE_LITERALS] == true
            )
            configuration.put(
                ComposeConfiguration.LIVE_LITERALS_V2_ENABLED_KEY, options[Config.LIVE_LITERALS_V2] == true
            )
            configuration.put(
                ComposeConfiguration.GENERATE_FUNCTION_KEY_META_CLASSES_KEY, options[Config.GENERATE_FUNCTION_KEY_META] == true
            )
            configuration.put(
                ComposeConfiguration.SOURCE_INFORMATION_ENABLED_KEY, options[Config.SOURCE_INFORMATION] == true
            )
            configuration.put(
                ComposeConfiguration.INTRINSIC_REMEMBER_OPTIMIZATION_ENABLED_KEY, options[Config.INTRINSIC_REMEMBER] == true
            )
            configuration.put(
                ComposeConfiguration.NON_SKIPPING_GROUP_OPTIMIZATION_ENABLED_KEY, options[Config.NON_SKIPPING_GROUP_OPTIMIZATION] == true
            )
            configuration.put(
                ComposeConfiguration.STRONG_SKIPPING_ENABLED_KEY, options[Config.STRONG_SKIPPING] == true
            )
            configuration.put(
                ComposeConfiguration.TRACE_MARKERS_ENABLED_KEY, options[Config.TRACE_MARKER] == true
            )
            this@overrideWith.registerProjectComponents(project, configuration)
        }

        override val supportsK2: Boolean
            get() = this.supportsK2
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
