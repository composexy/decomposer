package com.decomposer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.decomposer.ir.AccessorSignature
import com.decomposer.ir.AnonymousInit
import com.decomposer.ir.Body
import com.decomposer.ir.Class
import com.decomposer.ir.CommonSignature
import com.decomposer.ir.CompositeSignature
import com.decomposer.ir.Constructor
import com.decomposer.ir.ConstructorCall
import com.decomposer.ir.Coordinate
import com.decomposer.ir.Declaration
import com.decomposer.ir.EmptySignature
import com.decomposer.ir.EnumEntry
import com.decomposer.ir.ErrorDeclaration
import com.decomposer.ir.Expression
import com.decomposer.ir.ExpressionBody
import com.decomposer.ir.Field
import com.decomposer.ir.FileLocalSignature
import com.decomposer.ir.FileSignature
import com.decomposer.ir.Function
import com.decomposer.ir.FunctionExpression
import com.decomposer.ir.KotlinFile
import com.decomposer.ir.LocalDelegatedProperty
import com.decomposer.ir.LocalSignature
import com.decomposer.ir.LocalVarFlags
import com.decomposer.ir.Modality
import com.decomposer.ir.Operation
import com.decomposer.ir.Property
import com.decomposer.ir.PropertyFlags
import com.decomposer.ir.ScopedLocalSignature
import com.decomposer.ir.Signature
import com.decomposer.ir.SimpleType
import com.decomposer.ir.StarProjection
import com.decomposer.ir.Symbol
import com.decomposer.ir.TopLevelTable
import com.decomposer.ir.TypeAlias
import com.decomposer.ir.TypeAliasFlags
import com.decomposer.ir.TypeArgument
import com.decomposer.ir.TypeParameter
import com.decomposer.ir.TypeProjection
import com.decomposer.ir.ValueParameter
import com.decomposer.ir.ValueParameterFlags
import com.decomposer.ir.Variable
import com.decomposer.ir.Variance
import com.decomposer.ir.Visibility
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.max

enum class Keyword(val visual: String) {
    LATEINIT("lateinit"),
    CONST("const"),
    VAL("val"),
    VAR("var"),
    CROSSINLINE("crossline"),
    NOINLINE("noinline"),
    ACTUAL("actual"),
    PRIVATE("private"),
    INTERNAL("internal"),
    PROTECTED("protected"),
    IN("in"),
    OUT("out"),
    TYPEALIAS("typealias"),
    SEALED("sealed"),
    OPEN("open"),
    ABSTRACT("abstract"),
    EXPECT("expect"),
    GET("get"),
    SET("set"),
}

sealed interface AnnotationData

@Serializable
data class SourceLocation(
    val sourceStartOffset: Int,
    val sourceEndOffset: Int
) : AnnotationData

@Serializable
data class Description(
    val description: String
) : AnnotationData

class IrVisualData(val annotatedString: AnnotatedString)

