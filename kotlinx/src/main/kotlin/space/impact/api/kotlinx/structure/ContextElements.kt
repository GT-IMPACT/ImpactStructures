package space.impact.api.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.IStructureElementNoPlacement
import space.impact.api.structure.IWithExtendedContext
import space.impact.api.structure.StructureUtility

public fun <T, Context> contextElement(
    element: IStructureElement<Context>,
    context: T.() -> Context,
): IStructureElement<T> {
    return ContextElement(
        element = element,
        context = context,
    )
}

public fun <Context, T : IWithExtendedContext<Context>> withContext(
    element: IStructureElement<Context>,
): IStructureElement<T> {
    return StructureUtility.withContext(element)
}

public inline fun <T, reified Tile : TileEntity> tileAdderOf(
    hintBlock: Block,
    hintMetadata: Int = 0,
    noinline adder: T.(Tile) -> Boolean,
): IStructureElementNoPlacement<T> {
    return specificTileAdder(
        tileClass = Tile::class.java,
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
        adder = adder,
    )
}
