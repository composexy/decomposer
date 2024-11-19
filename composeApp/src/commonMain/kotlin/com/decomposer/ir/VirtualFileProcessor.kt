package com.decomposer.ir

import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.runtime.ir.ClassOrFile
import com.decomposer.runtime.ir.FieldAccessCommon
import com.decomposer.runtime.ir.IrAnonymousInit
import com.decomposer.runtime.ir.IrBlock
import com.decomposer.runtime.ir.IrBlockBody
import com.decomposer.runtime.ir.IrBranch
import com.decomposer.runtime.ir.IrBreak
import com.decomposer.runtime.ir.IrCall
import com.decomposer.runtime.ir.IrCatch
import com.decomposer.runtime.ir.IrClass
import com.decomposer.runtime.ir.IrClassReference
import com.decomposer.runtime.ir.IrComposite
import com.decomposer.runtime.ir.IrConst
import com.decomposer.runtime.ir.IrConstructor
import com.decomposer.runtime.ir.IrConstructorCall
import com.decomposer.runtime.ir.IrContinue
import com.decomposer.runtime.ir.IrDeclaration
import com.decomposer.runtime.ir.IrDeclarationBase
import com.decomposer.runtime.ir.IrDelegatingConstructorCall
import com.decomposer.runtime.ir.IrDoWhile
import com.decomposer.runtime.ir.IrDynamicMemberExpression
import com.decomposer.runtime.ir.IrDynamicOperatorExpression
import com.decomposer.runtime.ir.IrDynamicOperatorExpression.IrDynamicOperator
import com.decomposer.runtime.ir.IrEnumConstructorCall
import com.decomposer.runtime.ir.IrEnumEntry
import com.decomposer.runtime.ir.IrErrorCallExpression
import com.decomposer.runtime.ir.IrErrorDeclaration
import com.decomposer.runtime.ir.IrErrorExpression
import com.decomposer.runtime.ir.IrExpression
import com.decomposer.runtime.ir.IrField
import com.decomposer.runtime.ir.IrFunction
import com.decomposer.runtime.ir.IrFunctionBase
import com.decomposer.runtime.ir.IrFunctionExpression
import com.decomposer.runtime.ir.IrFunctionReference
import com.decomposer.runtime.ir.IrGetClass
import com.decomposer.runtime.ir.IrGetEnumValue
import com.decomposer.runtime.ir.IrGetField
import com.decomposer.runtime.ir.IrGetObject
import com.decomposer.runtime.ir.IrGetValue
import com.decomposer.runtime.ir.IrInlineClassRepresentation
import com.decomposer.runtime.ir.IrInstanceInitializerCall
import com.decomposer.runtime.ir.IrLocalDelegatedProperty
import com.decomposer.runtime.ir.IrLocalDelegatedPropertyReference
import com.decomposer.runtime.ir.IrMultiFieldValueClassRepresentation
import com.decomposer.runtime.ir.IrProperty
import com.decomposer.runtime.ir.IrPropertyReference
import com.decomposer.runtime.ir.IrReturn
import com.decomposer.runtime.ir.IrSetField
import com.decomposer.runtime.ir.IrSetValue
import com.decomposer.runtime.ir.IrSimpleTypeNullability
import com.decomposer.runtime.ir.IrSpreadElement
import com.decomposer.runtime.ir.IrStatement
import com.decomposer.runtime.ir.IrStringConcat
import com.decomposer.runtime.ir.IrSyntheticBody
import com.decomposer.runtime.ir.IrSyntheticBodyKind
import com.decomposer.runtime.ir.IrThrow
import com.decomposer.runtime.ir.IrTry
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.IrTypeAbbreviation
import com.decomposer.runtime.ir.IrTypeAlias
import com.decomposer.runtime.ir.IrTypeOp
import com.decomposer.runtime.ir.IrTypeOperator
import com.decomposer.runtime.ir.IrTypeParameter
import com.decomposer.runtime.ir.IrValueParameter
import com.decomposer.runtime.ir.IrVararg
import com.decomposer.runtime.ir.IrVarargElement
import com.decomposer.runtime.ir.IrVariable
import com.decomposer.runtime.ir.IrWhen
import com.decomposer.runtime.ir.IrWhile
import com.decomposer.runtime.ir.MemberAccessCommon
import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinaryCoordinates
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinaryNameAndType
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinaryTypeProjection
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding
import com.decomposer.runtime.ir.Loop as IrLoop
import org.jetbrains.kotlin.backend.common.serialization.encodings.ClassFlags as KClassFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.FieldFlags as KFieldFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.FunctionFlags as KFunctionFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.LocalVariableFlags as KLocalVariableFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.PropertyFlags as KPropertyFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.TypeAliasFlags as KTypeAliasFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.TypeParameterFlags as KTypeParameterFlags
import org.jetbrains.kotlin.backend.common.serialization.encodings.ValueParameterFlags as KValueParameterFlags
import org.jetbrains.kotlin.descriptors.ClassKind as KClassKind
import org.jetbrains.kotlin.descriptors.Modality as KModality
import org.jetbrains.kotlin.types.Variance as KVariance

internal class TopLevelTable(
    internal val declarations : DeclarationTable,
    internal val types : TypeTable,
    internal val signatures : SignatureTable,
    internal val strings : StringTable,
    internal val bodies : BodyTable,
    internal val debugInfos : DebugInfoTable
)

internal class DeclarationTable {

}

internal class TypeTable {

}

internal class SignatureTable {

}

internal class StringTable(
    private val data: List<String>
)

internal class BodyTable {

}

internal class DebugInfoTable(
    private val data: List<String>
)

internal class KotlinFile(
    private val filePath: String,
    private val topLevelDeclarations : TopLevelTable,
    private val topLevelClasses : List<TopLevelTable>
)

internal class SimpleType(
    val symbol: Symbol,
    val nullability: Nullability,
    val annotations: List<ConstructorCall>,
    val abbreviation: Abbreviation?,
    val arguments: List<TypeArgument>
) {

    enum class Nullability {
        MARKED_NULLABLE,
        NOT_SPECIFIED,
        DEFINITELY_NOT_NULL
    }
}

internal class MemberAccess(
    val extensionReceiver: Expression?,
    val dispatchReceiver: Expression?,
    val typeArgumentIndexes: List<Int>,
    val valueArguments: List<Expression?>
)

internal class Abbreviation(
    val annotations: List<ConstructorCall>,
    val typeAlias: Symbol,
    val hasQuestionMark: Boolean,
    val arguments: List<TypeArgument>
)

internal class Expression(
    val operation: Operation,
    val typeIndex: Int,
    val coordinate: Coordinate
) : StatementBase, VarargElement

internal class Coordinate(
    val startOffset: Int,
    val endOffset: Int
)

internal sealed interface Operation

internal class Block(
    val statements: List<Statement>,
    val originalNameIndex: Int?
) : Operation

internal class Break(
    val loopIndex: Int,
    val labelIndex: Int?
) : Operation

internal class Call(
    val symbol: Symbol,
    val memberAccess: MemberAccess,
    val superSymbol: Symbol?,
    val originalNameIndex: Int?
) : Operation

internal class ClassReference(
    val classSymbol: Symbol,
    val classTypeIndex: Int
) : Operation

internal class Composite(
    val statements: List<Statement>,
    val originalNameIndex: Int?
) : Operation

internal sealed interface Const : Operation

internal data object NullConst : Const

internal class BooleanConst(val value: Boolean) : Const

internal class CharConst(val value: Char) : Const

internal class ByteConst(val value: Byte) : Const

