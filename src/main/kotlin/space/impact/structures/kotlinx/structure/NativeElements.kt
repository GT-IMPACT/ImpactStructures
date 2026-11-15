package space.impact.structures.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import space.impact.structures.hint.HintColor
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.IStructureElementChain
import space.impact.structures.structure.IStructureElementDeferred
import space.impact.structures.structure.IStructureElementNoPlacement

/** Java-familiar names; trigger/context variants use distinct names to avoid lambda overload ambiguity. */
public fun <T> ofBlock(
    block: Block,
    metadata: Int = 0,
    fallbackBlock: Block = block,
    fallbackMetadata: Int = metadata,
): IStructureElement<T> {
    return block<T>(
        block = block,
        metadata = metadata,
        fallbackBlock = fallbackBlock,
        fallbackMetadata = fallbackMetadata,
    )
}

public fun <T> ofBlockAnyMeta(
    block: Block,
    fallbackBlock: Block = block,
    fallbackMetadata: Int = 0,
): IStructureElement<T> {
    return anyMetadataBlock<T>(
        block = block,
        fallbackBlock = fallbackBlock,
        fallbackMetadata = fallbackMetadata,
    )
}

public fun <T> ofBlockUnlocalizedName(
    modId: String,
    name: String,
    metadata: Int = 0,
    wildcard: Boolean = false,
): IStructureElement<T> {
    return registryBlock<T>(
        modId = modId,
        name = name,
        metadata = metadata,
        wildcard = wildcard,
    )
}

public fun <T> ofBlockUnlocalizedName(
    modId: String,
    name: String,
    metadata: Int,
    fallback: IStructureElement<T>,
): IStructureElement<T> {
    return registryBlockOr(
        modId = modId,
        name = name,
        metadata = metadata,
        fallback = fallback,
    )
}

public fun <T> ofBlocksFlat(
    blocks: Map<Block, Int>,
    defaultBlock: Block,
    defaultMetadata: Int = 0,
): IStructureElement<T> {
    return blocksFlat<T>(
        blocks = blocks,
        defaultBlock = defaultBlock,
        defaultMetadata = defaultMetadata,
    )
}

public fun <T> ofBlocksMap(
    blocks: Map<Block, Collection<Int>>,
    defaultBlock: Block,
    defaultMetadata: Int = 0,
): IStructureElement<T> {
    return blocksMap<T>(
        blocks = blocks,
        defaultBlock = defaultBlock,
        defaultMetadata = defaultMetadata,
    )
}

public fun <T> ofBlockHint(
    block: Block,
    metadata: Int,
    hintBlock: Block = block,
    hintMetadata: Int = metadata,
): IStructureElementNoPlacement<T> {
    return blockHint<T>(
        block = block,
        metadata = metadata,
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
    )
}

public fun <T> ofBlocksFlatHint(
    blocks: Map<Block, Int>,
    hintBlock: Block,
    hintMetadata: Int = 0,
): IStructureElementNoPlacement<T> {
    return blocksFlatHint<T>(
        blocks = blocks,
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
    )
}

public fun <T> ofBlocksMapHint(
    blocks: Map<Block, Collection<Int>>,
    hintBlock: Block,
    hintMetadata: Int = 0,
): IStructureElementNoPlacement<T> {
    return blocksMapHint<T>(
        blocks = blocks,
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
    )
}

public fun <T> ofHint(color: HintColor): IStructureElementNoPlacement<T> {
    return hint<T>(color)
}

public fun <T> ofHintDeferred(
    icons: () -> Array<IIcon>,
    tint: ShortArray? = null,
): IStructureElementNoPlacement<T> {
    return hintDeferred<T>(
        icons = icons,
        tint = tint,
    )
}

public fun <T> isAir(): IStructureElement<T> {
    return air<T>()
}

public fun <T> ofChain(vararg elements: IStructureElement<T>): IStructureElementChain<T> {
    return chain<T>(*elements)
}

public fun <T> ofChain(elements: List<IStructureElement<T>>): IStructureElementChain<T> {
    return chain<T>(elements)
}

public fun <T> ofBlockAdder(
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(Block, Int) -> Boolean,
): IStructureElement<T> {
    return blockAdder<T>(
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
        adder = adder,
    )
}

public fun <T> ofBlockAdder(
    color: HintColor,
    adder: T.(Block, Int) -> Boolean,
): IStructureElement<T> {
    return blockAdder(
        color = color,
        adder = adder,
    )
}

public fun <T> ofBlockAdderHint(
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(Block, Int) -> Boolean,
): IStructureElementNoPlacement<T> {
    return blockAdderHint<T>(
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
        adder = adder,
    )
}

public fun <T> ofTileAdder(
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(TileEntity?) -> Boolean,
): IStructureElementNoPlacement<T> {
    return tileAdder<T>(
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
        adder = adder,
    )
}

public fun <T, Tile : TileEntity> ofSpecificTileAdder(
    tileClass: Class<Tile>,
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(Tile) -> Boolean,
): IStructureElementNoPlacement<T> {
    return specificTileAdder<T, Tile>(
        tileClass = tileClass,
        hintBlock = hintBlock,
        hintMetadata = hintMetadata,
        adder = adder,
    )
}

public fun <T> onElementPass(
    element: IStructureElement<T>,
    action: T.() -> Unit,
): IStructureElement<T> {
    return onPass<T>(
        element = element,
        action = action,
    )
}

public fun <T> onElementFail(
    element: IStructureElement<T>,
    action: T.() -> Unit,
): IStructureElement<T> {
    return onFail<T>(
        element = element,
        action = action,
    )
}

public fun <T> withDescription(
    element: IStructureElement<T>,
    description: List<String>?,
): IStructureElement<T> {
    return described<T>(
        element = element,
        description = description,
    )
}

public fun <T> withChannel(
    element: IStructureElement<T>,
    channel: String,
): IStructureElement<T> {
    return channeled<T>(
        element = element,
        channel = channel,
    )
}

public fun <T> lazy(factory: () -> IStructureElement<T>): IStructureElementDeferred<T> {
    return lazyElement<T>(factory)
}

public fun <T> lazyFor(factory: T.() -> IStructureElement<T>): IStructureElementDeferred<T> {
    return lazyElementFor<T>(factory)
}

public fun <T> defer(factory: () -> IStructureElement<T>): IStructureElementDeferred<T> {
    return deferredElement<T>(factory)
}

public fun <T> deferFor(factory: T.() -> IStructureElement<T>): IStructureElementDeferred<T> {
    return deferredElementFor<T>(factory)
}

public fun <T> deferWithTrigger(factory: T.(ItemStack?) -> IStructureElement<T>): IStructureElementDeferred<T> {
    return deferredElementWithTrigger<T>(factory)
}
