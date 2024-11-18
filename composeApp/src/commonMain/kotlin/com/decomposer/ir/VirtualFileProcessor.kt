package com.decomposer.ir

import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.runtime.ir.ClassOrFile
import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding

internal class Tables {
    internal val declarationTable = DeclarationTable()
    internal val typeTable = TypeTable()
    internal val signatureTable = SignatureTable()
    internal val stringTable = StringTable()
    internal val bodyTable = BodyTable()
    internal val debugInfoTable = DebugInfoTable()
}

internal class DeclarationTable {

}

internal class TypeTable {

}

internal class SignatureTable {

}

internal class StringTable {
    private val table = mutableListOf<String>()

    fun addStrings(strings: List<String>) {
        table.addAll(strings)
    }
}

internal class BodyTable {

}

internal class DebugInfoTable {
    private val table = mutableListOf<String>()

    fun addStrings(strings: List<String>) {
        table.addAll(strings)
    }
}

internal class File(private val filePath: String) {
    private val file = Tables()
    private val topLevelClasses = mutableListOf<Tables>()

    fun addIrFile(fileIr: ClassOrFile) {
        file.stringTable.addStrings(fileIr.com_decomposer_runtime_ir_string)
        file.debugInfoTable.addStrings(fileIr.debug_info)
    }

    fun addIrClass(classIr: ClassOrFile) {
        val tables = Tables()
        tables.stringTable.addStrings(classIr.com_decomposer_runtime_ir_string)
        tables.debugInfoTable.addStrings(classIr.debug_info)
        topLevelClasses.add(tables)
    }
}

internal class SimpleType(
    val symbol: Symbol,
    val nullability: Nullability,
    val annotations: List<ConstructorCall>,
    val abbreviations: List<Abbreviation>,
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

internal abstract class Expression(
    val operation: Operation,
    val typeIndex: Int,
    val coordinate: Coordinate
) : BaseStatement, VarargElement

internal class Coordinate(
    val startOffset: Int,
    val endOffset: Int
)

internal sealed interface Operation

internal class Block(
    val statement: Statement,
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
    val originalNameIndex: String?
) : Operation

internal class ClassReference(
    val classSymbol: Symbol,
    val classTypeIndex: Int
) : Operation

internal class Composite(
    val statements: List<Statement>,
    val originalNameIndex: String?
) : Operation

internal sealed interface Const : Operation

internal object NullConst : Const

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
    val loopIndex: Int
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
    val originalNameIndex: String?
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
    val originalNameIndex: String?
) : Operation

internal class InstanceInitializerCall(
    val symbol: Symbol
) : Operation

internal class PropertyReference(
    val fieldSymbol: Symbol?,
    val getterSymbol: Symbol?,
    val setterSymbol: Symbol?,
    val originalNameIndex: String?,
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
    val originalNameIndex: String?
) : Operation

internal class SetValue(
    val symbol: Symbol,
    val value: Expression,
    val originalNameIndex: String?
) : Operation

internal class StringConcat(
    val arguments: List<Expression>
) : Operation

internal class Throw(
    val value: Expression
) : Operation

internal class Try(
    val result: Expression,
    val catch: List<Expression>,
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
    val originalNameIndex: String?
) : Operation

internal class While(
    val loopIndex: Int
) : Operation

internal class DynamicMemberExpression(
    val memberNameIndex: Int?,
    val receiver: Expression
) : Operation

internal class DynamicOperatorExpression(
    val operator: DynamicOperator,
    val receiver: Expression,
    val argument: List<Expression>
) : Operation {

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
}

internal class LocalDelegatedPropertyReference(
    val delegateSymbol: Symbol,
    val getterSymbol: Symbol?,
    val setterSymbol: Symbol?,
    val symbol: Symbol,
    val originalNameIndex: String?
): Operation

internal class ConstructorCall(
    val symbol: Symbol,
    val typeArgumentCount: Int,
    val memberAccess: MemberAccess,
    val originNameIndex: Int
)

internal class FunctionExpression(
    val symbol: Symbol,
    val originalNameIndex: String?,
    val memberAccess: MemberAccess,
    val reflectionTargetSymbol: Symbol?
) : Operation

internal class ErrorExpression(
    val descriptionIndex: Int
) : Operation

internal class ErrorCallExpression(
    val descriptionIndex: Int,
    val receiver: Expression?,
    val valueArguments: List<Expression>
) : Operation

internal abstract class Statement(
    val coordinate: Coordinate,
    val statement: BaseStatement
)

internal sealed interface BaseStatement

internal interface Declaration : BaseStatement

internal class DeclarationBase(
    val symbol: Symbol,
    val originalNameIndex: String,
    val coordinate: Coordinate,
    val flags: Flags,
    val annotations: List<ConstructorCall>
)

internal sealed interface Flags

internal class ClassFlag(
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
)

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
    val superSymbols: List<Symbol>,
    val inlineClassRepresentation: InlineClassRepresentation?,
    val multiFieldValueClassRepresentation: MultiFieldValueClassRepresentation?
) : Declaration

internal class InlineClassRepresentation(
    val underlyingPropertyNameIndex: Int,
    val underlyingPropertyTypeIndex: Int
)

internal class MultiFieldValueClassRepresentation(
    val underlyingPropertyNames: List<Int>,
    val underlyingPropertyTypes: List<Int>
)

internal class Constructor(
    val base: FunctionBase
) : Declaration

internal class FunctionBase(
    val base: DeclarationBase,
    val nameTypeIndex: Int,
    val typeParameters: List<TypeParameter>,
    val dispatchReceiver: ValueParameter?,
    val extensionReceiver: ValueParameter?,
    val contextReceiverParametersCount: Int?,
    val valueParameterIndexes: List<Int>,
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
    val nameTypeIndex: Int,
    val initializerIndex: Int?
) : Declaration

internal class Function(
    val base: FunctionBase,
    val overriden: Symbol
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
    val nameTypeIndex: Int,
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
) : BaseStatement

internal class Branch(
    val condition: Expression,
    val result: Expression
) : BaseStatement

internal class Catch(
    val catchParameter: Variable,
    val result: Expression
) : BaseStatement

internal class SyntheticBody(
    val kind: SyntheticBodyKind
) : BaseStatement

enum class SyntheticBodyKind {
    ENUM_VALUES,
    ENUM_VALUEOF,
    ENUM_ENTRIES
}

internal interface TypeArgument

internal class StarProjection : TypeArgument

internal class TypeProjection(
    val variance: Variance,
    val type: SimpleType
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
    private val originalFilesByPath = mutableMapOf<String, File>()
    private val composedFilesByPath = mutableMapOf<String, File>()

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
        composedFilesByPath.putIfAbsent(path, File(path))
        composedFilesByPath[path]!!.addIrFile(file)
    }

    private fun processComposedIrClass(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val clazz = ClassOrFile.ADAPTER.decode(protoByteArray)
        composedFilesByPath.putIfAbsent(path, File(path))
        composedFilesByPath[path]!!.addIrClass(clazz)
    }

    private fun processOriginalIrFile(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
        originalFilesByPath.putIfAbsent(path, File(path))
        originalFilesByPath[path]!!.addIrFile(file)
    }

    private fun processOriginalIrClass(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val clazz = ClassOrFile.ADAPTER.decode(protoByteArray)
        originalFilesByPath.putIfAbsent(path, File(path))
        originalFilesByPath[path]!!.addIrClass(clazz)
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