@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.decomposer.compiler.serialize

import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.declarations.Declaration
import com.decomposer.runtime.ir.declarations.File
import com.decomposer.runtime.ir.declarations.FileEntry
import com.decomposer.runtime.ir.declarations.MetadataSourceOwner
import com.decomposer.runtime.ir.declarations.Module
import com.decomposer.runtime.ir.declarations.MutableAnnotationContainer
import com.decomposer.runtime.ir.declarations.PackageFragment
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.FileSymbol
import org.jetbrains.kotlin.backend.common.serialization.proto.IrField
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrErrorDeclaration
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.name.Name as IrName
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.FqName

fun IrFileSymbol.map(): FileSymbol =
    FileSymbol(
        isBound = this.isBound,
        owner = this.owner.map()
    )

fun IrModuleFragment.map(): Module =
    Module(
        name = this.name.map()
    )

fun IrName.map(): Name =
    Name(
        name = this.asString(),
        special = this.isSpecial
    )

fun IrFile.map(): File =
    File(
        symbol = this.symbol.map(),
        module = this.module.map(),
        fileEntry = this.fileEntry.map(),
        packageFqName = this.packageFqName.map(),
        startOffset = this.startOffset,
        endOffset = this.endOffset,
        declarations = this.declarations.map {
            it.map()
        },
        annotations = this.annotations.map {
            it.map()
        }
    )

fun IrFileEntry.map(): FileEntry =
    FileEntry(
        name = this.name,
        maxOffset = this.maxOffset
    )

fun FqName.map(): String = this.asString()

fun IrDeclaration.map(): Declaration {
    return when (this) {
        is IrAnonymousInitializer -> this.map()
        is IrClass -> this.map()
        is IrConstructor -> this.map()
        is IrEnumEntry -> this.map()
        is IrErrorDeclaration -> this.map()
        is IrExternalPackageFragment -> this.map()
        else -> TODO()
    }
}

fun IrConstructorCall.map(): ConstructorCall = TODO()
