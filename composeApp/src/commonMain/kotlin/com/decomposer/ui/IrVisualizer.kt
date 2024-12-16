package com.decomposer.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
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
import com.decomposer.ir.EnumConstructorCall
import com.decomposer.ir.EnumEntry
import com.decomposer.ir.ErrorCallExpression
import com.decomposer.ir.ErrorDeclaration
import com.decomposer.ir.ErrorExpression
import com.decomposer.ir.Expression
import com.decomposer.ir.ExpressionBody
import com.decomposer.ir.Field
import com.decomposer.ir.FieldFlags
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
import com.decomposer.ir.LocalVarFlags
import com.decomposer.ir.LongConst
import com.decomposer.ir.Modality
import com.decomposer.ir.NullConst
import com.decomposer.ir.Property
import com.decomposer.ir.PropertyFlags
import com.decomposer.ir.PropertyReference
import com.decomposer.ir.Return
import com.decomposer.ir.SetField
import com.decomposer.ir.SetValue
import com.decomposer.ir.ShortConst
import com.decomposer.ir.Signature
import com.decomposer.ir.SimpleType
import com.decomposer.ir.SpreadElement
import com.decomposer.ir.StarProjection
import com.decomposer.ir.Statement
import com.decomposer.ir.StatementBase
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
import com.decomposer.ir.TypeOperator
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

