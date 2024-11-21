package com.decomposer.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.decomposer.ir.AccessorSignature
import com.decomposer.ir.AnonymousInit
import com.decomposer.ir.Block
import com.decomposer.ir.BlockBody
import com.decomposer.ir.Body
import com.decomposer.ir.BooleanConst
import com.decomposer.ir.Branch
import com.decomposer.ir.Break
import com.decomposer.ir.ByteConst
import com.decomposer.ir.Call
import com.decomposer.ir.Catch
import com.decomposer.ir.CharConst
import com.decomposer.ir.Class
import com.decomposer.ir.ClassFlags
import com.decomposer.ir.ClassKind
import com.decomposer.ir.ClassReference
import com.decomposer.ir.CommonSignature
import com.decomposer.ir.Composite
import com.decomposer.ir.CompositeSignature
import com.decomposer.ir.Const
import com.decomposer.ir.Constructor
import com.decomposer.ir.ConstructorCall
import com.decomposer.ir.Continue
import com.decomposer.ir.Coordinate
import com.decomposer.ir.Declaration
import com.decomposer.ir.DelegatingConstructorCall
import com.decomposer.ir.DoWhile
import com.decomposer.ir.DoubleConst
import com.decomposer.ir.DynamicMemberExpression
import com.decomposer.ir.DynamicOperatorExpression
import com.decomposer.ir.EmptySignature
import com.decomposer.ir.EnumConstructorCall
import com.decomposer.ir.EnumEntry
import com.decomposer.ir.ErrorCallExpression
import com.decomposer.ir.ErrorDeclaration
import com.decomposer.ir.ErrorExpression
import com.decomposer.ir.Expression
import com.decomposer.ir.ExpressionBody
import com.decomposer.ir.Field
import com.decomposer.ir.FieldFlags
import com.decomposer.ir.FileLocalSignature
import com.decomposer.ir.FileSignature
import com.decomposer.ir.FloatConst
import com.decomposer.ir.Function
import com.decomposer.ir.FunctionBase
import com.decomposer.ir.FunctionExpression
import com.decomposer.ir.FunctionFlags
import com.decomposer.ir.FunctionReference
import com.decomposer.ir.GetClass
import com.decomposer.ir.GetEnumValue
import com.decomposer.ir.GetField
import com.decomposer.ir.GetObject
import com.decomposer.ir.GetValue
import com.decomposer.ir.InstanceInitializerCall
import com.decomposer.ir.IntConst
import com.decomposer.ir.KotlinFile
import com.decomposer.ir.LocalDelegatedProperty
import com.decomposer.ir.LocalDelegatedPropertyReference
import com.decomposer.ir.LocalSignature
import com.decomposer.ir.LocalVarFlags
import com.decomposer.ir.LongConst
import com.decomposer.ir.Modality
import com.decomposer.ir.NullConst
import com.decomposer.ir.Property
import com.decomposer.ir.PropertyFlags
import com.decomposer.ir.PropertyReference
import com.decomposer.ir.Return
import com.decomposer.ir.ScopedLocalSignature
import com.decomposer.ir.SetField
import com.decomposer.ir.SetValue
import com.decomposer.ir.ShortConst
import com.decomposer.ir.Signature
import com.decomposer.ir.SimpleType
import com.decomposer.ir.StarProjection
import com.decomposer.ir.Statement
import com.decomposer.ir.StatementBody
import com.decomposer.ir.StringConcat
import com.decomposer.ir.StringConst
import com.decomposer.ir.Symbol
import com.decomposer.ir.SyntheticBody
import com.decomposer.ir.Throw
import com.decomposer.ir.TopLevelTable
import com.decomposer.ir.Try
import com.decomposer.ir.TypeAlias
import com.decomposer.ir.TypeAliasFlags
import com.decomposer.ir.TypeArgument
import com.decomposer.ir.TypeOp
import com.decomposer.ir.TypeParameter
import com.decomposer.ir.TypeParameterFlags
import com.decomposer.ir.TypeProjection
import com.decomposer.ir.ValueParameter
import com.decomposer.ir.ValueParameterFlags
import com.decomposer.ir.Vararg
import com.decomposer.ir.Variable
import com.decomposer.ir.Variance
import com.decomposer.ir.Visibility
import com.decomposer.ir.When
import com.decomposer.ir.While
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.max

