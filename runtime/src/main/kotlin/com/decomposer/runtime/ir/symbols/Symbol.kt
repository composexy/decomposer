package com.decomposer.runtime.ir.symbols

import com.decomposer.runtime.ir.declarations.AnonymousInitializer
import com.decomposer.runtime.ir.declarations.Class
import com.decomposer.runtime.ir.declarations.Constructor
import com.decomposer.runtime.ir.declarations.EnumEntry
import com.decomposer.runtime.ir.declarations.ExternalPackageFragment
import com.decomposer.runtime.ir.declarations.Field
import com.decomposer.runtime.ir.declarations.File
import com.decomposer.runtime.ir.declarations.Function
import com.decomposer.runtime.ir.declarations.LocalDelegatedProperty
import com.decomposer.runtime.ir.declarations.Property
import com.decomposer.runtime.ir.declarations.ReturnTarget
import com.decomposer.runtime.ir.declarations.SimpleFunction
import com.decomposer.runtime.ir.declarations.SymbolOwner
import com.decomposer.runtime.ir.declarations.TypeAlias
import com.decomposer.runtime.ir.declarations.TypeParameter
import com.decomposer.runtime.ir.declarations.ValueDeclaration
import com.decomposer.runtime.ir.expressions.ReturnableBlock
import kotlinx.serialization.Serializable

@Serializable
sealed interface Symbol {
    val owner: SymbolOwner
    val isBound: Boolean
}

@Serializable
sealed interface BindableSymbol<Owner : SymbolOwner> : Symbol {
    override val owner: Owner
}

@Serializable
sealed interface PackageFragmentSymbol : Symbol

@Serializable
data class FileSymbol(
    override val owner: File,
    override val isBound: Boolean
) : PackageFragmentSymbol, BindableSymbol<File>

@Serializable
data class ExternalPackageFragmentSymbol(
    override val owner: ExternalPackageFragment,
    override val isBound: Boolean
) : PackageFragmentSymbol, BindableSymbol<ExternalPackageFragment>

@Serializable
data class AnonymousInitializerSymbol(
    override val owner: AnonymousInitializer,
    override val isBound: Boolean
) : BindableSymbol<AnonymousInitializer>

@Serializable
data class EnumEntrySymbol(
    override val owner: EnumEntry,
    override val isBound: Boolean
) : BindableSymbol<EnumEntry>

@Serializable
data class FieldSymbol(
    override val owner: Field,
    override val isBound: Boolean
) : BindableSymbol<Field>

@Serializable
sealed interface ClassifierSymbol : Symbol

@Serializable
data class ClassSymbol(
    override val owner: Class,
    override val isBound: Boolean
) : ClassifierSymbol, BindableSymbol<Class>

@Serializable
data class TypeParameterSymbol(
    override val owner: TypeParameter,
    override val isBound: Boolean
) : ClassifierSymbol, BindableSymbol<TypeParameter>

@Serializable
sealed interface ValueSymbol : Symbol {
    override val owner: ValueDeclaration
}

@Serializable
data class ValueParameterSymbol(
    override val isBound: Boolean,
    override val owner: ValueDeclaration,
) : ValueSymbol, BindableSymbol<ValueDeclaration>

@Serializable
data class VariableSymbol(
    override val owner: ValueDeclaration,
    override val isBound: Boolean
) : ValueSymbol, BindableSymbol<ValueDeclaration>

@Serializable
sealed interface ReturnTargetSymbol : Symbol {
    override val owner: ReturnTarget
}

@Serializable
sealed interface FunctionSymbol : ReturnTargetSymbol {
    override val owner: Function
}

@Serializable
data class ConstructorSymbol(
    override val owner: Constructor,
    override val isBound: Boolean
) : FunctionSymbol, BindableSymbol<Constructor>

@Serializable
data class SimpleFunctionSymbol(
    override val owner: SimpleFunction,
    override val isBound: Boolean
) : FunctionSymbol, BindableSymbol<SimpleFunction>

@Serializable
data class ReturnableBlockSymbol(
    override val owner: ReturnableBlock,
    override val isBound: Boolean
) : ReturnTargetSymbol, BindableSymbol<ReturnableBlock>

@Serializable
data class PropertySymbol(
    override val owner: Property,
    override val isBound: Boolean
) : BindableSymbol<Property>

@Serializable
data class LocalDelegatedPropertySymbol(
    override val owner: LocalDelegatedProperty,
    override val isBound: Boolean
) : BindableSymbol<LocalDelegatedProperty>

@Serializable
data class TypeAliasSymbol(
    override val owner: TypeAlias,
    override val isBound: Boolean
) : BindableSymbol<TypeAlias>
