package space.impact.structures.kotlinx.structure

import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import space.impact.structures.hint.HintColor
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.IStructureElement.PlaceResult
import space.impact.structures.structure.IStructureElementChain
import space.impact.structures.structure.IStructureElementDeferred
import space.impact.structures.structure.IStructureElementNoPlacement
import space.impact.structures.structure.StructureUtility
import space.impact.structures.structure.adders.IBlockAdder
import space.impact.structures.structure.adders.ITileAdder

public fun <T> block(
    block: Block,
    metadata: Int = 0,
    fallbackBlock: Block = block,
    fallbackMetadata: Int = metadata,
): IStructureElement<T> {
    return StructureUtility.ofBlock(
        block,
        metadata,
        fallbackBlock,
        fallbackMetadata,
    )
}

public fun <T> anyMetadataBlock(
    block: Block,
    fallbackBlock: Block = block,
    fallbackMetadata: Int = 0,
): IStructureElement<T> {
    return StructureUtility.ofBlockAnyMeta(
        block,
        fallbackBlock,
        fallbackMetadata,
    )
}

public fun <T> registryBlock(
    modId: String,
    name: String,
    metadata: Int = 0,
    wildcard: Boolean = false,
): IStructureElement<T> {
    return StructureUtility.ofBlockUnlocalizedName(
        modId,
        name,
        metadata,
        wildcard,
    )
}

public fun <T> registryBlockOr(
    modId: String,
    name: String,
    metadata: Int,
    fallback: IStructureElement<T>,
): IStructureElement<T> {
    return StructureUtility.ofBlockUnlocalizedName(
        modId,
        name,
        metadata,
        fallback,
    )
}

public fun <T> blocksFlat(
    blocks: Map<Block, Int>,
    defaultBlock: Block,
    defaultMetadata: Int = 0,
): IStructureElement<T> {
    return StructureUtility.ofBlocksFlat(
        blocks,
        defaultBlock,
        defaultMetadata,
    )
}

public fun <T> blocksMap(
    blocks: Map<Block, Collection<Int>>,
    defaultBlock: Block,
    defaultMetadata: Int = 0,
): IStructureElement<T> {
    return StructureUtility.ofBlocksMap(
        blocks,
        defaultBlock,
        defaultMetadata,
    )
}

public fun <T> blockHint(
    block: Block,
    metadata: Int,
    hintBlock: Block = block,
    hintMetadata: Int = metadata,
): IStructureElementNoPlacement<T> {
    return StructureUtility.ofBlockHint(
        block,
        metadata,
        hintBlock,
        hintMetadata,
    )
}

public fun <T> blocksFlatHint(
    blocks: Map<Block, Int>,
    hintBlock: Block,
    hintMetadata: Int = 0,
): IStructureElementNoPlacement<T> {
    return StructureUtility.ofBlocksFlatHint(
        blocks,
        hintBlock,
        hintMetadata,
    )
}

public fun <T> blocksMapHint(
    blocks: Map<Block, Collection<Int>>,
    hintBlock: Block,
    hintMetadata: Int = 0,
): IStructureElementNoPlacement<T> {
    return StructureUtility.ofBlocksMapHint(
        blocks,
        hintBlock,
        hintMetadata,
    )
}

public fun <T> hint(color: HintColor): IStructureElementNoPlacement<T> {
    return StructureUtility.ofHint(color)
}

public fun <T> hintDeferred(
    icons: () -> Array<IIcon>,
    tint: ShortArray? = null,
): IStructureElementNoPlacement<T> {
    val supplier = Supplier {
        icons.invoke()
    }
    if (tint == null) {
        return StructureUtility.ofHintDeferred(supplier)
    }
    return StructureUtility.ofHintDeferred(
        supplier,
        tint,
    )
}

public fun <T> air(): IStructureElement<T> {
    return StructureUtility.isAir()
}

public fun <T> notAir(): IStructureElement<T> {
    return StructureUtility.notAir()
}

public fun <T> error(): IStructureElement<T> {
    return StructureUtility.error()
}

public fun <T> chain(vararg elements: IStructureElement<T>): IStructureElementChain<T> {
    return StructureUtility.ofChain(*elements)
}

public fun <T> chain(elements: List<IStructureElement<T>>): IStructureElementChain<T> {
    return StructureUtility.ofChain(elements)
}

public fun <T> blockAdder(
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(Block, Int) -> Boolean,
): IStructureElement<T> {
    return StructureUtility.ofBlockAdder(
        IBlockAdder { context, block, metadata ->
            adder.invoke(
                context,
                block,
                metadata,
            )
        },
        hintBlock,
        hintMetadata,
    )
}

public fun <T> blockAdder(
    color: HintColor,
    adder: T.(Block, Int) -> Boolean,
): IStructureElement<T> {
    return StructureUtility.ofBlockAdder(
        IBlockAdder { context, block, metadata ->
            adder.invoke(
                context,
                block,
                metadata,
            )
        },
        color,
    )
}

public fun <T> blockAdderHint(
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(Block, Int) -> Boolean,
): IStructureElementNoPlacement<T> {
    return StructureUtility.ofBlockAdderHint(
        IBlockAdder { context, block, metadata ->
            adder.invoke(
                context,
                block,
                metadata,
            )
        },
        hintBlock,
        hintMetadata,
    )
}

public fun <T> tileAdder(
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(TileEntity?) -> Boolean,
): IStructureElementNoPlacement<T> {
    return StructureUtility.ofTileAdder(
        ITileAdder { context, tile ->
            adder.invoke(
                context,
                tile,
            )
        },
        hintBlock,
        hintMetadata,
    )
}