internal class ShortConst(val value: Short) : Const

internal class IntConst(val value: Int) : Const

internal class LongConst(val value: Long) : Const

internal class FloatConst(val value: Float) : Const

internal class DoubleConst(val value: Double) : Const

internal class StringConst(val valueIndex: Int) : Const

internal class Continue(
    val loopIndex: Int,
    val labelIndex: Int?
) : Operation

internal class DelegatingConstructorCall(
    val symbol: Symbol,
    val memberAccess: MemberAccess
) : Operation

internal class DoWhile(
    val loop: Loop
) : Operation

internal class EnumConstructorCall(
    val symbol: Symbol,
    val memberAccess: MemberAccess
) : Operation

internal class FunctionReference(
    val symbol: Symbol,
    val originNameIndex: Int?,
    val memberAccess: MemberAccess,
    val reflectionTargetSymbol: Symbol?
) : Operation

internal class GetClass(
    val argument: Expression
) : Operation

internal class GetEnumValue(
    val symbol: Symbol
) : Operation

internal class GetField(
    val fieldAccess: FieldAccess,
    val originalNameIndex: Int?
) : Operation

internal class FieldAccess(
    val symbol: Symbol,
    val superSymbol: Symbol?,
    val receiver: Expression?
)

internal class GetObject(
    val symbol: Symbol
) : Operation

internal class GetValue(
    val symbol: Symbol,
    val originalNameIndex: Int?
) : Operation

internal class InstanceInitializerCall(
    val symbol: Symbol
) : Operation

internal class PropertyReference(
    val fieldSymbol: Symbol?,
    val getterSymbol: Symbol?,
    val setterSymbol: Symbol?,
    val originalNameIndex: Int?,
    val memberAccess: MemberAccess,
    val symbol: Symbol
) : Operation

internal class Return(
    val returnTargetSymbol: Symbol,
    val value: Expression
) : Operation

internal class SetField(
    val fieldAccess: FieldAccess,
    val value: Expression,
    val originalNameIndex: Int?
) : Operation

internal class SetValue(
    val symbol: Symbol,
    val value: Expression,
    val originalNameIndex: Int?
) : Operation

internal class StringConcat(
    val arguments: List<Expression>
) : Operation

internal class Throw(
    val value: Expression
) : Operation

internal class Try(
    val result: Expression,
    val catch: List<Statement>,
    val finally: Expression?
) : Operation

internal class TypeOp(
    val operator: TypeOperator,
    val operandIndex: Int,
    val argument: Expression,
) : Operation

enum class TypeOperator {
    CAST,
    IMPLICIT_CAST,
    IMPLICIT_NOTNULL,
    IMPLICIT_COERCION_TO_UNIT,
    IMPLICIT_INTEGER_COERCION,
    SAFE_CAST,
    INSTANCEOF,
    NOT_INSTANCEOF,
    SAM_CONVERSION,
    IMPLICIT_DYNAMIC_CAST,
    REINTERPRET_CAST
}

internal class Vararg(
    val elementTypeIndex: Int,
    val elements: List<VarargElement>
) : Operation

internal sealed interface VarargElement

internal class SpreadElement(
    val expression: Expression,
    val coordinate: Coordinate
) : VarargElement

internal class When(
    val branches: List<Statement>,
    val originalNameIndex: Int?
) : Operation

internal class While(
    val loop: Loop
) : Operation

internal class DynamicMemberExpression(
    val memberNameIndex: Int?,
    val receiver: Expression
) : Operation

internal class DynamicOperatorExpression(
    val operator: DynamicOperator,
    val receiver: Expression,
    val argument: List<Expression>
) : Operation

enum class DynamicOperator {
    UNARY_PLUS,
    UNARY_MINUS,
    EXCL,
    PREFIX_INCREMENT,
    POSTFIX_INCREMENT,
    PREFIX_DECREMENT,
    POSTFIX_DECREMENT,
    BINARY_PLUS,
    BINARY_MINUS,
    MUL,
    DIV,
    MOD,
    GT,
    LT,
    GE,
    LE,
    EQEQ,
    EXCLEQ,
    EQEQEQ,
    EXCLEQEQ,
    ANDAND,
    OROR,
    EQ,
    PLUSEQ,
    MINUSEQ,
    MULEQ,
    DIVEQ,
    MODEQ,
    ARRAY_ACCESS,
    INVOKE
}

internal class LocalDelegatedPropertyReference(
    val delegateSymbol: Symbol,
    val getterSymbol: Symbol?,
    val setterSymbol: Symbol?,
    val symbol: Symbol,
    val originalNameIndex: Int?
) : Operation

internal class ConstructorCall(
    val symbol: Symbol,
    val typeArgumentCount: Int,
    val memberAccess: MemberAccess,
    val originNameIndex: Int?
) : Operation

internal class FunctionExpression(
    val function: Function,
    val originNameIndex: Int?
) : Operation

internal class ErrorExpression(
    val descriptionIndex: Int
) : Operation

internal class ErrorCallExpression(
    val descriptionIndex: Int,
    val receiver: Expression?,
    val valueArguments: List<Expression>
) : Operation

internal class Statement(
    val coordinate: Coordinate,
    val statement: StatementBase
)

internal sealed interface StatementBase

internal interface Declaration : StatementBase

internal class DeclarationBase(
    val symbol: Symbol,
    val originalNameIndex: Int,
    val coordinate: Coordinate,
    val flags: Flags?,
    val annotations: List<ConstructorCall>
)

internal sealed interface Flags

internal class ClassFlags(
    val modality: Modality,
    val visibility: Visibility,
    val kind: ClassKind,
    val isCompanion: Boolean,
    val isInner: Boolean,
    val isData: Boolean,
    val isValue: Boolean,
    val isExpect: Boolean,
    val isExternal: Boolean,
    val isFun: Boolean,
    val hasEnumEntries: Boolean
) : Flags

internal enum class Modality {
    FINAL,
    SEALED,
    OPEN,
    ABSTRACT
}

internal enum class Visibility {
    PRIVATE,
    PRIVATE_TO_THIS,
    PROTECTED,
    INTERNAL,
    PUBLIC,
    LOCAL,
    INHERITED,
    INVISIBLE_FAKE,
    UNKNOWN
}

internal enum class ClassKind {
    CLASS,
    INTERFACE,
    ENUM_CLASS,
    ENUM_ENTRY,
    ANNOTATION_CLASS,
    OBJECT
}

internal class FunctionFlags(
    val modality: Modality,
    val visibility: Visibility,
    val isOperator: Boolean,
    val isInfix: Boolean,
    val isInline: Boolean,
    val isTailrec: Boolean,
    val isExternal: Boolean,
    val isSuspend: Boolean,
    val isExpect: Boolean,
    val isFakeOverride: Boolean,
) : Flags

internal class PropertyFlags(
    val modality: Modality,
    val visibility: Visibility,
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean,
    val isExternal: Boolean,
    val isDelegated: Boolean,
    val isExpect: Boolean,
    val isFakeOverride: Boolean
) : Flags

internal class ValueParameterFlags(
    val isCrossInline: Boolean,
    val isNoInline: Boolean,
    val isHidden: Boolean,
    val isAssignable: Boolean
) : Flags

internal class TypeAliasFlags(
    val visibility: Visibility,
    val isActual: Boolean
) : Flags

internal class TypeParameterFlags(
    val variance: Variance,
    val isReified: Boolean
) : Flags

internal class FieldFlags(
    val visibility: Visibility,
    val isFinal: Boolean,
    val isExternal: Boolean,
    val isStatic: Boolean
) : Flags

