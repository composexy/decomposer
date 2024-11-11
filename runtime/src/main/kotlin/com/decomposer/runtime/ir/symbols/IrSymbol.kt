package com.decomposer.runtime.ir.symbols

import com.decomposer.runtime.ir.declarations.IrAnonymousInitializer
import com.decomposer.runtime.ir.declarations.IrClass
import com.decomposer.runtime.ir.declarations.IrConstructor
import com.decomposer.runtime.ir.declarations.IrEnumEntry
import com.decomposer.runtime.ir.declarations.IrExternalPackageFragment
import com.decomposer.runtime.ir.declarations.IrField
import com.decomposer.runtime.ir.declarations.IrFile
import com.decomposer.runtime.ir.declarations.IrFunction
import com.decomposer.runtime.ir.declarations.IrLocalDelegatedProperty
import com.decomposer.runtime.ir.declarations.IrProperty
import com.decomposer.runtime.ir.declarations.IrReturnTarget
import com.decomposer.runtime.ir.declarations.IrSimpleFunction
import com.decomposer.runtime.ir.declarations.IrSymbolOwner
import com.decomposer.runtime.ir.declarations.IrTypeAlias
import com.decomposer.runtime.ir.declarations.IrTypeParameter
import com.decomposer.runtime.ir.declarations.IrValueDeclaration
import com.decomposer.runtime.ir.declarations.IrValueParameter
import com.decomposer.runtime.ir.declarations.IrVariable
import com.decomposer.runtime.ir.expressions.IrReturnableBlock

interface IrSymbol {
    val owner: IrSymbolOwner
    val hasDescriptor: Boolean
    val isBound: Boolean
}

interface IrBindableSymbol<Owner : IrSymbolOwner> : IrSymbol {
    override val owner: Owner
}

sealed interface IrPackageFragmentSymbol : IrSymbol

interface IrFileSymbol : IrPackageFragmentSymbol, IrBindableSymbol<IrFile>
interface IrExternalPackageFragmentSymbol : IrPackageFragmentSymbol, IrBindableSymbol<IrExternalPackageFragment>
interface IrAnonymousInitializerSymbol : IrBindableSymbol<IrAnonymousInitializer>
interface IrEnumEntrySymbol : IrBindableSymbol<IrEnumEntry>
interface IrFieldSymbol : IrBindableSymbol<IrField>

sealed interface IrClassifierSymbol : IrSymbol

interface IrClassSymbol : IrClassifierSymbol, IrBindableSymbol<IrClass>
interface IrTypeParameterSymbol : IrClassifierSymbol, IrBindableSymbol<IrTypeParameter>

sealed interface IrValueSymbol : IrSymbol {
    override val owner: IrValueDeclaration
}

interface IrValueParameterSymbol : IrValueSymbol, IrBindableSymbol<IrValueParameter>
interface IrVariableSymbol : IrValueSymbol, IrBindableSymbol<IrVariable>

sealed interface IrReturnTargetSymbol : IrSymbol {
    override val owner: IrReturnTarget
}

sealed interface IrFunctionSymbol : IrReturnTargetSymbol {
    override val owner: IrFunction
}

interface IrConstructorSymbol : IrFunctionSymbol, IrBindableSymbol<IrConstructor>
interface IrSimpleFunctionSymbol : IrFunctionSymbol, IrBindableSymbol<IrSimpleFunction>
interface IrReturnableBlockSymbol : IrReturnTargetSymbol, IrBindableSymbol<IrReturnableBlock>
interface IrPropertySymbol : IrBindableSymbol<IrProperty>
interface IrLocalDelegatedPropertySymbol : IrBindableSymbol<IrLocalDelegatedProperty>
interface IrTypeAliasSymbol : IrBindableSymbol<IrTypeAlias>
