package com.decomposer.ui

import androidx.compose.ui.text.AnnotatedString
import com.decomposer.ir.AnonymousInit
import com.decomposer.ir.Class
import com.decomposer.ir.Constructor
import com.decomposer.ir.Coordinate
import com.decomposer.ir.Declaration
import com.decomposer.ir.EnumEntry
import com.decomposer.ir.ErrorDeclaration
import com.decomposer.ir.Field
import com.decomposer.ir.Function
import com.decomposer.ir.KotlinFile
import com.decomposer.ir.LocalDelegatedProperty
import com.decomposer.ir.Property
import com.decomposer.ir.TopLevelTable
import com.decomposer.ir.TypeAlias
import com.decomposer.ir.TypeParameter
import com.decomposer.ir.ValueParameter
import com.decomposer.ir.Variable

data class SourceLocation(
    val sourceStartOffset: Int,
    val sourceEndOffset: Int
)

data class Description(
    val description: String
)

class IrVisualData(
    val annotatedString: AnnotatedString,
    val sourceLocationMetadata: Map<String, SourceLocation>,
    val descriptionMetadata: Map<String, Description>
)

class IrVisualBuilder(private val kotlinFile: KotlinFile) {
    private var used = false
    private val annotatedStringBuilder = AnnotatedString.Builder()
    private val sourceLocationMetadata = mutableMapOf<String, SourceLocation>()
    private val descriptionMetadata = mutableMapOf<String, Description>()
    private val sortedDeclarations = mutableMapOf<Declaration, TopLevelTable>()

    fun visualize(): IrVisualData {
        if (used) throw IllegalArgumentException("Reusing $this is not allowed!")
        visit(kotlinFile)
        return IrVisualData(
            annotatedString = annotatedStringBuilder.toAnnotatedString(),
            sourceLocationMetadata = mutableMapOf<String, SourceLocation>().also {
                it.putAll(sourceLocationMetadata)
                sourceLocationMetadata.clear()
            },
            descriptionMetadata = mutableMapOf<String, Description>().also {
                it.putAll(descriptionMetadata)
                descriptionMetadata.clear()
            }
        ).also {
            used = true
        }
    }

    private fun visit(file: KotlinFile) {
        val declarations = mutableMapOf<Declaration, TopLevelTable>()
        file.topLevelDeclarations?.let { table ->
            table.declarations.data.forEach { declaration ->
                declarations[declaration] = table
            }
        }
        file.topLevelClasses.forEach { table ->
            assert(table.declarations.data.size == 1)
            declarations[table.declarations.data[0]] = table
        }
        sortedDeclarations.putAll(
            declarations.toSortedMap(compareBy { it.range.startOffset })
        )
        sortedDeclarations.forEach {
            visit(it.key)
        }
    }

    private fun visit(declaration: Declaration) {

    }

    private val Declaration.range: Coordinate
        get() = when (this) {
            is Function -> this.base.base.coordinate
            is AnonymousInit -> this.base.coordinate
            is Class -> this.base.coordinate
            is Constructor -> this.base.base.coordinate
            is EnumEntry -> this.base.coordinate
            is ErrorDeclaration -> this.coordinate
            is Field -> this.base.coordinate
            is LocalDelegatedProperty -> this.base.coordinate
            is Property -> this.base.coordinate
            is TypeAlias -> this.base.coordinate
            is TypeParameter -> this.base.coordinate
            is ValueParameter -> this.base.coordinate
            is Variable -> this.base.coordinate
        }
}