public fun <T, Tile : TileEntity> specificTileAdder(
    tileClass: Class<Tile>,
    hintBlock: Block,
    hintMetadata: Int = 0,
    adder: T.(Tile) -> Boolean,
): IStructureElementNoPlacement<T> {
    return StructureUtility.ofSpecificTileAdder(
        BiPredicate { context, tile ->
            adder.invoke(
                context,
                tile,
            )
        },
        tileClass,
        hintBlock,
        hintMetadata,
    )
}

public fun <T> onPass(
    element: IStructureElement<T>,
    action: T.() -> Unit,
): IStructureElement<T> {
    return StructureUtility.onElementPass(
        Consumer { context ->
            action.invoke(context)
        },
        element,
    )
}

public fun <T> onFail(
    element: IStructureElement<T>,
    action: T.() -> Unit,
): IStructureElement<T> {
    return StructureUtility.onElementFail(
        Consumer { context ->
            action.invoke(context)
        },
        element,
    )
}

public fun <T> described(
    element: IStructureElement<T>,
    description: List<String>?,
): IStructureElement<T> {
    return StructureUtility.withDescription(
        description,
        element,
    )
}

public fun <T> onlyIf(
    element: IStructureElement<T>,
    whenDisabled: PlaceResult = PlaceResult.REJECT_CONTINUE,
    predicate: T.() -> Boolean,
): IStructureElement<T> {
    return StructureUtility.onlyIf(
        Predicate { context ->
            predicate.invoke(context)
        },
        element,
        whenDisabled,
    )
}

public fun <T> channeled(
    element: IStructureElement<T>,
    channel: String,
): IStructureElement<T> {
    return StructureUtility.withChannel(
        channel,
        element,
    )
}

public fun <T> step(
    a: Int = 0,
    b: Int = 0,
    c: Int = 0,
): IStructureElement<T> {
    return StructureUtility.step(
        a,
        b,
        c,
    )
}

public fun <T> lazyElement(factory: () -> IStructureElement<T>): IStructureElementDeferred<T> {
    return StructureUtility.lazy(
        Supplier {
            factory.invoke()
        },
    )
}

public fun <T> lazyElementFor(factory: T.() -> IStructureElement<T>): IStructureElementDeferred<T> {
    return StructureUtility.lazy(
        Function<T, IStructureElement<T>> { context ->
            factory.invoke(context)
        },
    )
}

public fun <T> deferredElement(factory: () -> IStructureElement<T>): IStructureElementDeferred<T> {
    return StructureUtility.defer(
        Supplier {
            factory.invoke()
        },
    )
}

public fun <T> deferredElementFor(factory: T.() -> IStructureElement<T>): IStructureElementDeferred<T> {
    return StructureUtility.defer(
        Function<T, IStructureElement<T>> { context ->
            factory.invoke(context)
        },
    )
}

public fun <T> deferredElementWithTrigger(
    factory: T.(ItemStack?) -> IStructureElement<T>,
): IStructureElementDeferred<T> {
    return StructureUtility.defer(
        BiFunction<T, ItemStack?, IStructureElement<T>> { context, trigger ->
            factory.invoke(
                context,
                trigger,
            )
        },
    )
}

public fun <T> deferredElementWithCheck(
    checkFactory: T.() -> IStructureElement<T>,
    factory: T.(ItemStack?) -> IStructureElement<T>,
): IStructureElementDeferred<T> {
    return StructureUtility.defer(
        Function<T, IStructureElement<T>> { context ->
            checkFactory.invoke(context)
        },
        BiFunction<T, ItemStack?, IStructureElement<T>> { context, trigger ->
            factory.invoke(
                context,
                trigger,
            )
        },
    )
}

public fun <T, Key> partitionBy(
    elements: Map<Key, IStructureElement<T>>,
    fallback: IStructureElement<T>? = null,
    key: T.() -> Key,
): IStructureElementDeferred<T> {
    val extractor = Function<T, Key> { context ->
        key.invoke(context)
    }
    if (fallback == null) {
        return StructureUtility.partitionBy(
            extractor,
            elements,
        )
    }
    return StructureUtility.partitionBy(
        extractor,
        elements,
        fallback,
    )
}

public fun <T> partitionByIndex(
    elements: List<IStructureElement<T>>,
    index: T.() -> Int,
): IStructureElementDeferred<T> {
    return StructureUtility.partitionBy(
        Function<T, Int> { context ->
            index.invoke(context)
        },
        elements,
    )
}

public fun <T, Key> partitionByWithTrigger(
    elements: Map<Key, IStructureElement<T>>,
    fallback: IStructureElement<T>? = null,
    key: T.(ItemStack?) -> Key,
): IStructureElementDeferred<T> {
    val extractor = BiFunction<T, ItemStack?, Key> { context, trigger ->
        key.invoke(
            context,
            trigger,
        )
    }
    if (fallback == null) {
        return StructureUtility.partitionBy(
            extractor,
            elements,
        )
    }
    return StructureUtility.partitionBy(
        extractor,
        elements,
        fallback,
    )
}

public fun <T> partitionByIndexWithTrigger(
    elements: List<IStructureElement<T>>,
    index: T.(ItemStack?) -> Int,
): IStructureElementDeferred<T> {
    return StructureUtility.defer(
        BiFunction<T, ItemStack?, Int> { context, trigger ->
            index.invoke(
                context,
                trigger,
            )
        },
        elements,
    )
}