enum class Keyword(val visual: String) {
    CLASS("class"),
    INTERFACE("interface"),
    ENUM("enum"),
    ANNOTATION("annotation"),
    OBJECT("object"),
    DATA("data"),
    INNER("inner"),
    COMPANION("companion"),
    VALUE("value"),
    INIT("init"),
    FUN("fun"),
    OVERRIDE("override"),
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
    BY("by"),
    STATIC("static"),
    EXTERNAL("external"),
    CONSTRUCTOR("constructor"),
    OPERATOR("operator"),
    INFIX("infix"),
    INLINE("inline"),
    TAILREC("tailrec"),
    SUSPEND("suspend"),
    REIFIED("reified"),
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
            withTable(it.value) { visualizeDeclaration(it.key) }
        }
    }

    private fun visualizeDeclaration(declaration: Declaration) {
        increaseIndent {
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
            newLine()
        }
    }

    private fun visualizeVariable(declaration: Variable) {
        val flags = declaration.base.flags as? LocalVarFlags
        flags?.let {
            visualizeLocalVarFlags(it)
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
        newLine()
    }

    private fun visualizeType(type: SimpleType) {
        visualizeAnnotations(type.annotations, multiLine = false)
        val name = type.symbol.declarationName
        append(name)
        visualizeTypeArguments(type.arguments)
    }

    private fun visualizeTypeArguments(arguments: List<TypeArgument>) {
        if (arguments.isNotEmpty()) {
            withAngleBrackets {
                arguments.forEachIndexed { index, argument ->
                    visualizeTypeArgument(argument)
                    if (index != arguments.size - 1) {
                        append(',')
                        space()
                    }
                }
            }
        }
    }

    private fun visualizeTypeArgument(argument: TypeArgument) {
        when (argument) {
            StarProjection -> append('*')
            is TypeProjection -> {
                visualizeVariance(argument.variance)
                val type = types(argument.typeIndex)
                visualizeType(type)
            }
        }
    }

    private fun visualizeVariance(variance: Variance) {
        when (variance) {
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

    private fun visualizePropertyFlags(flags: PropertyFlags) {
        visualizeVisibility(flags.visibility)
        visualizeModality(flags.modality)
        if (flags.isExpect) {
            keyword(Keyword.EXPECT)
            space()
        }
        if (flags.isLateinit) {
            keyword(Keyword.LATEINIT)
            space()
        }
        if (flags.isConst) {
            keyword(Keyword.CONST)
            space()
        }
        if (flags.isExternal) {
            keyword(Keyword.EXTERNAL)
            space()
        }
        if (flags.isVar) {
            keyword(Keyword.VAR)
        } else {
            keyword(Keyword.VAL)
        }
        space()
    }

    private fun visualizeProperty(declaration: Property) {
        visualizeAnnotations(declaration.base.annotations)
        val flags = declaration.base.flags as? PropertyFlags
        flags?.let {
            visualizePropertyFlags(it)
        }
        val name = strings(declaration.nameIndex)
        append(name)
        val typeIndex = declaration.backingField?.typeIndex ?: declaration.getter?.base?.typeIndex
        val type = types(typeIndex!!)
        appendSpaced(':')
        visualizeType(type)
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
        withBraces {
            when (body) {
                is ExpressionBody -> visualizeExpressionBody(body)
                is StatementBody -> visualizeStatementBody(body)
            }
        }
    }

    private fun visualizeLocalDelegatedProperty(declaration: LocalDelegatedProperty) {
        visualizeAnnotations(declaration.base.annotations)
        val flags = declaration.base.flags as? LocalVarFlags
        flags?.let {
            visualizeLocalVarFlags(it)
        }
        val name = strings(declaration.nameIndex)
        append(name)
        declaration.delegate?.let {
            keywordSpaced(Keyword.BY)
            val delegateName = strings(it.nameIndex)
            append(delegateName)
        }
        newLine()
    }

    private fun visualizeLocalVarFlags(flags: LocalVarFlags) {
        if (flags.isLateinit) {
            keyword(Keyword.LATEINIT)
            space()
        }
        if (flags.isConst) {
            keyword(Keyword.CONST)
            space()
        }
        if (flags.isVar) {
            keyword(Keyword.VAR)
        } else {
            keyword(Keyword.VAL)
        }
        space()
    }

    private fun visualizeField(declaration: Field) {
        val flags = declaration.base.flags as? FieldFlags
        flags?.let {
            visualizeFieldFlags(it)
        }
        val name = strings(declaration.nameIndex)
        append(name)
        append(": ")
        val type = types(declaration.typeIndex)
        visualizeType(type)
        declaration.initializerIndex?.let {
            val expressionBody = bodies(it) as? ExpressionBody
            expressionBody?.let {
                appendSpaced('=')
                visualizeExpressionBody(expressionBody)
            }
        }
    }

    private fun visualizeFieldFlags(flags: FieldFlags) {
        visualizeVisibility(flags.visibility)
        if (flags.isStatic) {
            keyword(Keyword.STATIC)
            space()
        }
        if (flags.isFinal) {
            keyword(Keyword.VAL)
        } else {
            keyword(Keyword.VAR)
        }
        space()
    }

    private fun visualizeErrorDeclaration(declaration: ErrorDeclaration) = Unit

    private fun visualizeEnumEntry(declaration: EnumEntry) {
        visualizeAnnotations(declaration.base.annotations)
        val name = strings(declaration.nameIndex)
        append(name)
        declaration.initializerIndex?.let {
            val expressionBody = bodies(it) as? ExpressionBody
            expressionBody?.let {
                visualizeExpressionBody(expressionBody)
            }
        }
        declaration.correspondingClass?.let { clazz ->
            withBraces {
                clazz.declarations.forEach { declaration ->
                    visualizeDeclaration(declaration)
                }
            }
        }
    }

    private fun visualizeConstructor(declaration: Constructor) {
        visualizeFunctionBase(declaration.base)
    }

    private fun visualizeFunctionBase(functionBase: FunctionBase) {
        visualizeAnnotations(functionBase.base.annotations)
        val flags = functionBase.base.flags as? FunctionFlags
        flags?.let {
            visualizeFunctionFlags(it)
        }
        val isConstructor = functionBase.base.symbol.kind == Symbol.Kind.CONSTRUCTOR_SYMBOL
        if (isConstructor) {
            keyword(Keyword.CONSTRUCTOR)
        } else {
            keywordSpaced(Keyword.FUN)
            visualizeTypeParameters(functionBase.typeParameters)
            functionBase.extensionReceiver?.let {
                val name = strings(it.nameIndex)
                append(name)
                append('.')
            }
            val name = strings(functionBase.nameIndex)
            append(name)
        }
        visualizeValueParameters(functionBase.valueParameters)
        val type = types(functionBase.typeIndex)
        append(": ")
        visualizeType(type)
        functionBase.bodyIndex?.let {
            val statementBody = bodies(it) as? StatementBody
            statementBody?.let {
                visualizeStatementBody(statementBody)
            }
        }
    }

    private fun visualizeFunctionFlags(flags: FunctionFlags) {
        visualizeVisibility(flags.visibility)
        visualizeModality(flags.modality)
        if (flags.isOperator) {
            keyword(Keyword.OPERATOR)
            space()
        }
        if (flags.isInfix) {
            keyword(Keyword.INFIX)
            space()
        }
        if (flags.isInline) {
            keyword(Keyword.INLINE)
            space()
        }
        if (flags.isTailrec) {
            keyword(Keyword.TAILREC)
            space()
        }
        if (flags.isExternal) {
            keyword(Keyword.EXTERNAL)
            space()
        }
        if (flags.isSuspend) {
            keyword(Keyword.SUSPEND)
            space()
        }
        if (flags.isExpect) {
            keyword(Keyword.EXPECT)
            space()
        }
    }

    private fun visualizeClass(declaration: Class) {
        visualizeAnnotations(declaration.base.annotations)
        val primaryConstructor = declaration.declarations.firstOrNull {
            it is Constructor && it.isPrimary
        } as? Constructor
        val declarationsNoPrimary = declaration.declarations.filter { it != primaryConstructor }
        val flags = declaration.base.flags as? ClassFlags
        flags?.let {
            visualizeClassFlags(it)
        }
        val name = strings(declaration.nameIndex)
        append(name)
        if (declaration.typeParameters.isNotEmpty()) {
            visualizeTypeParameters(declaration.typeParameters)
        }
        val constructorProperties = mutableListOf<Property>()
        primaryConstructor?.let {
            val valueParameters = it.base.valueParameters
            if (valueParameters.isNotEmpty()) {
                withParentheses {
                    valueParameters.forEachIndexed { index, parameter ->
                        val paramName = strings(parameter.nameIndex)
                        val property = declaration.findPropertyWithName(paramName)
                        if (property != null) {
                            constructorProperties.add(property)
                            visualizeProperty(property)
                        }
                        if (index != valueParameters.size - 1) {
                            append(',')
                        }
                        newLine()
                    }
                }
            }
        }
        space()
        val superTypes = declaration.superTypeIndexes
            .map { types(it) }
            .filter { it.symbol.declarationName != "Any" }
        if (superTypes.isNotEmpty()) {
            append(": ")
            superTypes.forEachIndexed { index, type ->
                visualizeType(type)
                if (index != superTypes.size - 1) append(", ")
            }
            space()
        }
        val remainingDeclarations = declarationsNoPrimary.toMutableList()
        remainingDeclarations.removeAll(constructorProperties)
        if (remainingDeclarations.isNotEmpty()) {
            withBraces {
                remainingDeclarations.forEach { declaration ->
                    visualizeDeclaration(declaration)
                }
            }
        } else {
            newLine()
        }
    }

    private fun visualizeClassFlags(flags: ClassFlags) {
        visualizeVisibility(flags.visibility)
        visualizeModality(flags.modality)
        if (flags.isInner) {
            keyword(Keyword.INNER)
            space()
        }
        if (flags.isExpect) {
            keyword(Keyword.EXPECT)
            space()
        }
        if (flags.isExternal) {
            keyword(Keyword.EXTERNAL)
            space()
        }
        if (flags.isData) {
            keyword(Keyword.DATA)
            space()
        }
        if (flags.isValue) {
            keyword(Keyword.VALUE)
            space()
        }
        if (flags.isFun) {
            keyword(Keyword.FUN)
            space()
        }
        if (flags.isCompanion) {
            keyword(Keyword.COMPANION)
            space()
        }
        visualizeClassKind(flags.kind)
    }

    private fun visualizeClassKind(kind: ClassKind) {
        when (kind) {
            ClassKind.CLASS -> {
                keyword(Keyword.CLASS)
                space()
            }
            ClassKind.INTERFACE -> {
                keyword(Keyword.INTERFACE)
                space()
            }
            ClassKind.ENUM_CLASS -> {
                keyword(Keyword.ENUM)
                keywordSpaced(Keyword.CLASS)
            }
            ClassKind.ENUM_ENTRY -> Unit
            ClassKind.ANNOTATION_CLASS -> {
                keyword(Keyword.ANNOTATION)
                keywordSpaced(Keyword.CLASS)
            }
            ClassKind.OBJECT -> {
                keyword(Keyword.OBJECT)
                space()
            }
        }
    }

    private fun visualizeValueParameters(
        declarations: List<ValueParameter>,
        multiLine: Boolean = true
    ) {
        if (declarations.isNotEmpty()) {
            withParentheses(multiLine) {
                declarations.forEach {
                    visualizeValueParameter(it)
                }
            }
        }
    }

    private fun visualizeValueParameter(declaration: ValueParameter) {
        val flags = declaration.base.flags as? ValueParameterFlags
        flags?.let {
            visualizeValueParameterFlags(it)
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

    private fun visualizeValueParameterFlags(flags: ValueParameterFlags) {
        if (flags.isCrossInline) {
            keyword(Keyword.CROSSINLINE)
            space()
        }
        if (flags.isNoInline) {
            keyword(Keyword.NOINLINE)
            space()
        }
    }

    private fun visualizeExpressionBody(expressionBody: ExpressionBody) {
        visualizeExpression(expressionBody.expression)
    }

    private fun visualizeStatementBody(statementBody: StatementBody) {
        visualizeStatement(statementBody.statement)
    }

    private fun visualizeTypeParameters(
        declarations: List<TypeParameter>,
        multiLine: Boolean = false
    ) {
        if (declarations.isNotEmpty()) {
            withAngleBrackets(multiLine) {
                declarations.forEach {
                    visualizeTypeParameter(it)
                }
            }
        }
    }

    private fun visualizeTypeParameter(declaration: TypeParameter) {
        val flags = declaration.base.flags as? TypeParameterFlags
        flags?.let {
            visualizeTypeParameterFlags(it)
        }
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

    private fun visualizeTypeParameterFlags(flags: TypeParameterFlags) {
        if (flags.isReified) {
            keyword(Keyword.REIFIED)
            space()
        }
        visualizeVariance(flags.variance)
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
                                visualizeExpression(expression)
                                if (index != normalArguments.size - 1) {
                                    append(',')
                                }
                            }
                        }
                    }
                } else {
                    normalArguments.forEachIndexed { index, expression ->
                        if (expression != null) {
                            visualizeExpression(expression)
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

    private fun visualizeFunctionExpression(expression: FunctionExpression) {
        visualizeLambda(expression.function.base)
    }

    private fun visualizeLambda(function: FunctionBase) {
        append('{')
        if (function.valueParameters.isNotEmpty()) {
            space()
            function.valueParameters.forEachIndexed { index, parameter ->
                visualizeValueParameter(parameter)
                if (index != function.valueParameters.size - 1) append(", ")
            }
            append(" ->")
        }
        newLine()
        function.bodyIndex?.let {
            val body = bodies(it) as? StatementBody
            if (body != null) {
                visualizeBody(body)
            }
        }
    }

    private fun visualizeAnonymousInit(declaration: AnonymousInit) {
        keyword(Keyword.INIT)
        space()
        withBraces {
            val statementBody = bodies(declaration.bodyIndex) as? StatementBody
            statementBody?.let {
                visualizeStatementBody(statementBody)
            }
        }
    }

    private fun visualizeFunction(declaration: Function) {
        if (declaration.overriden.isNotEmpty()) {
            keyword(Keyword.OVERRIDE)
            space()
        }
        visualizeFunctionBase(declaration.base)
    }

    private fun visualizeBlock(block: Block) {

    }

    private fun visualizeExpression(expression: Expression) {
        val operation = expression.operation
        when (operation) {
            is Block -> visualizeBlock(operation)
            is Break -> visualizeBreak(operation)
            is Call -> visualizeCall(operation)
            is ClassReference -> visualizeClassReference(operation)
            is Composite -> visualizeComposite(operation)
            is Const -> visualizeConst(operation)
            is ConstructorCall -> visualizeConstructorCall(operation)
            is Continue -> visualizeContinue(operation)
            is DelegatingConstructorCall -> visualizeDelegatingConstructorCall(operation)
            is DoWhile -> visualizeDoWhile(operation)
            is DynamicMemberExpression -> visualizeDynamicMemberExpression(operation)
            is DynamicOperatorExpression -> visualizeOperatorExpression(operation)
            is EnumConstructorCall -> visualizeEnumConstructorCall(operation)
            is ErrorCallExpression -> visualizeErrorCallExpression(operation)
            is ErrorExpression -> visualizeErrorExpression(operation)
            is FunctionExpression -> visualizeFunctionExpression(operation)
            is FunctionReference -> visualizeFunctionReference(operation)
            is GetClass -> visualizeGetClass(operation)
            is GetEnumValue -> visualizeGetEnumValue(operation)
            is GetField -> visualizeGetField(operation)
            is GetObject -> visualizeGetObject(operation)
            is GetValue -> visualizeGetValue(operation)
            is InstanceInitializerCall -> visualizeInstanceInitializerCall(operation)
            is LocalDelegatedPropertyReference -> visualizeLocalDelegatedPropertyReference(operation)
            is PropertyReference -> visualizePropertyReference(operation)
            is Return -> visualizeReturn(operation)
            is SetField -> visualizeSetField(operation)
            is SetValue -> visualizeSetValue(operation)
            is StringConcat -> visualizeStringConcat(operation)
            is Throw -> visualizeThrow(operation)
            is Try -> visualizeTry(operation)
            is TypeOp -> visualizeTypeOp(operation)
            is Vararg -> visualizeVarargs(operation)
            is When -> visualizeWhen(operation)
            is While -> visualizeWhile(operation)
        }
    }

    private fun visualizePropertyReference(operation: PropertyReference) {
        TODO("Not yet implemented")
    }

    private fun visualizeLocalDelegatedPropertyReference(
        operation: LocalDelegatedPropertyReference
    ) {
        TODO("Not yet implemented")
    }

    private fun visualizeInstanceInitializerCall(operation: InstanceInitializerCall) {
        TODO("Not yet implemented")
    }

    private fun visualizeGetValue(operation: GetValue) {
        TODO("Not yet implemented")
    }

    private fun visualizeGetObject(operation: GetObject) {
        TODO("Not yet implemented")
    }

    private fun visualizeGetField(operation: GetField) {
        TODO("Not yet implemented")
    }

    private fun visualizeGetEnumValue(operation: GetEnumValue) {
        TODO("Not yet implemented")
    }

    private fun visualizeReturn(operation: Return) {
        TODO("Not yet implemented")
    }

    private fun visualizeSetField(operation: SetField) {
        TODO("Not yet implemented")
    }

    private fun visualizeSetValue(operation: SetValue) {
        TODO("Not yet implemented")
    }

    private fun visualizeStringConcat(operation: StringConcat) {
        TODO("Not yet implemented")
    }

    private fun visualizeThrow(operation: Throw) {
        TODO("Not yet implemented")
    }

    private fun visualizeTry(operation: Try) {
        TODO("Not yet implemented")
    }

    private fun visualizeTypeOp(operation: TypeOp) {
        TODO("Not yet implemented")
    }

    private fun visualizeVarargs(operation: Vararg) {
        TODO("Not yet implemented")
    }

    private fun visualizeWhen(operation: When) {
        TODO("Not yet implemented")
    }

    private fun visualizeWhile(operation: While) {
        TODO("Not yet implemented")
    }

    private fun visualizeGetClass(operation: GetClass) {
        TODO("Not yet implemented")
    }

    private fun visualizeFunctionReference(operation: FunctionReference) {
        TODO("Not yet implemented")
    }

    private fun visualizeErrorExpression(operation: ErrorExpression) {
        TODO("Not yet implemented")
    }

    private fun visualizeErrorCallExpression(operation: ErrorCallExpression) {
        TODO("Not yet implemented")
    }

    private fun visualizeEnumConstructorCall(operation: EnumConstructorCall) {
        TODO("Not yet implemented")
    }

    private fun visualizeOperatorExpression(operation: DynamicOperatorExpression) {
        TODO("Not yet implemented")
    }

    private fun visualizeDynamicMemberExpression(operation: DynamicMemberExpression) {
        TODO("Not yet implemented")
    }

    private fun visualizeDoWhile(operation: DoWhile) {
        TODO("Not yet implemented")
    }

    private fun visualizeDelegatingConstructorCall(operation: DelegatingConstructorCall) {
        TODO("Not yet implemented")
    }

    private fun visualizeContinue(operation: Continue) {
        TODO("Not yet implemented")
    }

    private fun visualizeConst(operation: Const) {
        when (operation) {
            is BooleanConst -> TODO()
            is ByteConst -> TODO()
            is CharConst -> TODO()
            is DoubleConst -> TODO()
            is FloatConst -> TODO()
            is IntConst -> TODO()
            is LongConst -> TODO()
            NullConst -> TODO()
            is ShortConst -> TODO()
            is StringConst -> TODO()
        }
    }

    private fun visualizeComposite(operation: Composite) {
        TODO("Not yet implemented")
    }

    private fun visualizeClassReference(operation: ClassReference) {
        TODO("Not yet implemented")
    }

    private fun visualizeCall(operation: Call) {
        TODO("Not yet implemented")
    }

    private fun visualizeBreak(operation: Break) {
        TODO("Not yet implemented")
    }

    private fun visualizeStatement(statement: Statement) {
        when (val statementBase = statement.statement) {
            is Declaration -> visualizeDeclaration(statementBase)
            is BlockBody -> visualizeBlockBody(statementBase)
            is Branch -> visualizeBranch(statementBase)
            is Catch -> visualizeCatch(statementBase)
            is Expression -> visualizeExpression(statementBase)
            is SyntheticBody -> visualizeSyntheticBody(statementBase)
        }
    }

    private fun visualizeSyntheticBody(statement: SyntheticBody) {
        TODO("Not yet implemented")
    }

    private fun visualizeCatch(statement: Catch) {
        TODO("Not yet implemented")
    }

    private fun visualizeBranch(statement: Branch) {
        TODO("Not yet implemented")
    }

    private fun visualizeBlockBody(statement: BlockBody) {
        TODO("Not yet implemented")
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

    private fun keywordSpaced(keyword: Keyword) = withKeyword { appendSpaced(keyword.visual) }

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

    private fun withBraces(multiLine: Boolean = true, block: () -> Unit) {
        append('{')
        if (!multiLine) {
            space()
            block()
            space()
        } else {
            increaseIndent {
                newLine()
                block()
                newLine()
            }
        }
        append('}')
    }

    private fun withParentheses(multiLine: Boolean = true, block: () -> Unit) {
        append('(')
        if (!multiLine) {
            block()
        } else {
            increaseIndent {
                newLine()
                block()
                newLine()
            }
        }
        append(')')
    }

    private fun withAngleBrackets(multiLine: Boolean = false, block: () -> Unit) {
        append('<')
        if (!multiLine) {
            block()
        } else {
            increaseIndent {
                newLine()
                block()
                newLine()
            }
        }
        append('>')
    }

    private fun Class.findPropertyWithName(name: String): Property? {
        return this.declarations.firstOrNull {
            it is Property && strings(nameIndex) == name
        } as? Property
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