class IrVisualBuilder(
    private val kotlinFile: KotlinFile,
    private val packageName: String? = null,
    private val indentSize: Int = 2,
    private val theme: Theme = Theme.dark,
    private val highlights: List<Pair<Int, Int>> = emptyList(),
    private val onClickDescription: (Description) -> Unit,
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
        packageName?.let {
            keyword(Keyword.PACKAGE)
            space()
            simple(it)
            newLine(indent = false)
            newLine(indent = false)
        }
        sortedDeclarations.forEach {
            withTable(it.value) { visualizeDeclaration(it.key) }
        }
    }

    private fun visualizeDeclaration(declaration: Declaration) {
        when (declaration) {
            is Function -> {
                visualizeFunction(declaration)
                newLine()
            }
            is AnonymousInit -> visualizeAnonymousInit(declaration)
            is Class -> {
                visualizeClass(declaration)
                newLine()
            }
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

    private fun visualizeVariable(declaration: Variable) {
        val flags = (declaration.base.flags as? LocalVarFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        val name = strings(declaration.nameIndex)
        symbol(name)
        val type = types(declaration.typeIndex)
        punctuation(':')
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
        symbol(name)
        visualizeTypeArguments(type.arguments)
    }

    private fun visualizeTypeArguments(arguments: List<TypeArgument>) {
        if (arguments.isNotEmpty()) {
            withAngleBrackets {
                arguments.forEachIndexed { index, argument ->
                    visualizeTypeArgument(argument)
                    if (index != arguments.size - 1) {
                        punctuation(',')
                        space()
                    }
                }
            }
        }
    }

    private fun visualizeTypeArgument(argument: TypeArgument) {
        when (argument) {
            StarProjection -> punctuation('*')
            is TypeProjection -> {
                argument.variance.keyword?.let {
                    keyword(it)
                    space()
                }
                val type = types(argument.typeIndex)
                visualizeType(type)
            }
        }
    }

    private fun visualizeTypeAlias(declaration: TypeAlias) {
        val flags = (declaration.base.flags as? TypeAliasFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        keyword(Keyword.TYPEALIAS)
        space()
        val name = strings(declaration.nameIndex)
        symbol(name)
        val type = types(declaration.typeIndex)
        punctuationSpaced('=')
        visualizeType(type)
    }

    private fun visualizeProperty(declaration: Property) {
        visualizeAnnotations(declaration.base.annotations)
        val flags = (declaration.base.flags as? PropertyFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        val name = strings(declaration.nameIndex)
        symbol(name)
        val typeIndex = declaration.backingField?.typeIndex ?: declaration.getter?.base?.typeIndex
        val type = types(typeIndex!!)
        punctuationSpaced(':')
        visualizeType(type)
        increaseIndent {
            declaration.getter?.let { getter ->
                newLine()
                visualizeAnnotations(getter.base.base.annotations)
                keyword(Keyword.GET)
                punctuationSpaced("()")
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
                punctuationSpaced("()")
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
        val flags = (declaration.base.flags as? LocalVarFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        val name = strings(declaration.nameIndex)
        symbol(name)
        declaration.delegate?.let {
            keywordSpaced(Keyword.BY)
            val delegateName = strings(it.nameIndex)
            symbol(delegateName)
        }
        newLine()
    }

    private fun visualizeField(declaration: Field) {
        val flags = (declaration.base.flags as? FieldFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        val name = strings(declaration.nameIndex)
        symbol(name)
        punctuation(":")
        space()
        val type = types(declaration.typeIndex)
        visualizeType(type)
        declaration.initializerIndex?.let {
            val expressionBody = bodies(it) as? ExpressionBody
            expressionBody?.let {
                punctuation('=')
                visualizeExpressionBody(expressionBody)
            }
        }
    }

    private fun visualizeErrorDeclaration(declaration: ErrorDeclaration) = Unit

    private fun visualizeEnumEntry(declaration: EnumEntry) {
        visualizeAnnotations(declaration.base.annotations)
        val name = strings(declaration.nameIndex)
        symbol(name)
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
        val flags = (functionBase.base.flags as? FunctionFlags).keywords
        val isConstructor = functionBase.base.symbol.kind == Symbol.Kind.CONSTRUCTOR_SYMBOL
        if (isConstructor) {
            keyword(Keyword.CONSTRUCTOR)
        } else {
            flags.forEach {
                keyword(it)
                space()
            }
            keyword(Keyword.FUN)
            space()
            visualizeTypeParameters(functionBase.typeParameters)
            functionBase.extensionReceiver?.let {
                val name = strings(it.nameIndex)
                symbol(name)
                punctuation('.')
            }
            val name = strings(functionBase.nameIndex)
            function(name)
        }
        visualizeValueParameters(functionBase.valueParameters)
        val type = types(functionBase.typeIndex)
        punctuation(':')
        space()
        visualizeType(type)
        functionBase.bodyIndex?.let {
            val statementBody = bodies(it) as? StatementBody
            statementBody?.let {
                visualizeStatementBody(statementBody)
            }
        }
    }

    private fun visualizeClass(declaration: Class) {
        visualizeAnnotations(declaration.base.annotations)
        val primaryConstructor = declaration.declarations.firstOrNull {
            it is Constructor && it.isPrimary
        } as? Constructor
        val declarationsNoPrimary = declaration.declarations.filter { it != primaryConstructor }
        val flags = (declaration.base.flags as? ClassFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        val name = strings(declaration.nameIndex)
        symbol(name)
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
                            punctuation(',')
                        }
                        newLine()
                    }
                }
            }
        }
        space()
        val superTypes = declaration.superTypeIndexes
            .map { types(it) }
            .filter { !it.isAny() }
        if (superTypes.isNotEmpty()) {
            punctuation(':')
            space()
            superTypes.forEachIndexed { index, type ->
                visualizeType(type)
                if (index != superTypes.size - 1) {
                    punctuation(',')
                    space()
                }
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
        val flags = (declaration.base.flags as? ValueParameterFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        visualizeAnnotations(declaration.base.annotations, multiLine = false)
        val name = strings(declaration.nameIndex)
        symbol(name)
        punctuation(':')
        space()
        val type = types(declaration.typeIndex)
        visualizeType(type)
        declaration.defaultValueIndex?.let {
            val expressionBody = bodies(it) as? ExpressionBody ?: return@let
            punctuationSpaced('=')
            visualizeExpressionBody(expressionBody)
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
        val flags = (declaration.base.flags as? TypeParameterFlags).keywords
        flags.forEach {
            keyword(it)
            space()
        }
        val name = strings(declaration.nameIndex)
        symbol(name)
        val superTypes = declaration.superTypeIndexes.map { types(it) }
        if (superTypes.isNotEmpty()) {
            punctuationSpaced(':')
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
        punctuation('@')
        visualizeConstructorCall(annotation, isAnnotation = true, multiLine = multiLine)
    }

    private fun visualizeConstructorCall(
        call: ConstructorCall,
        isAnnotation: Boolean = false,
        multiLine: Boolean = false
    ) {
        call.memberAccess.dispatchReceiver?.let {
            visualizeExpression(it)
            punctuation('.')
        }
        symbol(call.symbol.declarationName)
        val shouldVisualize = !isAnnotation || call.memberAccess.valueArguments.any { it != null }
        if (shouldVisualize) {
            visualizeArguments(call.memberAccess.valueArguments, multiLine)
        }
    }

    private fun visualizeArguments(valueArguments: List<Expression?>, multiLine: Boolean = true) {
        val trailingLambda = valueArguments.lastOrNull()?.let {
            it.operation as? FunctionExpression
        }
        val normalArguments = if (trailingLambda != null) {
            valueArguments.dropLast(1)
        } else valueArguments

        if (normalArguments.isNotEmpty()) {
            punctuation('(')
            withSimple {
                if (multiLine) {
                    increaseIndent {
                        normalArguments.forEachIndexed { index, expression ->
                            newLine()
                            if (expression != null) {
                                visualizeExpression(expression)
                                if (index != normalArguments.size - 1) {
                                    punctuation(',')
                                }
                            }
                        }
                    }
                } else {
                    normalArguments.forEachIndexed { index, expression ->
                        if (expression != null) {
                            visualizeExpression(expression)
                            if (index != normalArguments.size - 1) {
                                punctuation(',')
                                space()
                            }
                        }
                    }
                }
            }
            if (multiLine) newLine()
            punctuation(')')
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
        val startOffset = function.base.coordinate.startOffset
        val endOffset = function.base.coordinate.endOffset
        val highlighting = highlights.firstOrNull {
            it.first == startOffset && it.second == endOffset
        } != null
        val block = {
            withSourceLocation(SourceLocation(startOffset, endOffset)) {
                punctuation('{')
                if (function.valueParameters.isNotEmpty()) {
                    space()
                    function.valueParameters.forEachIndexed { index, parameter ->
                        visualizeValueParameter(parameter)
                        if (index != function.valueParameters.size - 1) {
                            punctuation(',')
                            space()
                        }
                    }
                    space()
                    punctuation("->")
                }
                newLine()
                function.bodyIndex?.let {
                    val body = bodies(it) as? StatementBody
                    if (body != null) {
                        visualizeBody(body)
                    }
                }
            }
        }
        if (highlighting) {
            highlight { block() }
        } else {
            block()
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
        val startOffset = declaration.base.base.coordinate.startOffset
        val endOffset = declaration.base.base.coordinate.endOffset
        val highlighting = highlights.firstOrNull {
            it.first == startOffset && it.second == endOffset
        } != null
        val block = {
            withSourceLocation(SourceLocation(startOffset, endOffset)) {
                if (declaration.overriden.isNotEmpty()) {
                    keyword(Keyword.OVERRIDE)
                    space()
                }
                visualizeFunctionBase(declaration.base)
            }
        }
        if (highlighting) {
            highlight { block() }
        } else {
            block()
        }
    }

    private fun visualizeExpression(expression: Expression) {
        when (val operation = expression.operation) {
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
            is DynamicOperatorExpression -> visualizeDynamicOperatorExpression(operation)
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

    private fun visualizeBlock(operation: Block) {
        function("run")
        space()
        withBraces {
            operation.statements.forEach {
                visualizeStatement(it)
            }
        }
    }

    private fun visualizePropertyReference(operation: PropertyReference) {
        punctuation("::")
        symbol(operation.symbol.declarationName)
    }

    private fun visualizeLocalDelegatedPropertyReference(
        operation: LocalDelegatedPropertyReference
    ) {
        punctuation("::")
        symbol(operation.delegateSymbol.declarationName)
    }

    private fun visualizeInstanceInitializerCall(operation: InstanceInitializerCall) = Unit

    private fun visualizeGetValue(operation: GetValue) {
        symbol(operation.symbol.declarationName)
    }

    private fun visualizeGetObject(operation: GetObject) {
        symbol(operation.symbol.declarationName)
    }

    private fun visualizeGetField(operation: GetField) {
        val receiver = operation.fieldAccess.receiver
        if (receiver != null) {
            visualizeExpression(receiver)
            punctuation('.')
        }
        symbol(operation.fieldAccess.symbol.declarationName)
    }

    private fun visualizeGetEnumValue(operation: GetEnumValue) {
        symbol(operation.symbol.declarationName)
    }

    private fun visualizeReturn(operation: Return) {
        keyword(Keyword.RETURN)
        val targetName = operation.returnTargetSymbol.declarationName
        if (operation.returnTargetSymbol.kind != Symbol.Kind.FUNCTION_SYMBOL) {
            punctuation('@')
            symbol(targetName)
        }
        val returnType = types(operation.value.typeIndex)
        if (!(returnType.isUnit() && operation.value.operation is GetObject)) {
            space()
            visualizeExpression(operation.value)
        }
    }

    private fun visualizeSetField(operation: SetField) {
        operation.fieldAccess.receiver?.let {
            visualizeExpression(it)
            punctuation('.')
        }
        val name = operation.fieldAccess.symbol.declarationName
        symbol(name)
        punctuationSpaced('=')
        visualizeExpression(operation.value)
    }

    private fun visualizeSetValue(operation: SetValue) {
        val name = operation.symbol.declarationName
        symbol(name)
        punctuationSpaced('=')
        visualizeExpression(operation.value)
    }

    private fun visualizeStringConcat(operation: StringConcat) {
        withQuotes {
            operation.arguments.forEach {
                when (it.operation) {
                    is StringConst -> visualizeConst(it.operation)
                    is GetValue -> {
                        punctuation('$')
                        visualizeGetValue(it.operation)
                    }
                    else -> {
                        punctuation('$')
                        withBraces(false) {
                            visualizeExpression(it)
                        }
                    }
                }
            }
        }
    }

    private fun visualizeThrow(operation: Throw) {
        keyword(Keyword.THROW)
        space()
        visualizeExpression(operation.value)
    }

    private fun visualizeTry(operation: Try) {
        keyword(Keyword.TRY)
        space()
        withBraces {
            visualizeExpression(operation.result)
        }
        val catches = operation.catch
        catches.forEach {
            visualizeStatement(it)
        }
        operation.finally?.let {
            keywordSpaced(Keyword.FINALLY)
            withBraces {
                visualizeExpression(it)
            }
        }
    }

    private fun visualizeTypeOp(operation: TypeOp) {
        visualizeExpression(operation.argument)
        operation.operator.mapKeyword()?.let {
            keywordSpaced(it)
            val type = types(operation.operandIndex)
            visualizeType(type)
        }
    }

    private fun visualizeVarargs(operation: Vararg) {
        operation.elements.forEachIndexed { index, element ->
            when (element) {
                is Expression -> visualizeExpression(element)
                is SpreadElement -> visualizeSpreadElement(element)
            }
            if (index != operation.elements.size - 1) {
                punctuation(',')
                space()
            }
        }
    }

    private fun visualizeSpreadElement(element: SpreadElement) {
        punctuation('*')
        visualizeExpression(element.expression)
    }

    private fun visualizeWhen(operation: When) {
        val origin = operation.originNameIndex?.let {
            statementOrigin(it)
        }
        when (origin) {
            StatementOrigin.OROR -> {
                val branches = operation.branches.map { it.statement as Branch }
                visualizeExpression(branches[0].condition)
                punctuationSpaced("||")
                visualizeExpression(branches[1].result)
            }
            StatementOrigin.ANDAND -> {
                val branches = operation.branches.map { it.statement as Branch }
                visualizeExpression(branches[0].condition)
                punctuationSpaced("&&")
                visualizeExpression(branches[0].result)
            }
            StatementOrigin.IF -> {
                val branches = operation.branches.map { it.statement as Branch }
                branches.forEachIndexed { index, branch ->
                    val isElse = index == branches.size - 1
                            && (branch.condition.operation as? BooleanConst)?.value == true
                    val isIf = index == 0
                    when {
                        isIf -> {
                            keyword(Keyword.IF)
                            space()
                            withParentheses(false) {
                                visualizeExpression(branch.condition)
                            }
                            space()
                            withBraces {
                                visualizeExpression(branch.result)
                            }
                        }
                        isElse -> {
                            keywordSpaced(Keyword.ELSE)
                            withBraces {
                                visualizeExpression(branch.result)
                            }
                        }
                        else -> {
                            keywordSpaced(Keyword.ELSE)
                            keyword(Keyword.IF)
                            space()
                            withParentheses(false) {
                                visualizeExpression(branch.condition)
                            }
                            space()
                            withBraces {
                                visualizeExpression(branch.result)
                            }
                        }
                    }
                }
            }
            else -> {
                val branches = operation.branches.map { it.statement as Branch }
                keyword(Keyword.WHEN)
                withBraces {
                    branches.forEach {
                        val isElse = (it.condition.operation as? BooleanConst)?.value == true
                        if (isElse) {
                            keyword(Keyword.ELSE)
                        } else {
                            visualizeExpression(it.condition)
                        }
                        punctuationSpaced("->")
                        withBraces {
                            visualizeExpression(it.result)
                        }
                    }
                }
            }
        }
    }

    private fun visualizeWhile(operation: While) {
        operation.loop.labelIndex?.let {
            symbol(strings(it))
            punctuation('@')
        }
        keyword(Keyword.WHILE)
        space()
        withParentheses(false) {
            visualizeExpression(operation.loop.condition)
        }
        space()
        withBraces {
            operation.loop.body?.let {
                visualizeExpression(it)
            }
        }
    }

    private fun visualizeGetClass(operation: GetClass) {
        visualizeExpression(operation.argument)
        punctuation("::")
        keyword(Keyword.CLASS)
    }

    private fun visualizeFunctionReference(operation: FunctionReference) {
        val dispatchReceiver = operation.memberAccess.dispatchReceiver
        val extensionReceiver = operation.memberAccess.extensionReceiver
        if (dispatchReceiver != null) {
            visualizeExpression(dispatchReceiver)
        } else if (extensionReceiver != null) {
            visualizeExpression(extensionReceiver)
        }
        punctuation("::")
        symbol(operation.symbol.declarationName)
    }

    private fun visualizeErrorExpression(operation: ErrorExpression) = Unit
    private fun visualizeErrorCallExpression(operation: ErrorCallExpression) = Unit

    private fun visualizeEnumConstructorCall(operation: EnumConstructorCall) {
        val valueArguments = operation.memberAccess.valueArguments

        if (valueArguments.isNotEmpty()) {
            withParentheses(false) {
                valueArguments.forEach {
                    it?.let {
                        visualizeExpression(it)
                    }
                }
            }
        }
    }

    private fun visualizeDynamicOperatorExpression(operation: DynamicOperatorExpression) = Unit
    private fun visualizeDynamicMemberExpression(operation: DynamicMemberExpression) = Unit

    private fun visualizeDoWhile(operation: DoWhile) {
        keyword(Keyword.DO)
        space()
        withBraces {
            operation.loop.body?.let {
                visualizeExpression(it)
            }
        }
        keywordSpaced(Keyword.WHILE)
        withParentheses {
            visualizeExpression(operation.loop.condition)
        }
    }

    private fun visualizeDelegatingConstructorCall(operation: DelegatingConstructorCall) {
        keyword(Keyword.THIS)
        val arguments = operation.memberAccess.valueArguments
        withParentheses(false) {
            arguments.forEach {
                it?.let {
                    visualizeExpression(it)
                }
            }
        }
    }

    private fun visualizeContinue(operation: Continue) {
        keyword(Keyword.CONTINUE)
        operation.labelIndex?.let {
            punctuation('@')
            symbol(strings(it))
        }
    }

    private fun visualizeConst(operation: Const) {
        when (operation) {
            is BooleanConst -> {
                value(operation.value.toString())
            }
            is ByteConst -> {
                value(operation.value.toString())
            }
            is CharConst -> {
                punctuation('\'')
                value(operation.value.toString())
                punctuation('\'')
            }
            is DoubleConst -> {
                value(operation.value.toString())
            }
            is FloatConst -> {
                value("${operation.value}f")
            }
            is IntConst -> {
                withDescription(Description(operation.value.toString(2))) {
                    value(operation.value.toString())
                }
            }
            is LongConst -> {
                value("${operation.value}L")
            }
            NullConst -> {
                value("null")
            }
            is ShortConst -> {
                value(operation.value.toString())
            }
            is StringConst -> {
                punctuation('"')
                value(strings(operation.valueIndex))
                punctuation('"')
            }
        }
    }

    private fun visualizeComposite(operation: Composite) {
        operation.statements.forEach {
            visualizeStatement(it)
            newLine()
        }
    }

    private fun visualizeClassReference(operation: ClassReference) {
        val type = types(operation.classTypeIndex)
        visualizeType(type)
        punctuation("::")
        keyword(Keyword.CLASS)
    }

    private fun visualizeCall(operation: Call) {
        val dispatchReceiver = operation.memberAccess.dispatchReceiver
        val extensionReceiver = operation.memberAccess.extensionReceiver
        dispatchReceiver ?: extensionReceiver?.let {
            visualizeExpression(it)
            punctuation('.')
        }
        symbol(operation.symbol.declarationName)
        val types = operation.memberAccess.typeArgumentIndexes.map {
            types(it)
        }
        if (types.isNotEmpty()) {
            withAngleBrackets {
                types.forEach {
                    visualizeType(it)
                }
            }
        }
        val valueArguments = operation.memberAccess.valueArguments
        if (valueArguments.isNotEmpty()) {
            withParentheses(false) {
                valueArguments.forEach {
                    it?.let {
                        visualizeExpression(it)
                    }
                }
            }
        }
    }

    private fun visualizeBreak(operation: Break) {
        keyword(Keyword.BREAK)
        operation.labelIndex?.let {
            punctuation('@')
            symbol(strings(it))
        }
    }

    private fun visualizeStatement(statement: Statement) {
        visualizeStatementBase(statement.statement)
        newLine()
    }

    private fun visualizeStatementBase(statementBase: StatementBase) {
        when (statementBase) {
            is Declaration -> visualizeDeclaration(statementBase)
            is BlockBody -> visualizeBlockBody(statementBase)
            is Branch -> visualizeBranch(statementBase)
            is Catch -> visualizeCatch(statementBase)
            is Expression -> visualizeExpression(statementBase)
            is SyntheticBody -> visualizeSyntheticBody(statementBase)
        }
    }

    private fun visualizeSyntheticBody(statement: SyntheticBody) = Unit
    private fun visualizeBranch(statement: Branch) = Unit

    private fun visualizeBlockBody(statement: BlockBody) {
        statement.statements.forEach {
            visualizeStatement(it)
            newLine()
        }
    }

    private fun visualizeCatch(statement: Catch) {
        keywordSpaced(Keyword.CATCH)
        val variable = statement.catchParameter
        val name = strings(variable.nameIndex)
        val type = types(variable.typeIndex)
        withParentheses(false) {
            symbol(name)
            punctuation(':')
            space()
            visualizeType(type)
        }
        space()
        withBraces {
            visualizeExpression(statement.result)
        }
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

    private fun statementOrigin(index: Int): StatementOrigin {
        val name = strings(index)
        return StatementOrigin.valueOf(name)
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

    private fun withQuotes(block: () -> Unit) {
        punctuation('"')
        block()
        punctuation('"')
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

    private fun punctuation(char: Char) = withPunctuation { append(char) }

    private fun punctuationSpaced(char: Char) {
        space()
        punctuation(char)
        space()
    }

    private fun punctuationSpaced(text: String) {
        space()
        punctuation(text)
        space()
    }

    private fun value(text: String) = withValue { append(text) }

    private fun simple(text: String) = withSimple { append(text) }

    private fun function(text: String) = simple(text)

    private fun symbol(text: String) = simple(text)

    private fun highlight(block: () -> Unit) {
        withStyle(style = theme.code.highlight, block)
    }

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
        val link = LinkAnnotation.Clickable(
            TAG_DESCRIPTION,
            TextLinkStyles(style = theme.code.value.copy(textDecoration = TextDecoration.Underline))
        ) {
            onClickDescription(description)
        }
        annotatedStringBuilder.pushLink(link)
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
        punctuation('{')
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
        punctuation('}')
    }

    private fun withParentheses(multiLine: Boolean = true, block: () -> Unit) {
        punctuation('(')
        if (!multiLine) {
            block()
        } else {
            increaseIndent {
                newLine()
                block()
                newLine()
            }
        }
        punctuation(')')
    }

    private fun withAngleBrackets(multiLine: Boolean = false, block: () -> Unit) {
        punctuation('<')
        if (!multiLine) {
            block()
        } else {
            increaseIndent {
                newLine()
                block()
                newLine()
            }
        }
        punctuation('>')
    }

    private fun Class.findPropertyWithName(name: String): Property? {
        return this.declarations.firstOrNull {
            it is Property && strings(nameIndex) == name
        } as? Property
    }

    private val Modality.keyword: Keyword?
        get() {
            return when (this) {
                Modality.FINAL -> null
                Modality.SEALED -> Keyword.SEALED
                Modality.OPEN -> Keyword.OPEN
                Modality.ABSTRACT -> Keyword.ABSTRACT
            }
        }


    private val Visibility.keyword: Keyword?
        get() {
            return when (this) {
                Visibility.PRIVATE,
                Visibility.PRIVATE_TO_THIS -> Keyword.PRIVATE
                Visibility.PROTECTED -> Keyword.PROTECTED
                Visibility.INTERNAL -> Keyword.INTERNAL
                Visibility.PUBLIC,
                Visibility.LOCAL,
                Visibility.INHERITED,
                Visibility.INVISIBLE_FAKE,
                Visibility.UNKNOWN -> null
            }
        }

    private val Variance.keyword: Keyword?
        get() {
            return when (this) {
                Variance.INVARIANT -> null
                Variance.IN_VARIANCE -> Keyword.IN
                Variance.OUT_VARIANCE -> Keyword.OUT
            }
        }

    private val ValueParameterFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            if (this.isCrossInline) keywords.add(Keyword.CROSSINLINE)
            if (this.isNoInline) keywords.add(Keyword.NOINLINE)
            return keywords
        }

    private val TypeAliasFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            this.visibility.keyword?.let { keywords.add(it) }
            if (this.isActual) keywords.add(Keyword.ACTUAL)
            return keywords
        }

    private val TypeParameterFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            if (this.isReified) keywords.add(Keyword.REIFIED)
            this.variance.keyword?.let { keywords.add(it) }
            return keywords
        }

    private val FunctionFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            this.visibility.keyword?.let { keywords.add(it) }
            this.modality.keyword?.let { keywords.add(it) }
            if (this.isOperator) keywords.add(Keyword.OPERATOR)
            if (this.isInfix) keywords.add(Keyword.INFIX)
            if (this.isInline) keywords.add(Keyword.INLINE)
            if (this.isTailrec) keywords.add(Keyword.TAILREC)
            if (this.isExternal) keywords.add(Keyword.EXTERNAL)
            if (this.isSuspend) keywords.add(Keyword.SUSPEND)
            if (this.isExpect) keywords.add(Keyword.EXPECT)
            return keywords
        }

    private val PropertyFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            this.visibility.keyword?.let { keywords.add(it) }
            this.modality.keyword?.let { keywords.add(it) }
            if (this.isExpect) keywords.add(Keyword.EXPECT)
            if (this.isLateinit) keywords.add(Keyword.LATEINIT)
            if (this.isConst) keywords.add(Keyword.CONST)
            if (this.isExternal) keywords.add(Keyword.EXTERNAL)
            if (this.isVar) keywords.add(Keyword.VAR)
            else keywords.add(Keyword.VAL)
            return keywords
        }

    private val LocalVarFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            if (this.isLateinit) keywords.add(Keyword.LATEINIT)
            if (this.isConst) keywords.add(Keyword.CONST)
            if (this.isVar) keywords.add(Keyword.VAR)
            else keywords.add(Keyword.VAL)
            return keywords
        }

    private val FieldFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            this.visibility.keyword?.let { keywords.add(it) }
            if (this.isStatic) keywords.add(Keyword.STATIC)
            if (this.isFinal) keywords.add(Keyword.VAL)
            else keywords.add(Keyword.VAR)
            return keywords
        }

    private val ClassFlags?.keywords: List<Keyword>
        get() {
            val keywords = mutableListOf<Keyword>()
            if (this == null) return keywords
            this.visibility.keyword?.let { keywords.add(it) }
            this.modality.keyword?.let { keywords.add(it) }
            if (this.isInner) keywords.add(Keyword.INNER)
            if (this.isExpect) keyword(Keyword.EXPECT)
            if (this.isExternal) keyword(Keyword.EXTERNAL)
            if (this.isData) keyword(Keyword.DATA)
            if (this.isValue) keyword(Keyword.VALUE)
            if (this.isFun) keyword(Keyword.FUN)
            if (this.isCompanion) keyword(Keyword.COMPANION)
            keywords.addAll(this.kind.keywords)
            return keywords
        }

    private val ClassKind.keywords: List<Keyword>
        get() {
            return when (this) {
                ClassKind.CLASS -> listOf(Keyword.CLASS)
                ClassKind.INTERFACE -> listOf(Keyword.INTERFACE)
                ClassKind.ENUM_CLASS -> listOf(Keyword.ENUM, Keyword.CLASS)
                ClassKind.ENUM_ENTRY -> emptyList()
                ClassKind.ANNOTATION_CLASS -> listOf(Keyword.ANNOTATION, Keyword.CLASS)
                ClassKind.OBJECT -> listOf(Keyword.OBJECT)
            }
        }

    private val Symbol.fqName: String
        get() {
            return when (val signature = signatures(this.signatureId)) {
                is CommonSignature -> buildString {
                    signature.packageFqNameIndexes.forEach {
                        append(strings(it))
                        append('.')
                    }
                    append(strings(signature.declarationFqNameIndexes[0]))
                }
                else -> ""
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

    private fun TypeOperator.mapKeyword(): Keyword? {
        return when (this) {
            TypeOperator.REINTERPRET_CAST,
            TypeOperator.CAST -> Keyword.AS
            TypeOperator.SAFE_CAST -> Keyword.SAFE_AS
            TypeOperator.INSTANCEOF -> Keyword.IS
            TypeOperator.NOT_INSTANCEOF -> Keyword.NOT_IS
            else -> null
        }
    }

    enum class StatementOrigin(val value: String?) {
        SAFE_CALL(null),
        UMINUS("-"),
        UPLUS("+"),
        EXCL("!"),
        EXCLEXCL("!!"),
        ELVIS("?:"),
        LT("<"),
        GT(">"),
        LTEQ("<="),
        GTEQ(">="),
        EQEQ("=="),
        EQEQEQ("==="),
        EXCLEQ("!="),
        EXCLEQEQ("!=="),
        IN("in"),
        NOT_IN("!in"),
        ANDAND("&&"),
        OROR("||"),
        PLUS("+"),
        MINUS("-"),
        MUL("*"),
        DIV("/"),
        PERC("%"),
        RANGE(".."),
        RANGE_UNTIL("until"),
        INVOKE(null),
        VARIABLE_AS_FUNCTION(null),
        GET_ARRAY_ELEMENT(null),
        PREFIX_INCR("++"),
        PREFIX_DECR("--"),
        POSTFIX_INCR("++"),
        POSTFIX_DECR("--"),
        EQ("="),
        PLUSEQ("+="),
        MINUSEQ("-="),
        MULTEQ("*="),
        DIVEQ("/="),
        PERCEQ("%="),
        ARGUMENTS_REORDERING_FOR_CALL(null),
        DESTRUCTURING_DECLARATION(null),
        GET_PROPERTY(null),
        GET_LOCAL_PROPERTY(null),
        IF(null),
        WHEN(null),
        WHEN_COMMA(null),
        WHILE_LOOP(null),
        DO_WHILE_LOOP(null),
        FOR_LOOP(null),
        FOR_LOOP_ITERATOR(null),
        FOR_LOOP_INNER_WHILE(null),
        FOR_LOOP_HAS_NEXT(null),
        FOR_LOOP_NEXT(null),
        LAMBDA(null),
        DEFAULT_VALUE(null),
        ANONYMOUS_FUNCTION(null),
        OBJECT_LITERAL(null),
        ADAPTED_FUNCTION_REFERENCE(null),
        SUSPEND_CONVERSION(null),
        FUN_INTERFACE_CONSTRUCTOR_REFERENCE(null),
        INITIALIZE_PROPERTY_FROM_PARAMETER(null),
        INITIALIZE_FIELD(null),
        PROPERTY_REFERENCE_FOR_DELEGATE(null),
        BRIDGE_DELEGATION(null),
        SYNTHETIC_NOT_AUTOBOXED_CHECK(null),
        PARTIAL_LINKAGE_RUNTIME_ERROR(null),
    }

    private fun SimpleType.isAny(): Boolean = isNotNullClassType("kotlin.Any")
    private fun SimpleType.isNullableAny(): Boolean = isNullableClassType("kotlin.Any")
    private fun SimpleType.isString(): Boolean = isNotNullClassType("kotlin.String")
    private fun SimpleType.isNullableString(): Boolean = isNullableClassType("kotlin.String")
    private fun SimpleType.isArray(): Boolean = isNotNullClassType("kotlin.Array")
    private fun SimpleType.isNullableArray(): Boolean = isNullableClassType("kotlin.Array")
    private fun SimpleType.isNothing(): Boolean = isNotNullClassType("kotlin.Nothing")
    private fun SimpleType.isNullableNothing(): Boolean = isNullableClassType("kotlin.Nothing")
    private fun SimpleType.isUnit() = isNotNullClassType("kotlin.Unit")
    private fun SimpleType.isBoolean(): Boolean = isNotNullClassType("kotlin.Boolean")
    private fun SimpleType.isChar(): Boolean = isNotNullClassType("kotlin.Char")
    private fun SimpleType.isByte(): Boolean = isNotNullClassType("kotlin.Byte")
    private fun SimpleType.isShort(): Boolean = isNotNullClassType("kotlin.Short")
    private fun SimpleType.isInt(): Boolean = isNotNullClassType("kotlin.Int")
    private fun SimpleType.isLong(): Boolean = isNotNullClassType("kotlin.Long")
    private fun SimpleType.isUByte(): Boolean = isNotNullClassType("kotlin.UByte")
    private fun SimpleType.isUShort(): Boolean = isNotNullClassType("kotlin.UShort")
    private fun SimpleType.isUInt(): Boolean = isNotNullClassType("kotlin.UInt")
    private fun SimpleType.isULong(): Boolean = isNotNullClassType("kotlin.ULong")
    private fun SimpleType.isFloat(): Boolean = isNotNullClassType("kotlin.Float")
    private fun SimpleType.isDouble(): Boolean = isNotNullClassType("kotlin.Double")
    private fun SimpleType.isNumber(): Boolean = isNotNullClassType("kotlin.Number")
    private fun SimpleType.isComparable(): Boolean = isNotNullClassType("kotlin.Comparable")
    private fun SimpleType.isCharSequence(): Boolean = isNotNullClassType("kotlin.CharSequence")
    private fun SimpleType.isIterable(): Boolean = isNotNullClassType("kotlin.collections.Iterable")
    private fun SimpleType.isCollection(): Boolean = isNotNullClassType("kotlin.collections.Collection")

    private fun SimpleType.isMarkedNullable(): Boolean {
        return this.nullability == SimpleType.Nullability.MARKED_NULLABLE
    }

    private fun SimpleType.isNotNullClassType(fqName: String): Boolean {
        return fqName == this.symbol.fqName && !this.isMarkedNullable()
    }

    private fun SimpleType.isNullableClassType(fqName: String): Boolean {
        return fqName == this.symbol.fqName && this.isMarkedNullable()
    }

    companion object {
        const val TAG_SOURCE_LOCATION = "SOURCE_LOCATION"
        const val TAG_DESCRIPTION = "DESCRIPTION"
        private val LINE_SEPARATOR: String = System.lineSeparator()
        private const val PRE_COMPOSE_IR_FQ_NAME = "com.decomposer.runtime.PreComposeIr"
        private const val POST_COMPOSE_IR_FQ_NAME = "com.decomposer.runtime.PostComposeIr"
    }
}

enum class Keyword(val visual: String) {
    THIS("this"),
    DO("do"),
    WHILE("while"),
    IF("if"),
    WHEN("when"),
    ELSE("else"),
    SAFE_AS("as?"),
    IS("is"),
    NOT_IS("!is"),
    AS("as"),
    THROW("throw"),
    TRY("try"),
    CATCH("catch"),
    FINALLY("finally"),
    CONTINUE("continue"),
    BREAK("break"),
    RETURN("return"),
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
    PACKAGE("package")
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