internal class LocalVarFlags(
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean
) : Flags

internal class AnonymousInit(
    val base: DeclarationBase,
    val bodyIndex: Int?
) : Declaration

internal class Class(
    val base: DeclarationBase,
    val nameIndex: Int,
    val thisReceiver: ValueParameter?,
    val typeParameters: List<TypeParameter>,
    val declarations: List<Declaration>,
    val superTypeIndexes: List<Int>,
    val inlineClassRepresentation: InlineClassRepresentation?,
    val multiFieldValueClassRepresentation: MultiFieldValueClassRepresentation?
) : Declaration

internal class InlineClassRepresentation(
    val underlyingPropertyNameIndex: Int,
    val underlyingPropertyTypeIndex: Int
)

internal class MultiFieldValueClassRepresentation(
    val underlyingPropertyNameIndexes: List<Int>,
    val underlyingPropertyTypeIndexes: List<Int>
)

internal class Constructor(
    val base: FunctionBase
) : Declaration

internal class FunctionBase(
    val base: DeclarationBase,
    val nameIndex: Int,
    val typeIndex: Int,
    val typeParameters: List<TypeParameter>,
    val dispatchReceiver: ValueParameter?,
    val extensionReceiver: ValueParameter?,
    val contextReceiverParametersCount: Int?,
    val valueParameterIndexes: List<ValueParameter>,
    val bodyIndex: Int?
)

internal class EnumEntry(
    val base: DeclarationBase,
    val nameIndex: Int,
    val initializerIndex: Int?,
    val correspondingClass: Class?
) : Declaration

internal class Field(
    val base: DeclarationBase,
    val nameIndex: Int,
    val typeIndex: Int,
    val initializerIndex: Int?
) : Declaration

internal class Function(
    val base: FunctionBase,
    val overriden: List<Symbol>
) : Declaration

internal class Property(
    val base: DeclarationBase,
    val nameIndex: Int,
    val backingField: Field?,
    val getter: Function?,
    val setter: Function?
) : Declaration

internal class TypeParameter(
    val base: DeclarationBase,
    val nameIndex: Int,
    val superTypeIndexes: List<Int>
) : Declaration

internal class Variable(
    val base: DeclarationBase,
    val nameIndex: Int,
    val typeIndex: Int,
    val initializer: Expression?
) : Declaration

internal class ValueParameter(
    val base: DeclarationBase,
    val nameIndex: Int,
    val typeIndex: Int,
    val varargElementTypeIndex: Int?,
    val defaultValueIndex: Int?
) : Declaration

internal class LocalDelegatedProperty(
    val base: DeclarationBase,
    val nameIndex: Int,
    val typeIndex: Int,
    val delegate: Variable?,
    val getter: Function?,
    val setter: Function?
) : Declaration

internal class TypeAlias(
    val base: DeclarationBase,
    val nameIndex: Int,
    val typeIndex: Int,
    val typeParameters: List<TypeParameter>
) : Declaration

internal class ErrorDeclaration(
    val coordinate: Coordinate
) : Declaration

internal class BlockBody(
    val statements: List<Statement>
) : StatementBase

internal class Branch(
    val condition: Expression,
    val result: Expression
) : StatementBase

internal class Catch(
    val catchParameter: Variable,
    val result: Expression
) : StatementBase

internal class SyntheticBody(
    val kind: SyntheticBodyKind
) : StatementBase

enum class SyntheticBodyKind {
    ENUM_VALUES,
    ENUM_VALUEOF,
    ENUM_ENTRIES
}

internal interface TypeArgument

internal object StarProjection : TypeArgument

internal class TypeProjection(
    val variance: Variance,
    val typeIndex: Int
) : TypeArgument

enum class Variance {
    INVARIANT,
    IN_VARIANCE,
    OUT_VARIANCE
}

internal class Symbol(
    val kind: Kind,
    val signatureId: Int
) {
    enum class Kind {
        FUNCTION_SYMBOL,
        CONSTRUCTOR_SYMBOL,
        ENUM_ENTRY_SYMBOL,
        FIELD_SYMBOL,
        VALUE_PARAMETER_SYMBOL,
        RETURNABLE_BLOCK_SYMBOL,
        CLASS_SYMBOL,
        TYPE_PARAMETER_SYMBOL,
        VARIABLE_SYMBOL,
        ANONYMOUS_INIT_SYMBOL,
        STANDALONE_FIELD_SYMBOL,
        RECEIVER_PARAMETER_SYMBOL,
        PROPERTY_SYMBOL,
        LOCAL_DELEGATED_PROPERTY_SYMBOL,
        TYPEALIAS_SYMBOL,
        FILE_SYMBOL;
    }
}

internal class Loop(
    val loopId: Int,
    val condition: Expression,
    val labelIndex: Int?,
    val body: Expression?,
    val originalNameIndex: Int?
)

internal sealed interface IdSignature

internal data object FileSignature : IdSignature

internal class CommonIdSignature(
    val packageFqNameIndexes: List<Int>,
    val declarationFqNameIndexes: List<Int>,
    val memberUniqueIdIndex: Long?,
    val flags: Long?,
    val debugInfoIndex: Int?
) : IdSignature

internal class FileLocalSignature(
    val containerIdIndex: Int,
    val localIdIndex: Long,
    val debugInfoIndex: Int?
) : IdSignature

internal class AccessorIdSignature(
    val propertySignatureIndex: Int,
    val nameIndex: Int,
    val accessorHashIdIndex: Int,
    val flags: Long?,
    val debugInfoIndex: Int?
) : IdSignature

internal class ScopedLocalSignature(
    val scopedLocalSignatureIndex: Int
) : IdSignature

internal class CompositeSignature(
    val containerIdIndex: Int,
    val innerSignatureIndex: Int
) : IdSignature

internal class LocalSignature(
    val localFqNameIndexes: List<Int>,
    val localHash: Long?,
    val debugInfoIndex: Int?
) : IdSignature

internal class VirtualFileProcessor {
    private val processorScope = CoroutineScope(Dispatchers.Default)
    private val originalFilesByPath = mutableMapOf<String, KotlinFile>()
    private val composedFilesByPath = mutableMapOf<String, KotlinFile>()

    fun processVirtualFileIr(ir: VirtualFileIr) = processorScope.launch {
        if (ir.originalIrFile.isNotEmpty()) {
            processOriginalIrFile(ir.filePath, ir.originalIrFile)
        }
        ir.originalTopLevelIrClasses.forEach {
            processOriginalIrClass(ir.filePath, it)
        }
        if (ir.composedIrFile.isNotEmpty()) {
            processComposedIrFile(ir.filePath, ir.composedIrFile)
        }
        ir.composedTopLevelIrClasses.forEach {
            processComposedIrClass(ir.filePath, it)
        }
    }

    private fun processComposedIrFile(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
    }

    private fun processComposedIrClass(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val clazz = ClassOrFile.ADAPTER.decode(protoByteArray)
    }

    private fun processOriginalIrFile(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
    }

    private fun processOriginalIrClass(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val clazz = ClassOrFile.ADAPTER.decode(protoByteArray)
    }

    private fun parseType(type: IrType): SimpleType {
        val simpleType = type.simple ?: throw IllegalArgumentException("Unexpected type: $type")
        return SimpleType(
            symbol = parseSymbol(simpleType.classifier),
            nullability = parseNullability(simpleType.nullability),
            annotations = simpleType.annotation_.map { parseAnnotation(it) },
            abbreviation = simpleType.abbreviation?.let { parseAbbreviation(it) },
            arguments = simpleType.argument.map { parseArgument(it) }
        )
    }

