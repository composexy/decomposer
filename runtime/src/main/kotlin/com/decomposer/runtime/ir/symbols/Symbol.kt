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
import com.decomposer.runtime.ir.declarations.ValueParameter
import com.decomposer.runtime.ir.declarations.Variable
import com.decomposer.runtime.ir.expressions.ReturnableBlock
import kotlinx.serialization.Serializable

@Serializable
sealed interface Symbol {
    val owner: SymbolOwner
    val hasDescriptor: Boolean
    val isBound: Boolean
}

@Serializable
sealed interface BindableSymbol<Owner : SymbolOwner> : Symbol {
    override val owner: Owner
}

@Serializable
sealed interface PackageFragmentSymbol : Symbol

interface FileSymbol : PackageFragmentSymbol, BindableSymbol<File>
interface ExternalPackageFragmentSymbol : PackageFragmentSymbol, BindableSymbol<ExternalPackageFragment>
interface AnonymousInitializerSymbol : BindableSymbol<AnonymousInitializer>
interface EnumEntrySymbol : BindableSymbol<EnumEntry>
interface FieldSymbol : BindableSymbol<Field>

sealed interface ClassifierSymbol : Symbol

interface ClassSymbol : ClassifierSymbol, BindableSymbol<Class>
interface TypeParameterSymbol : ClassifierSymbol, BindableSymbol<TypeParameter>

sealed interface ValueSymbol : Symbol {
    override val owner: ValueDeclaration
}

interface ValueParameterSymbol : ValueSymbol, BindableSymbol<ValueParameter>
interface VariableSymbol : ValueSymbol, BindableSymbol<Variable>

sealed interface ReturnTargetSymbol : Symbol {
    override val owner: ReturnTarget
}

sealed interface FunctionSymbol : ReturnTargetSymbol {
    override val owner: Function
}

interface ConstructorSymbol : FunctionSymbol, BindableSymbol<Constructor>
interface SimpleFunctionSymbol : FunctionSymbol, BindableSymbol<SimpleFunction>
interface ReturnableBlockSymbol : ReturnTargetSymbol, BindableSymbol<ReturnableBlock>
interface PropertySymbol : BindableSymbol<Property>
interface LocalDelegatedPropertySymbol : BindableSymbol<LocalDelegatedProperty>
interface TypeAliasSymbol : BindableSymbol<TypeAlias>
