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

        val OPTION_POST_COMPOSE_IR_STORAGE_ENABLED = CliOption(
            optionName = "postComposeIrStorageEnabled",
            valueDescription = DESCRIPTION_BOOLEAN,
            description = KEY_POST_COMPOSE_IR_STORAGE_ENABLED.toString(),
            required = false,
            allowMultipleOccurrences = false
        )

        val OPTION_PRE_COMPOSE_IR_STORAGE_ENABLED = CliOption(
            optionName = "preComposeIrStorageEnabled",
            valueDescription = DESCRIPTION_BOOLEAN,
            description = KEY_PRE_COMPOSE_IR_STORAGE_ENABLED.toString(),
            required = false,
            allowMultipleOccurrences = false
        )
    }

    override val pluginId = "com.decomposer.compiler"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            OPTION_ENABLED,
            OPTION_POST_COMPOSE_IR_STORAGE_ENABLED,
            OPTION_PRE_COMPOSE_IR_STORAGE_ENABLED
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
            OPTION_POST_COMPOSE_IR_STORAGE_ENABLED.optionName -> {
                configuration.put(KEY_POST_COMPOSE_IR_STORAGE_ENABLED, value.toBoolean())
            }
            OPTION_PRE_COMPOSE_IR_STORAGE_ENABLED.optionName ->  {
                configuration.put(KEY_PRE_COMPOSE_IR_STORAGE_ENABLED, value.toBoolean())
            }
            else -> error("Unknown decomposer plugin option: ${option.optionName}")
        }
    }
}

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>(
    "Disable all decomposer features if false."
)

internal val KEY_POST_COMPOSE_IR_STORAGE_ENABLED = CompilerConfigurationKey<Boolean>(
    "Storage all IR files of current compilation after compose compiler plugin is run."
)

internal val KEY_PRE_COMPOSE_IR_STORAGE_ENABLED = CompilerConfigurationKey<Boolean>(
    "Storage all IR files of current compilation before compose compiler plugin is run."
)