    private fun parseAnnotation(annotation: IrConstructorCall): ConstructorCall {
        return ConstructorCall(
            symbol = parseSymbol(annotation.symbol),
            typeArgumentCount = annotation.constructor_type_arguments_count,
            memberAccess = parseMemberAccess(annotation.member_access),
            originNameIndex = annotation.origin_name
        )
    }

    private fun parseMemberAccess(call: MemberAccessCommon): MemberAccess {
        return MemberAccess(
            extensionReceiver = call.extension_receiver?.let { parseExpression(it) },
            dispatchReceiver = call.dispatch_receiver?.let { parseExpression(it) },
            typeArgumentIndexes = call.type_argument,
            valueArguments = call.value_argument.map {
                it.expression?.let { expression -> parseExpression(expression) }
            }
        )
    }

    private fun parseAbbreviation(abbreviation: IrTypeAbbreviation): Abbreviation {
        return Abbreviation(
            annotations = abbreviation.annotation_.map { parseAnnotation(it) },
            typeAlias = parseSymbol(abbreviation.type_alias),
            hasQuestionMark = abbreviation.has_question_mark,
            arguments = abbreviation.argument.map { parseArgument(it) }
        )
    }

    private fun parseArgument(argument: Long): TypeArgument {
        val typeProjectionData = BinaryTypeProjection.decode(argument)
        return if (typeProjectionData.isStarProjection) {
            StarProjection
        } else {
            return TypeProjection(
                variance = parseVariance(typeProjectionData.variance),
                typeIndex = typeProjectionData.typeIndex
            )
        }
    }

    private fun parseExpression(expression: IrExpression): Expression {
        val op = expression.operation
        val operation: Operation = when {
            op.block != null -> parseBlock(op.block!!)
            op.break_ != null -> parseBreak(op.break_!!)
            op.call != null -> parseCall(op.call!!)
            op.class_reference != null -> parseClassReference(op.class_reference!!)
            op.composite != null -> parseComposite(op.composite!!)
            op.const_ != null -> parseConst(op.const_!!)
            op.continue_ != null -> parseContinue(op.continue_!!)
            op.delegating_constructor_call != null -> {
                parseDelegatingConstructorCall(op.delegating_constructor_call!!)
            }
            op.do_while != null -> parseDoWhile(op.do_while!!)
            op.enum_constructor_call != null -> parseEnumConstructorCall(op.enum_constructor_call!!)
            op.function_reference != null -> parseFunctionReference(op.function_reference!!)
            op.get_class != null -> parseGetClass(op.get_class!!)
            op.get_enum_value != null -> parseGetEnumValue(op.get_enum_value!!)
            op.get_field != null -> parseGetField(op.get_field!!)
            op.get_object != null -> parseGetObject(op.get_object!!)
            op.get_value != null -> parseGetValue(op.get_value!!)
            op.instance_initializer_call != null -> {
                parseInstanceInitializerCall(op.instance_initializer_call!!)
            }
            op.property_reference != null -> parsePropertyReference(op.property_reference!!)
            op.return_ != null -> parseReturn(op.return_!!)
            op.set_field != null -> parseSetField(op.set_field!!)
            op.set_value != null -> parseSetValue(op.set_value!!)
            op.string_concat != null -> parseStringConcat(op.string_concat!!)
            op.throw_ != null -> parseThrow(op.throw_!!)
            op.try_ != null -> parseTry(op.try_!!)
            op.type_op != null -> parseTypeOp(op.type_op!!)
            op.vararg_ != null -> parseVararg(op.vararg_!!)
            op.when_ != null -> parseWhen(op.when_!!)
            op.while_ != null -> parseWhile(op.while_!!)
            op.dynamic_member != null -> parseDynamicMemberExpression(op.dynamic_member!!)
            op.dynamic_operator != null -> parseDynamicOperatorExpression(op.dynamic_operator!!)
            op.local_delegated_property_reference != null -> {
                parseLocalDelegatedPropertyReference(op.local_delegated_property_reference!!)
            }
            op.constructor_call != null -> parseConstructorCall(op.constructor_call!!)
            op.function_expression != null -> parseFunctionExpression(op.function_expression!!)
            op.error_expression != null -> parseErrorExpression(op.error_expression!!)
            op.error_call_expression != null -> parseErrorCallExpression(op.error_call_expression!!)
            else -> throw IllegalArgumentException("Unexpected expression operation: $op")
        }
        return Expression(
            typeIndex = expression.type,
            coordinate = parseCoordinates(expression.coordinates),
            operation = operation
        )
    }

    private fun parseErrorCallExpression(expression: IrErrorCallExpression): ErrorCallExpression {
        return ErrorCallExpression(
            descriptionIndex = expression.description,
            receiver = expression.receiver_?.let { parseExpression(it) },
            valueArguments = expression.value_argument.map { parseExpression(it) }
        )
    }

    private fun parseErrorExpression(expression: IrErrorExpression): ErrorExpression {
        return ErrorExpression(
            descriptionIndex = expression.description
        )
    }

    private fun parseFunctionExpression(expression: IrFunctionExpression): FunctionExpression {
        return FunctionExpression(
            function = parseFunction(expression.function),
            originNameIndex = expression.origin_name
        )
    }

    private fun parseConstructorCall(expression: IrConstructorCall): ConstructorCall {
        return ConstructorCall(
            symbol = parseSymbol(expression.symbol),
            typeArgumentCount = expression.constructor_type_arguments_count,
            memberAccess = parseMemberAccess(expression.member_access),
            originNameIndex = expression.origin_name
        )
    }