class IrVisualBuilder(
    private val kotlinFile: KotlinFile,
    private val indentSize: Int = 2,
    private val theme: Theme = Theme.dark
) {
    private var used = false
    private val annotatedStringBuilder = AnnotatedString.Builder()
    private val sortedDeclarations = mutableMapOf<Declaration, TopLevelTable>()
    private val indentUnit = buildString {
        for (i in 0 until max(2, indentSize)) append(' ')
    }
    private var indentLevel = 0
    private var currentTable: TopLevelTable? = null

    fun visualize(): IrVisualData {
        if (used) throw IllegalArgumentException("Reusing $this is not allowed!")
        visualize(kotlinFile)
        return IrVisualData(annotatedString = annotatedStringBuilder.toAnnotatedString()).also {
            used = true
        }
    }

    private fun visualize(file: KotlinFile) {
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
            withTable(it.value) { visualize(it.key) }
        }
    }

    private fun visualize(declaration: Declaration) {
        increaseIndent {
            newLine()
            newLine()
            when (declaration) {
                is Function -> visualizeFunction(declaration)
                is AnonymousInit -> visualizeAnonymousInit(declaration)
                is Class -> visualizeClass(declaration)
                is Constructor -> visualizeConstructor(declaration)
                is EnumEntry -> visualizeEnumEntry(declaration)
                is ErrorDeclaration -> visualizeErrorDeclaration(declaration)
                is Field -> visualizeField(declaration)
                is LocalDelegatedProperty -> visualizeLocalDelegatedProperty(declaration)
                is Property -> visualizeProperty(declaration)
                is TypeAlias -> visualizeTypeAlias(declaration)
                is TypeParameter -> visualizeTypeParameter(declaration)
                is ValueParameter -> visualizeValueParameter(declaration)
                is Variable -> visualizeVariable(declaration)
            }
        }
    }

    private fun visualizeVariable(declaration: Variable) {
        val flags = declaration.base.flags as? LocalVarFlags
        flags?.let {
            if (it.isLateinit) {
                keyword(Keyword.LATEINIT)
                space()
            }
            if (it.isConst) {
                keyword(Keyword.CONST)
                space()
            }
            if (it.isVar) {
                keyword(Keyword.VAR)
            } else {
                keyword(Keyword.VAL)
            }
            space()
        }

        val name = strings(declaration.nameIndex)
        append(name)
        val type = types(declaration.typeIndex)
        append(':')
        space()
        visualizeType(type)

        declaration.initializer?.let {
            appendSpaced('=')
            visualizeExpression(it)
        }
    }

    private fun visualizeType(type: SimpleType) {
        visualizeAnnotations(type.annotations, multiLine = false)
        val name = type.symbol.declarationName
        append(name)
        visualizeTypeArguments(type.arguments)
    }

    private fun visualizeTypeArguments(arguments: List<TypeArgument>) {
        if (arguments.isNotEmpty()) {
            append('<')
            arguments.forEachIndexed { index, argument ->
                visualizeTypeArgument(argument)
                if (index != arguments.size - 1) {
                    append(',')
                    space()
                }
            }
            append('>')
        }
    }

    private fun visualizeTypeArgument(argument: TypeArgument) {
        when (argument) {
            StarProjection -> append('*')
            is TypeProjection -> {
                when (argument.variance) {
                    Variance.INVARIANT -> Unit
                    Variance.IN_VARIANCE -> {
                        keyword(Keyword.IN)
                        space()
                    }
                    Variance.OUT_VARIANCE -> {
                        keyword(Keyword.OUT)
                        space()
                    }
                }
                val type = types(argument.typeIndex)
                visualizeType(type)
            }
        }
    }

    private fun visualizeTypeAlias(declaration: TypeAlias) {
        val flags = declaration.base.flags as? TypeAliasFlags
        flags?.let {
            val visibility = flags.visibility
            visualizeVisibility(visibility)
            if (it.isActual) {
                keyword(Keyword.ACTUAL)
                space()
            }
        }
        keyword(Keyword.TYPEALIAS)
        space()
        val name = strings(declaration.nameIndex)
        append(name)
        val type = types(declaration.typeIndex)
        appendSpaced(':')
        visualizeType(type)
    }

    private fun visualizeModality(modality: Modality) {
        when (modality) {
            Modality.FINAL -> Unit
            Modality.SEALED -> {
                keyword(Keyword.SEALED)
                space()
            }
            Modality.OPEN -> {
                keyword(Keyword.OPEN)
                space()
            }
            Modality.ABSTRACT -> {
                keyword(Keyword.ABSTRACT)
                space()
            }
        }
    }

    private fun visualizeVisibility(visibility: Visibility) {
        when (visibility) {
            Visibility.PRIVATE,
            Visibility.PRIVATE_TO_THIS -> {
                keyword(Keyword.PRIVATE)
                space()
            }
            Visibility.PROTECTED -> {
                keyword(Keyword.PROTECTED)
                space()
            }
            Visibility.INTERNAL -> {
                keyword(Keyword.INTERNAL)
                space()
            }
            Visibility.PUBLIC,
            Visibility.LOCAL,
            Visibility.INHERITED,
            Visibility.INVISIBLE_FAKE,
            Visibility.UNKNOWN -> Unit
        }
    }

    private fun visualizeProperty(declaration: Property) {
        visualizeAnnotations(declaration.base.annotations)
        val flags = declaration.base.flags as? PropertyFlags
        flags?.let {
            visualizeVisibility(it.visibility)
            visualizeModality(it.modality)
            if (it.isExpect) {
                keyword(Keyword.EXPECT)
                space()
            }
            if (it.isLateinit) {
                keyword(Keyword.LATEINIT)
                space()
            }
            if (it.isConst) {
                keyword(Keyword.CONST)
                space()
            }
            if (it.isVar) {
                keyword(Keyword.VAR)
            } else {
                keyword(Keyword.VAL)
            }
            space()
        }
        val name = strings(declaration.nameIndex)
        append(name)
        val typeIndex = declaration.backingField?.typeIndex ?: declaration.getter?.base?.typeIndex
        val type = types(typeIndex!!)
        appendSpaced(':')
        visualizeType(type)
        declaration.backingField?.let {
            appendSpaced('=')
            visualizeField(it)
        }
        increaseIndent {
            declaration.getter?.let { getter ->
                newLine()
                visualizeAnnotations(getter.base.base.annotations)
                keyword(Keyword.GET)
                append("()")
                space()
                getter.base.bodyIndex?.let {
                    val body = bodies(it)
                    visualizeBody(body)
                }
            }
            declaration.setter?.let { setter ->
                newLine()
                visualizeAnnotations(setter.base.base.annotations)
                keyword(Keyword.SET)
                append("()")
                space()
                setter.base.bodyIndex?.let {
                    val body = bodies(it)
                    visualizeBody(body)
                }
            }
        }
    }

    private fun visualizeBody(body: Body) {

    }

    private fun visualizeLocalDelegatedProperty(declaration: LocalDelegatedProperty) {
        TODO("Not yet implemented")
    }

    private fun visualizeField(declaration: Field) {
        TODO("Not yet implemented")
    }

    private fun visualizeErrorDeclaration(declaration: ErrorDeclaration) {
        TODO("Not yet implemented")
    }

    private fun visualizeEnumEntry(declaration: EnumEntry) {
        TODO("Not yet implemented")
    }

    private fun visualizeConstructor(declaration: Constructor) {
        TODO("Not yet implemented")
    }

    private fun visualizeClass(declaration: Class) {
        visualizeAnnotations(declaration.base.annotations)
    }

    private fun visualizeValueParameter(declaration: ValueParameter) {
        val flags = declaration.base.flags as? ValueParameterFlags
        flags?.let {
            if (it.isCrossInline) {
                keyword(Keyword.CROSSINLINE)
                space()
            }
            if (it.isNoInline) {
                keyword(Keyword.NOINLINE)
                space()
            }
        }

        visualizeAnnotations(declaration.base.annotations, multiLine = false)
        val name = strings(declaration.nameIndex)
        append(name)
        append(':')
        space()
        val type = types(declaration.typeIndex)
        visualizeType(type)
        declaration.defaultValueIndex?.let {
            val expressionBody = bodies(it) as? ExpressionBody ?: return@let
            appendSpaced('=')
            visualizeExpressionBody(expressionBody)
        }
    }

    private fun visualizeExpressionBody(expressionBody: ExpressionBody) {
        visualizeExpression(expressionBody.expression)
    }

    private fun visualizeTypeParameter(declaration: TypeParameter) {
        val name = strings(declaration.nameIndex)
        append(name)
        val superTypes = declaration.superTypeIndexes.map { types(it) }
        if (superTypes.isNotEmpty()) {
            appendSpaced(':')
            superTypes.forEachIndexed { index, type ->
                visualizeType(type)
                if (index != superTypes.size - 1) {
                    space()
                }
            }
        }
    }

    private fun visualizeAnnotations(
        annotations: List<ConstructorCall>,
        multiLine: Boolean = true
    ) {
        val filtered = annotations.filter {
            !isDecomposerAnnotation(it)
        }
        filtered.forEach { annotation ->
            visualizeAnnotation(annotation)
            if (multiLine) {
                newLine()
            } else {
                space()
            }
        }
    }

    private fun visualizeAnnotation(
        annotation: ConstructorCall,
        multiLine: Boolean = true
    ) = withAnnotation {
        append('@')
        visualizeConstructorCall(annotation, isAnnotation = true, multiLine = multiLine)
    }

    private fun visualizeConstructorCall(
        call: ConstructorCall,
        isAnnotation: Boolean = false,
        multiLine: Boolean = false
    ) {
        call.memberAccess.dispatchReceiver?.let {
            visualizeExpression(it)
            append('.')
        }
        append(call.symbol.declarationName)
        val shouldVisualize = !isAnnotation || call.memberAccess.valueArguments.any { it != null }
        if (shouldVisualize) {
            visualizeArguments(call.memberAccess.valueArguments, multiLine)
        }
    }

    private fun visualizeArguments(valueArguments: List<Expression?>, multiLine: Boolean = true) {
        val trailingLambda = valueArguments.last()?.let {
            it.operation as? FunctionExpression
        }
        val normalArguments = if (trailingLambda != null) {
            valueArguments.dropLast(1)
        } else valueArguments

        if (normalArguments.isNotEmpty()) {
            append('(')
            withSimple {
                if (multiLine) {
                    increaseIndent {
                        normalArguments.forEachIndexed { index, expression ->
                            newLine()
                            if (expression != null) {
                                visualizeOperation(expression.operation)
                                if (index != normalArguments.size - 1) {
                                    append(',')
                                }
                            }
                        }
                    }
                } else {
                    normalArguments.forEachIndexed { index, expression ->
                        if (expression != null) {
                            visualizeOperation(expression.operation)
                            if (index != normalArguments.size - 1) {
                                append(',')
                                space()
                            }
                        }
                    }
                }
            }
            if (multiLine) newLine()
            append(')')
        }
        if (trailingLambda != null) {
            space()
            visualizeFunctionExpression(trailingLambda)
        }
    }

    private fun visualizeOperation(operation: Operation) {

    }

    private fun visualizeFunctionExpression(expression: FunctionExpression) {

    }

    private fun visualizeAnonymousInit(declaration: AnonymousInit) {
        TODO("Not yet implemented")
    }

    private fun visualizeFunction(declaration: Function) {
        TODO("Not yet implemented")
    }

    private fun visualizeExpression(expression: Expression) {

    }

    private fun newLine(indent: Boolean = true) {
        annotatedStringBuilder.append(LINE_SEPARATOR)
        if (indent) indent()
    }

    private fun space() {
        annotatedStringBuilder.append(' ')
    }

    private fun indent() {
        for (i in 0 until indentLevel) annotatedStringBuilder.append(indentUnit)
    }

    private fun increaseIndent(block: () -> Unit) {
        indentLevel += 1
        block()
        indentLevel -= 1
    }

    private fun isDecomposerAnnotation(annotation: ConstructorCall): Boolean {
        val fqName = annotation.symbol.fqName
        return fqName == PRE_COMPOSE_IR_FQ_NAME || fqName == POST_COMPOSE_IR_FQ_NAME
    }

    private fun withTable(table: TopLevelTable, block: () -> Unit) {
        val previous = currentTable
        currentTable = table
        block()
        currentTable = previous
    }

    private fun strings(index: Int): String {
        return currentTable!!.strings.data[index]
    }

    private fun signatures(index: Int): Signature {
        return currentTable!!.signatures.data[index]
    }

    private fun bodies(index: Int): Body {
        return currentTable!!.bodies.data[index]
    }

    private fun types(index: Int): SimpleType {
        return currentTable!!.types.data[index]
    }

    private fun withPunctuation(block: () -> Unit) = withStyle(theme.code.punctuation, block)

    private fun withKeyword(block: () -> Unit) = withStyle(theme.code.keyword, block)

    private fun withValue(block: () -> Unit) = withStyle(theme.code.value, block)

    private fun withAnnotation(block: () -> Unit) = withStyle(theme.code.annotation, block)

    private fun withComment(block: () -> Unit) = withStyle(theme.code.comment, block)

    private fun withSimple(block: () -> Unit) = withStyle(theme.code.simple, block)

    private fun keyword(keyword: Keyword) = keyword(keyword.visual)

    private fun keyword(text: String) = withKeyword { append(text) }

    private fun punctuation(text: String) = withPunctuation { append(text) }

    private fun value(text: String) = withValue { append(text) }

    private fun simple(text: String) = withSimple { append(text) }

    private fun withStyle(style: SpanStyle, block: () -> Unit) {
        annotatedStringBuilder.pushStyle(style)
        block()
        annotatedStringBuilder.pop()
    }

    private fun withSourceLocation(sourceLocation: SourceLocation, block: () -> Unit) {
        val annotationString = Json.encodeToString(sourceLocation)
        annotatedStringBuilder.pushStringAnnotation(TAG_SOURCE_LOCATION, annotationString)
        block()
        annotatedStringBuilder.pop()
    }

    private fun withDescription(description: Description, block: () -> Unit) {
        val annotationString = Json.encodeToString(description)
        annotatedStringBuilder.pushStringAnnotation(TAG_DESCRIPTION, annotationString)
        block()
        annotatedStringBuilder.pop()
    }

    private fun append(char: Char) {
        annotatedStringBuilder.append(char)
    }

    private fun append(string: String) {
        annotatedStringBuilder.append(string)
    }

    private fun appendSpaced(char: Char) = with(annotatedStringBuilder) {
        append(' ')
        append(char)
        append(' ')
    }

    private fun appendSpaced(string: String) = with(annotatedStringBuilder) {
        append(' ')
        append(string)
        append(' ')
    }

    private val Symbol.fqName: String
        get() {
            return when (val signature = signatures(this.signatureId)) {
                is AccessorSignature -> TODO()
                is CommonSignature -> buildString {
                    signature.packageFqNameIndexes.forEach {
                        append(strings(it))
                        append('.')
                    }
                    append(strings(signature.declarationFqNameIndexes[0]))
                }
                is CompositeSignature -> TODO()
                EmptySignature -> TODO()
                is FileLocalSignature -> TODO()
                FileSignature -> TODO()
                is LocalSignature -> TODO()
                is ScopedLocalSignature -> TODO()
            }
        }

    private val Symbol.declarationName: String
        get() {
            return this.fqName.split(".").last()
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

    companion object {
        private val LINE_SEPARATOR: String = System.lineSeparator()
        private val PRE_COMPOSE_IR_FQ_NAME = "com.decomposer.runtime.PreComposeIr"
        private val POST_COMPOSE_IR_FQ_NAME = "com.decomposer.runtime.PostComposeIr"
        private val TAG_SOURCE_LOCATION = "SOURCE_LOCATION"
        private val TAG_DESCRIPTION = "DESCRPTION"
    }
}
