package com.decomposer.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class DecomposerCommandLineProcessor : CommandLineProcessor {

    internal companion object {
        private const val DESCRIPTION_BOOLEAN = "<true|false>"

        val OPTION_ENABLED = CliOption(
            optionName = "enabled",
            valueDescription = DESCRIPTION_BOOLEAN,
            description = KEY_ENABLED.toString(),
            required = false,
            allowMultipleOccurrences = false
        )

        val OPTION_IR_STORAGE_ENABLED = CliOption(
            optionName = "irStorageEnabled",
            valueDescription = DESCRIPTION_BOOLEAN,
            description = KEY_IR_STORAGE_ENABLED.toString(),
            required = false,
            allowMultipleOccurrences = false
        )

        val OPTION_SOURCE_STORAGE_ENABLED = CliOption(
            optionName = "sourceStorageEnabled",
            valueDescription = DESCRIPTION_BOOLEAN,
            description = KEY_SOURCE_STORAGE_ENABLED.toString(),
            required = false,
            allowMultipleOccurrences = false
        )
    }

    override val pluginId = "com.decomposer.compiler"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            OPTION_ENABLED,
            OPTION_IR_STORAGE_ENABLED,
            OPTION_SOURCE_STORAGE_ENABLED
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option.optionName) {
            OPTION_ENABLED.optionName -> {
                configuration.put(KEY_ENABLED, value.toBoolean())
            }
            OPTION_IR_STORAGE_ENABLED.optionName -> {
                configuration.put(KEY_IR_STORAGE_ENABLED, value.toBoolean())
            }
            OPTION_SOURCE_STORAGE_ENABLED.optionName ->  {
                configuration.put(KEY_SOURCE_STORAGE_ENABLED, value.toBoolean())
            }
            else -> error("Unknown decomposer plugin option: ${option.optionName}")
        }
    }
}

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>(
    "Disable all decomposer features if false."
)

internal val KEY_IR_STORAGE_ENABLED = CompilerConfigurationKey<Boolean>(
    "Storage all IR files of current compilation after compose compiler plugin is run."
)

internal val KEY_SOURCE_STORAGE_ENABLED = CompilerConfigurationKey<Boolean>(
    "Storage all source files of current compilation."
)