    private fun parseLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference
    ): LocalDelegatedPropertyReference {
        return LocalDelegatedPropertyReference(
            delegateSymbol = parseSymbol(expression.delegate_),
            getterSymbol = expression.getter?.let { parseSymbol(it) },
            setterSymbol = expression.setter?.let { parseSymbol(it) },
            symbol = parseSymbol(expression.symbol),
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression
    ): DynamicOperatorExpression {
        val operator = when (expression.operator_) {
            IrDynamicOperator.UNARY_PLUS -> DynamicOperator.UNARY_PLUS
            IrDynamicOperator.UNARY_MINUS -> DynamicOperator.UNARY_MINUS
            IrDynamicOperator.EXCL -> DynamicOperator.EXCL
            IrDynamicOperator.PREFIX_INCREMENT -> DynamicOperator.PREFIX_INCREMENT
            IrDynamicOperator.POSTFIX_INCREMENT -> DynamicOperator.POSTFIX_INCREMENT
            IrDynamicOperator.PREFIX_DECREMENT -> DynamicOperator.PREFIX_DECREMENT
            IrDynamicOperator.POSTFIX_DECREMENT -> DynamicOperator.POSTFIX_DECREMENT
            IrDynamicOperator.BINARY_PLUS -> DynamicOperator.BINARY_PLUS
            IrDynamicOperator.BINARY_MINUS -> DynamicOperator.BINARY_MINUS
            IrDynamicOperator.MUL -> DynamicOperator.MUL
            IrDynamicOperator.DIV -> DynamicOperator.DIV
            IrDynamicOperator.MOD -> DynamicOperator.MOD
            IrDynamicOperator.GT -> DynamicOperator.GT
            IrDynamicOperator.LT -> DynamicOperator.LT
            IrDynamicOperator.GE -> DynamicOperator.GE
            IrDynamicOperator.LE -> DynamicOperator.LE
            IrDynamicOperator.EQEQ -> DynamicOperator.EQEQ
            IrDynamicOperator.EXCLEQ -> DynamicOperator.EXCLEQ
            IrDynamicOperator.EQEQEQ -> DynamicOperator.EQEQEQ
            IrDynamicOperator.EXCLEQEQ -> DynamicOperator.EXCLEQEQ
            IrDynamicOperator.ANDAND -> DynamicOperator.ANDAND
            IrDynamicOperator.OROR -> DynamicOperator.OROR
            IrDynamicOperator.EQ -> DynamicOperator.EQ
            IrDynamicOperator.PLUSEQ -> DynamicOperator.PLUSEQ
            IrDynamicOperator.MINUSEQ -> DynamicOperator.MINUSEQ
            IrDynamicOperator.MULEQ -> DynamicOperator.MULEQ
            IrDynamicOperator.DIVEQ -> DynamicOperator.DIVEQ
            IrDynamicOperator.MODEQ -> DynamicOperator.MODEQ
            IrDynamicOperator.ARRAY_ACCESS -> DynamicOperator.ARRAY_ACCESS
            IrDynamicOperator.INVOKE -> DynamicOperator.INVOKE
        }
        return DynamicOperatorExpression(
            operator = operator,
            receiver = parseExpression(expression.receiver_),
            argument = expression.argument.map { parseExpression(it) }
        )
    }

    private fun parseDynamicMemberExpression(
        expression: IrDynamicMemberExpression
    ): DynamicMemberExpression {
        return DynamicMemberExpression(
            memberNameIndex = expression.member_name,
            receiver = parseExpression(expression.receiver_)
        )
    }

    private fun parseWhile(expression: IrWhile): While {
        return While(loop = parseLoop(expression.loop))
    }

    private fun parseWhen(expression: IrWhen): When {
        return When(
            branches = expression.branch.map { parseStatement(it) },
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseVararg(expression: IrVararg): Vararg {
        return Vararg(
            elementTypeIndex = expression.element_type,
            elements = expression.element.map { parseVarargElement(it) }
        )
    }

    private fun parseVarargElement(varargElement: IrVarargElement): VarargElement {
        return when {
            varargElement.expression != null -> parseExpression(varargElement.expression!!)
            varargElement.spread_element != null -> parseSpreadElement(varargElement.spread_element!!)
            else -> throw IllegalArgumentException("Unexpected vararg element type: $varargElement")
        }
    }

    private fun parseSpreadElement(spreadElement: IrSpreadElement): SpreadElement {
        return SpreadElement(
            expression = parseExpression(spreadElement.expression),
            coordinate = parseCoordinates(spreadElement.coordinates)
        )
    }

    private fun parseTypeOp(expression: IrTypeOp): TypeOp {
        val operator = when (expression.operator_) {
            IrTypeOperator.CAST -> TypeOperator.CAST
            IrTypeOperator.IMPLICIT_CAST -> TypeOperator.IMPLICIT_CAST
            IrTypeOperator.IMPLICIT_NOTNULL -> TypeOperator.IMPLICIT_NOTNULL
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT -> TypeOperator.IMPLICIT_COERCION_TO_UNIT
            IrTypeOperator.IMPLICIT_INTEGER_COERCION -> TypeOperator.IMPLICIT_INTEGER_COERCION
            IrTypeOperator.SAFE_CAST -> TypeOperator.SAFE_CAST
            IrTypeOperator.INSTANCEOF -> TypeOperator.INSTANCEOF
            IrTypeOperator.NOT_INSTANCEOF -> TypeOperator.NOT_INSTANCEOF
            IrTypeOperator.SAM_CONVERSION -> TypeOperator.SAM_CONVERSION
            IrTypeOperator.IMPLICIT_DYNAMIC_CAST -> TypeOperator.IMPLICIT_DYNAMIC_CAST
            IrTypeOperator.REINTERPRET_CAST -> TypeOperator.REINTERPRET_CAST
        }
        return TypeOp(
            operator = operator,
            operandIndex = expression.operand,
            argument = parseExpression(expression.argument),
        )
    }

    private fun parseTry(expression: IrTry): Try {
        return Try(
            result = parseExpression(expression.result),
            catch = expression.catch_.map { parseStatement(it) },
            finally = expression.finally_?.let { parseExpression(it) }
        )
    }

    private fun parseThrow(expression: IrThrow): Throw {
        return Throw(
            value = parseExpression(expression.value_)
        )
    }

    private fun parseStringConcat(expression: IrStringConcat): StringConcat {
        return StringConcat(
            arguments = expression.argument.map { parseExpression(it) }
        )
    }

    private fun parseSetValue(expression: IrSetValue): SetValue {
        return SetValue(
            symbol = parseSymbol(expression.symbol),
            value = parseExpression(expression.value_),
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseSetField(expression: IrSetField): SetField {
        return SetField(
            fieldAccess = parseFieldAccess(expression.field_access),
            value = parseExpression(expression.value_),
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseInstanceInitializerCall(
        expression: IrInstanceInitializerCall
    ): InstanceInitializerCall {
        return InstanceInitializerCall(
            symbol = parseSymbol(expression.symbol)
        )
    }

    private fun parseReturn(expression: IrReturn): Return {
        return Return(
            returnTargetSymbol = parseSymbol(expression.return_target),
            value = parseExpression(expression.value_)
        )
    }

    private fun parseGetValue(expression: IrGetValue): GetValue {
        return GetValue(
            symbol = parseSymbol(expression.symbol),
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseGetObject(expression: IrGetObject): GetObject {
        return GetObject(
            symbol = parseSymbol(expression.symbol)
        )
    }

    private fun parsePropertyReference(expression: IrPropertyReference): PropertyReference {
        return PropertyReference(
            fieldSymbol = expression.field_?.let { parseSymbol(it) },
            getterSymbol = expression.getter?.let { parseSymbol(it) },
            setterSymbol = expression.setter?.let { parseSymbol(it) },
            originalNameIndex = expression.origin_name,
            memberAccess = parseMemberAccess(expression.member_access),
            symbol = parseSymbol(expression.symbol)
        )
    }

    private fun parseGetField(expression: IrGetField): GetField {
        return GetField(
            fieldAccess = parseFieldAccess(expression.field_access),
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseFieldAccess(fieldAccess: FieldAccessCommon): FieldAccess {
        return FieldAccess(
            symbol = parseSymbol(fieldAccess.symbol),
            superSymbol = fieldAccess.super_?.let { parseSymbol(it) },
            receiver = fieldAccess.receiver_?.let { parseExpression(it) }
        )
    }

    private fun parseGetEnumValue(expression: IrGetEnumValue): GetEnumValue {
        return GetEnumValue(
            symbol = parseSymbol(expression.symbol)
        )
    }

    private fun parseGetClass(expression: IrGetClass): GetClass {
        return GetClass(
            argument = parseExpression(expression.argument)
        )
    }

    private fun parseFunctionReference(expression: IrFunctionReference): FunctionReference {
        return FunctionReference(
            symbol = parseSymbol(expression.symbol),
            originNameIndex = expression.origin_name,
            memberAccess = parseMemberAccess(expression.member_access),
            reflectionTargetSymbol = expression.reflection_target_symbol?.let { parseSymbol(it) }
        )
    }

    private fun parseEnumConstructorCall(expression: IrEnumConstructorCall): EnumConstructorCall {
        return EnumConstructorCall(
            symbol = parseSymbol(expression.symbol),
            memberAccess = parseMemberAccess(expression.member_access)
        )
    }

    private fun parseDoWhile(expression: IrDoWhile): DoWhile {
        return DoWhile(loop = parseLoop(expression.loop))
    }

    private fun parseLoop(loop: IrLoop): Loop {
        return Loop(
            loopId = loop.loop_id,
            condition = parseExpression(loop.condition),
            labelIndex = loop.label,
            body = loop.body?.let { parseExpression(it) },
            originalNameIndex = loop.origin_name
        )
    }

    private fun parseDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall
    ): DelegatingConstructorCall {
        return DelegatingConstructorCall(
            symbol = parseSymbol(expression.symbol),
            memberAccess = parseMemberAccess(expression.member_access)
        )
    }

    private fun parseContinue(expression: IrContinue): Continue {
        return Continue(
            loopIndex = expression.loop_id,
            labelIndex = expression.label
        )
    }

    private fun parseConst(expression: IrConst): Const {
        return when {
            expression.null_ != null -> NullConst
            expression.boolean != null -> BooleanConst(expression.boolean!!)
            expression.char != null -> CharConst(expression.char!!.toChar())
            expression.byte != null -> ByteConst(expression.byte!!.toByte())
            expression.short != null -> ShortConst(expression.short!!.toShort())
            expression.int != null -> IntConst(expression.int!!)
            expression.long != null -> LongConst(expression.long!!)
            expression.float_bits != null -> FloatConst(Float.fromBits(expression.float_bits!!))
            expression.double_bits != null -> DoubleConst(Double.fromBits(expression.double_bits!!))
            expression.string != null -> StringConst(expression.string!!)
            else -> throw IllegalArgumentException("Unexpected const expression: $expression")
        }
    }

    private fun parseComposite(expression: IrComposite): Composite {
        return Composite(
            statements = expression.statement.map { parseStatement(it) },
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseClassReference(expression: IrClassReference): ClassReference {
        return ClassReference(
            classSymbol = parseSymbol(expression.class_symbol),
            classTypeIndex = expression.class_type
        )
    }

    private fun parseCall(expression: IrCall): Call {
        return Call(
            symbol = parseSymbol(expression.symbol),
            memberAccess = parseMemberAccess(expression.member_access),
            superSymbol = expression.super_?.let { parseSymbol(it) },
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseBreak(expression: IrBreak): Break {
        return Break(
            loopIndex = expression.loop_id,
            labelIndex = expression.label
        )
    }

    private fun parseBlock(expression: IrBlock): Block {
        return Block(
            statements = expression.statement.map { parseStatement(it) },
            originalNameIndex = expression.origin_name
        )
    }

    private fun parseStatement(statement: IrStatement): Statement {
        return Statement(
            coordinate = parseCoordinates(statement.coordinates),
            statement = when {
                statement.declaration != null -> parseDeclaration(statement.declaration!!)
                statement.expression != null -> parseExpression(statement.expression!!)
                statement.block_body != null -> parseBlockBody(statement.block_body!!)
                statement.branch != null -> parseBranch(statement.branch!!)
                statement.catch_ != null -> parseCatch(statement.catch_!!)
                statement.synthetic_body != null -> parseSyntheticBody(statement.synthetic_body!!)
                else -> throw IllegalArgumentException("Unexpected state type: $statement")
            }
        )
    }

    private fun parseSyntheticBody(statement: IrSyntheticBody): SyntheticBody {
        return SyntheticBody(
            kind = when (statement.kind) {
                IrSyntheticBodyKind.ENUM_VALUES -> SyntheticBodyKind.ENUM_VALUES
                IrSyntheticBodyKind.ENUM_VALUEOF -> SyntheticBodyKind.ENUM_VALUEOF
                IrSyntheticBodyKind.ENUM_ENTRIES -> SyntheticBodyKind.ENUM_ENTRIES
            }
        )
    }

    private fun parseCatch(statement: IrCatch): Catch {
        return Catch(
            catchParameter = parseVariable(statement.catch_parameter),
            result = parseExpression(statement.result)
        )
    }

    private fun parseVariable(irVariable: IrVariable): Variable {
        val nameTypeData = BinaryNameAndType.decode(irVariable.name_type)
        return Variable(
            base = parseDeclarationBase(
                irVariable.base,
                irVariable.base.flags?.let { parseLocalVariableFlags(it) }
            ),
            nameIndex = nameTypeData.nameIndex,
            typeIndex = nameTypeData.typeIndex,
            initializer = irVariable.initializer?.let { parseExpression(it) }
        )
    }

    private fun parseDeclarationBase(base: IrDeclarationBase, flags: Flags?): DeclarationBase {
        return DeclarationBase(
            symbol = parseSymbol(base.symbol),
            originalNameIndex = base.origin_name,
            coordinate = parseCoordinates(base.coordinates),
            flags = flags,
            annotations = base.annotation_.map { parseConstructorCall(it) }
        )
    }

    private fun parseBranch(statement: IrBranch): Branch {
        return Branch(
            condition = parseExpression(statement.condition),
            result = parseExpression(statement.result)
        )
    }

    private fun parseBlockBody(statement: IrBlockBody): BlockBody {
        return BlockBody(
            statements = statement.statement.map { parseStatement(it) }
        )
    }

    private fun parseDeclaration(statement: IrDeclaration): Declaration {
        return when {
            statement.ir_anonymous_init != null -> parseAnonymousInit(statement.ir_anonymous_init!!)
            statement.ir_class != null -> parseClass(statement.ir_class!!)
            statement.ir_constructor != null -> parseConstructor(statement.ir_constructor!!)
            statement.ir_enum_entry != null -> parseEnumEntry(statement.ir_enum_entry!!)
            statement.ir_field != null -> parseField(statement.ir_field!!)
            statement.ir_function != null -> parseFunction(statement.ir_function!!)
            statement.ir_property != null -> parseProperty(statement.ir_property!!)
            statement.ir_type_parameter != null -> parseTypeParameter(statement.ir_type_parameter!!)
            statement.ir_variable != null -> parseVariable(statement.ir_variable!!)
            statement.ir_value_parameter != null -> {
                parseValueParameter(statement.ir_value_parameter!!)
            }
            statement.ir_local_delegated_property != null -> {
                parseLocalDelegatedProperty(statement.ir_local_delegated_property!!)
            }
            statement.ir_type_alias != null -> parseTypeAlias(statement.ir_type_alias!!)
            statement.ir_error_declaration != null -> {
                parseErrorDeclaration(statement.ir_error_declaration!!)
            }
            else -> throw IllegalArgumentException("Unexpected declaration type: $statement")
        }
    }

    private fun parseTypeAlias(irTypeAlias: IrTypeAlias): TypeAlias {
        val nameTypeData = BinaryNameAndType.decode(irTypeAlias.name_type)
        return TypeAlias(
            base = parseDeclarationBase(
                irTypeAlias.base,
                irTypeAlias.base.flags?.let { parseTypeAliasFlags(it) }
            ),
            nameIndex = nameTypeData.nameIndex,
            typeIndex = nameTypeData.typeIndex,
            typeParameters = irTypeAlias.type_parameter.map { parseTypeParameter(it) }
        )
    }

    private fun parseTypeAliasFlags(encoded: Long): TypeAliasFlags {
        val flags = KTypeAliasFlags.decode(encoded)
        return TypeAliasFlags(
            visibility = parseVisibility(flags.visibility),
            isActual = flags.isActual
        )
    }

    private fun parseVisibility(visibility: DescriptorVisibility): Visibility {
        return when (visibility.delegate) {
            is Visibilities.Private -> Visibility.PRIVATE
            is Visibilities.PrivateToThis -> Visibility.PRIVATE_TO_THIS
            is Visibilities.Protected -> Visibility.PROTECTED
            is Visibilities.Internal -> Visibility.INTERNAL
            is Visibilities.Public -> Visibility.PUBLIC
            is Visibilities.Local -> Visibility.LOCAL
            is Visibilities.Inherited -> Visibility.INHERITED
            is Visibilities.InvisibleFake -> Visibility.INVISIBLE_FAKE
            else -> Visibility.UNKNOWN
        }
    }

    private fun parseLocalDelegatedProperty(
        irLocalDelegatedProperty: IrLocalDelegatedProperty
    ): LocalDelegatedProperty {
        val nameTypeData = BinaryNameAndType.decode(irLocalDelegatedProperty.name_type)
        return LocalDelegatedProperty(
            base = parseDeclarationBase(
                irLocalDelegatedProperty.base,
                irLocalDelegatedProperty.base.flags?.let { parseLocalVariableFlags(it) }
            ),
            nameIndex = nameTypeData.nameIndex,
            typeIndex = nameTypeData.typeIndex,
            delegate = irLocalDelegatedProperty.delegate_?.let { parseVariable(it) },
            getter = irLocalDelegatedProperty.getter?.let { parseFunction(it) },
            setter = irLocalDelegatedProperty.setter?.let { parseFunction(it) }
        )
    }

    private fun parseLocalVariableFlags(encoded: Long): LocalVarFlags {
        val flags = KLocalVariableFlags.decode(encoded)
        return LocalVarFlags(
            isVar = flags.isVar,
            isConst = flags.isConst,
            isLateinit = flags.isLateinit
        )
    }

    private fun parseErrorDeclaration(irErrorDeclaration: IrErrorDeclaration): ErrorDeclaration {
        return ErrorDeclaration(
            coordinate = parseCoordinates(irErrorDeclaration.coordinates)
        )
    }

    private fun parseValueParameter(irValueParameter: IrValueParameter): ValueParameter {
        val nameTypeData = BinaryNameAndType.decode(irValueParameter.name_type)
        return ValueParameter(
            base = parseDeclarationBase(
                irValueParameter.base,
                irValueParameter.base.flags?.let { parseValueParameterFlags(it) }
            ),
            nameIndex = nameTypeData.nameIndex,
            typeIndex = nameTypeData.typeIndex,
            varargElementTypeIndex = irValueParameter.vararg_element_type,
            defaultValueIndex = irValueParameter.default_value
        )
    }

    private fun parseValueParameterFlags(encoded: Long): ValueParameterFlags {
        val flags = KValueParameterFlags.decode(encoded)
        return ValueParameterFlags(
            isCrossInline = flags.isCrossInline,
            isNoInline = flags.isNoInline,
            isHidden = flags.isHidden,
            isAssignable = flags.isAssignable
        )
    }

    private fun parseTypeParameter(irTypeParameter: IrTypeParameter): TypeParameter {
        return TypeParameter(
            base = parseDeclarationBase(
                irTypeParameter.base,
                irTypeParameter.base.flags?.let { parseTypeParameterFlags(it) }),
            nameIndex = irTypeParameter.name,
            superTypeIndexes = irTypeParameter.super_type
        )
    }

    private fun parseTypeParameterFlags(encoded: Long): TypeParameterFlags {
        val flags = KTypeParameterFlags.decode(encoded)
        return TypeParameterFlags(
            variance = parseVariance(flags.variance),
            isReified = flags.isReified
        )
    }

    private fun parseVariance(variance: KVariance): Variance {
        return when (variance) {
            KVariance.INVARIANT -> Variance.INVARIANT
            KVariance.IN_VARIANCE -> Variance.IN_VARIANCE
            KVariance.OUT_VARIANCE -> Variance.OUT_VARIANCE
        }
    }

    private fun parseProperty(irProperty: IrProperty): Property {
        return Property(
            base = parseDeclarationBase(
                irProperty.base,
                irProperty.base.flags?.let { parsePropertyFlags(it) }
            ),
            nameIndex = irProperty.name,
            backingField = irProperty.backing_field?.let { parseField(it) },
            getter = irProperty.getter?.let { parseFunction(it) },
            setter = irProperty.setter?.let { parseFunction(it) }
        )
    }

    private fun parsePropertyFlags(encoded: Long): PropertyFlags {
        val flags = KPropertyFlags.decode(encoded)
        return PropertyFlags(
            modality = parseModality(flags.modality),
            visibility = parseVisibility(flags.visibility),
            isVar = flags.isVar,
            isConst = flags.isConst,
            isLateinit = flags.isLateinit,
            isExternal = flags.isExternal,
            isDelegated = flags.isDelegated,
            isExpect = flags.isExpect,
            isFakeOverride = flags.isFakeOverride
        )
    }

    private fun parseModality(modality: KModality): Modality {
        return when (modality) {
            KModality.FINAL -> Modality.FINAL
            KModality.SEALED -> Modality.SEALED
            KModality.OPEN -> Modality.OPEN
            KModality.ABSTRACT -> Modality.ABSTRACT
        }
    }

    private fun parseFunction(irFunction: IrFunction): Function {
        return Function(
            base = parseFunctionBase(irFunction.base),
            overriden = irFunction.overridden.map { parseSymbol(it) }
        )
    }

    private fun parseFunctionBase(irFunctionBase: IrFunctionBase): FunctionBase {
        val nameTypeData = BinaryNameAndType.decode(irFunctionBase.name_type)
        return FunctionBase(
            base = parseDeclarationBase(
                irFunctionBase.base,
                irFunctionBase.base.flags?.let { parseFunctionFlags(it) }
            ),
            nameIndex = nameTypeData.nameIndex,
            typeIndex = nameTypeData.typeIndex,
            typeParameters = irFunctionBase.type_parameter.map { parseTypeParameter(it) },
            dispatchReceiver = irFunctionBase.dispatch_receiver?.let { parseValueParameter(it) },
            extensionReceiver = irFunctionBase.extension_receiver?.let { parseValueParameter(it) },
            contextReceiverParametersCount = irFunctionBase.context_receiver_parameters_count,
            valueParameterIndexes = irFunctionBase.value_parameter.map { parseValueParameter(it) },
            bodyIndex = irFunctionBase.body
        )
    }

    private fun parseFunctionFlags(encoded: Long): FunctionFlags {
        val flags = KFunctionFlags.decode(encoded)
        return FunctionFlags(
            modality = parseModality(flags.modality),
            visibility = parseVisibility(flags.visibility),
            isOperator = flags.isOperator,
            isInfix = flags.isInfix,
            isInline = flags.isInline,
            isTailrec = flags.isTailrec,
            isExternal = flags.isExternal,
            isSuspend = flags.isSuspend,
            isExpect = flags.isExpect,
            isFakeOverride = flags.isFakeOverride
        )
    }

    private fun parseField(irField: IrField): Field {
        val nameTypeData = BinaryNameAndType.decode(irField.name_type)
        return Field(
            base = parseDeclarationBase(
                irField.base,
                irField.base.flags?.let { parseFieldFlags(it) }
            ),
            nameIndex = nameTypeData.nameIndex,
            typeIndex = nameTypeData.typeIndex,
            initializerIndex = irField.initializer
        )
    }

    private fun parseFieldFlags(encoded: Long): FieldFlags {
        val flags = KFieldFlags.decode(encoded)
        return FieldFlags(
            visibility = parseVisibility(flags.visibility),
            isFinal = flags.isFinal,
            isExternal = flags.isExternal,
            isStatic = flags.isStatic
        )
    }

    private fun parseEnumEntry(irEnumEntry: IrEnumEntry): EnumEntry {
        return EnumEntry(
            base = parseDeclarationBase(irEnumEntry.base, null),
            nameIndex = irEnumEntry.name,
            initializerIndex = irEnumEntry.initializer,
            correspondingClass = irEnumEntry.corresponding_class?.let { parseClass(it) }
        )
    }

    private fun parseConstructor(irConstructor: IrConstructor): Constructor {
        return Constructor(
            base = parseFunctionBase(irConstructor.base)
        )
    }

    private fun parseClass(irClass: IrClass): Class {
        return Class(
            base = parseDeclarationBase(
                irClass.base,
                irClass.base.flags?.let { parseClassFlags(it) }
            ),
            nameIndex = irClass.name,
            thisReceiver = irClass.this_receiver?.let { parseValueParameter(it) },
            typeParameters = irClass.type_parameter.map { parseTypeParameter(it) },
            declarations = irClass.declaration.map { parseDeclaration(it) },
            superTypeIndexes = irClass.super_type,
            inlineClassRepresentation = irClass.inline_class_representation?.let {
                parseInlineClassRepresentation(it)
            },
            multiFieldValueClassRepresentation = irClass.multi_field_value_class_representation?.let {
                parseMultiFieldValueClassRepresentation(it)
            }
        )
    }

    private fun parseClassFlags(encoded: Long): ClassFlags {
        val flags = KClassFlags.decode(encoded)
        return ClassFlags(
            modality = parseModality(flags.modality),
            visibility = parseVisibility(flags.visibility),
            kind = parseClassKind(flags.kind),
            isCompanion = flags.isCompanion,
            isInner = flags.isInner,
            isData = flags.isData,
            isValue = flags.isValue,
            isExpect = flags.isExpect,
            isExternal = flags.isExternal,
            isFun = flags.isFun,
            hasEnumEntries = flags.hasEnumEntries
        )
    }

    private fun parseClassKind(kind: KClassKind): ClassKind {
        return when (kind) {
            KClassKind.CLASS -> ClassKind.CLASS
            KClassKind.INTERFACE -> ClassKind.INTERFACE
            KClassKind.ENUM_CLASS -> ClassKind.ENUM_CLASS
            KClassKind.ENUM_ENTRY -> ClassKind.ENUM_ENTRY
            KClassKind.ANNOTATION_CLASS -> ClassKind.ANNOTATION_CLASS
            KClassKind.OBJECT -> ClassKind.OBJECT
        }
    }

    private fun parseMultiFieldValueClassRepresentation(
        multiFieldValueClassRepresentation: IrMultiFieldValueClassRepresentation
    ): MultiFieldValueClassRepresentation {
        return MultiFieldValueClassRepresentation(
            underlyingPropertyNameIndexes = multiFieldValueClassRepresentation.underlying_property_name,
            underlyingPropertyTypeIndexes = multiFieldValueClassRepresentation.underlying_property_type
        )
    }

    private fun parseInlineClassRepresentation(
        inlineClassRepresentation: IrInlineClassRepresentation
    ): InlineClassRepresentation {
        return InlineClassRepresentation(
            underlyingPropertyNameIndex = inlineClassRepresentation.underlying_property_name,
            underlyingPropertyTypeIndex = inlineClassRepresentation.underlying_property_type
        )
    }

    private fun parseAnonymousInit(irAnonymousInit: IrAnonymousInit): AnonymousInit {
        return AnonymousInit(
            base = parseDeclarationBase(irAnonymousInit.base, null),
            bodyIndex = irAnonymousInit.body
        )
    }

    private fun parseCoordinates(coordinates: Long): Coordinate {
        val coordinatesData = BinaryCoordinates.decode(coordinates)
        return Coordinate(
            startOffset = coordinatesData.startOffset,
            endOffset = coordinatesData.endOffset
        )
    }

    private fun parseNullability(nullablity: IrSimpleTypeNullability?): SimpleType.Nullability {
        return when (nullablity) {
            IrSimpleTypeNullability.MARKED_NULLABLE -> SimpleType.Nullability.MARKED_NULLABLE
            IrSimpleTypeNullability.NOT_SPECIFIED -> SimpleType.Nullability.NOT_SPECIFIED
            IrSimpleTypeNullability.DEFINITELY_NOT_NULL -> SimpleType.Nullability.DEFINITELY_NOT_NULL
            null -> SimpleType.Nullability.NOT_SPECIFIED
        }
    }

    private fun parseSymbol(classifier: Long): Symbol {
        val symbolData = BinarySymbolData.decode(classifier)
        return Symbol(
            signatureId = symbolData.signatureId,
            kind = when (symbolData.kind) {
                BinarySymbolData.SymbolKind.FUNCTION_SYMBOL -> Symbol.Kind.FUNCTION_SYMBOL
                BinarySymbolData.SymbolKind.CONSTRUCTOR_SYMBOL -> Symbol.Kind.CONSTRUCTOR_SYMBOL
                BinarySymbolData.SymbolKind.ENUM_ENTRY_SYMBOL -> Symbol.Kind.ENUM_ENTRY_SYMBOL
                BinarySymbolData.SymbolKind.FIELD_SYMBOL -> Symbol.Kind.FIELD_SYMBOL
                BinarySymbolData.SymbolKind.VALUE_PARAMETER_SYMBOL -> Symbol.Kind.VALUE_PARAMETER_SYMBOL
                BinarySymbolData.SymbolKind.RETURNABLE_BLOCK_SYMBOL -> Symbol.Kind.RETURNABLE_BLOCK_SYMBOL
                BinarySymbolData.SymbolKind.CLASS_SYMBOL -> Symbol.Kind.CLASS_SYMBOL
                BinarySymbolData.SymbolKind.TYPE_PARAMETER_SYMBOL -> Symbol.Kind.TYPE_PARAMETER_SYMBOL
                BinarySymbolData.SymbolKind.VARIABLE_SYMBOL -> Symbol.Kind.VARIABLE_SYMBOL
                BinarySymbolData.SymbolKind.ANONYMOUS_INIT_SYMBOL -> Symbol.Kind.ANONYMOUS_INIT_SYMBOL
                BinarySymbolData.SymbolKind.STANDALONE_FIELD_SYMBOL -> Symbol.Kind.STANDALONE_FIELD_SYMBOL
                BinarySymbolData.SymbolKind.RECEIVER_PARAMETER_SYMBOL -> Symbol.Kind.RECEIVER_PARAMETER_SYMBOL
                BinarySymbolData.SymbolKind.PROPERTY_SYMBOL -> Symbol.Kind.PROPERTY_SYMBOL
                BinarySymbolData.SymbolKind.LOCAL_DELEGATED_PROPERTY_SYMBOL -> Symbol.Kind.LOCAL_DELEGATED_PROPERTY_SYMBOL
                BinarySymbolData.SymbolKind.TYPEALIAS_SYMBOL -> Symbol.Kind.TYPEALIAS_SYMBOL
                BinarySymbolData.SymbolKind.FILE_SYMBOL -> Symbol.Kind.FILE_SYMBOL
            }
        )
    }
}

private val jsonAdapter = run {
    val moshi = Moshi.Builder()
        .add(WireJsonAdapterFactory())
        .build()
    moshi.adapter(ClassOrFile::class.java)
}

private fun ClassOrFile.printJson() {
    val json = jsonAdapter.indent("  ").toJson(this)
    println(json)
}