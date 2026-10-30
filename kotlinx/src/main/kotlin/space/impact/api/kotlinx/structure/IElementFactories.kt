package space.impact.api.kotlinx.structure

import kotlin.reflect.KMutableProperty1
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import space.impact.api.hint.HintColor
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.IStructureElement.PlaceResult
import space.impact.api.structure.IStructureElementChain
import space.impact.api.structure.IStructureElementCheckOnly
import space.impact.api.structure.IStructureElementDeferred
import space.impact.api.structure.IStructureElementNoPlacement
import space.impact.api.kotlinx.structure.air as createAir
import space.impact.api.kotlinx.structure.anyMetadataBlock as createAnyMetadataBlock
import space.impact.api.kotlinx.structure.block as createBlock
import space.impact.api.kotlinx.structure.blockAdder as createBlockAdder
import space.impact.api.kotlinx.structure.blockAdderHint as createBlockAdderHint
import space.impact.api.kotlinx.structure.blockHint as createBlockHint
import space.impact.api.kotlinx.structure.blocksFlat as createBlocksFlat
import space.impact.api.kotlinx.structure.blocksFlatHint as createBlocksFlatHint
import space.impact.api.kotlinx.structure.blocksMap as createBlocksMap
import space.impact.api.kotlinx.structure.blocksMapHint as createBlocksMapHint
import space.impact.api.kotlinx.structure.chain as createChain
import space.impact.api.kotlinx.structure.channeled as createChanneled
import space.impact.api.kotlinx.structure.contextElement as createContextElement
import space.impact.api.kotlinx.structure.customElement as createCustomElement
import space.impact.api.kotlinx.structure.deferredElement as createDeferredElement
import space.impact.api.kotlinx.structure.deferredElementFor as createDeferredElementFor
import space.impact.api.kotlinx.structure.deferredElementWithCheck as createDeferredElementWithCheck
import space.impact.api.kotlinx.structure.deferredElementWithTrigger as createDeferredElementWithTrigger
import space.impact.api.kotlinx.structure.described as createDescribed
import space.impact.api.kotlinx.structure.error as createError
import space.impact.api.kotlinx.structure.hint as createHint
import space.impact.api.kotlinx.structure.hintDeferred as createHintDeferred
import space.impact.api.kotlinx.structure.lazyElement as createLazyElement
import space.impact.api.kotlinx.structure.lazyElementFor as createLazyElementFor
import space.impact.api.kotlinx.structure.notAir as createNotAir
import space.impact.api.kotlinx.structure.onFail as createOnFail
import space.impact.api.kotlinx.structure.onPass as createOnPass
import space.impact.api.kotlinx.structure.onlyIf as createOnlyIf
import space.impact.api.kotlinx.structure.partitionBy as createPartitionBy
import space.impact.api.kotlinx.structure.partitionByCheckAndTrigger as createPartitionByCheckAndTrigger
import space.impact.api.kotlinx.structure.partitionByCheckAndTriggerIndex as createPartitionByCheckAndTriggerIndex
import space.impact.api.kotlinx.structure.partitionByIndex as createPartitionByIndex
import space.impact.api.kotlinx.structure.partitionByIndexWithTrigger as createPartitionByIndexWithTrigger
import space.impact.api.kotlinx.structure.partitionByWithTrigger as createPartitionByWithTrigger
import space.impact.api.kotlinx.structure.registryBlock as createRegistryBlock
import space.impact.api.kotlinx.structure.registryBlockOr as createRegistryBlockOr
import space.impact.api.kotlinx.structure.specificTileAdder as createSpecificTileAdder
import space.impact.api.kotlinx.structure.step as createStep
import space.impact.api.kotlinx.structure.tiered as createTiered
import space.impact.api.kotlinx.structure.tieredCheckOnly as createTieredCheckOnly
import space.impact.api.kotlinx.structure.tileAdder as createTileAdder
import space.impact.api.kotlinx.structure.withHint as createWithHint

/** Factories keep the controller type inferred inside a structure builder. */
public interface IElementFactories<T> {

    public fun block(
        block: Block,
        metadata: Int = 0,
        fallbackBlock: Block = block,
        fallbackMetadata: Int = metadata,
    ): IStructureElement<T> {
        return createBlock<T>(
            block = block,
            metadata = metadata,
            fallbackBlock = fallbackBlock,
            fallbackMetadata = fallbackMetadata,
        )
    }

    public fun anyMetadataBlock(
        block: Block,
        fallbackBlock: Block = block,
        fallbackMetadata: Int = 0,
    ): IStructureElement<T> {
        return createAnyMetadataBlock<T>(
            block = block,
            fallbackBlock = fallbackBlock,
            fallbackMetadata = fallbackMetadata,
        )
    }

    public fun registryBlock(
        modId: String,
        name: String,
        metadata: Int = 0,
        wildcard: Boolean = false,
    ): IStructureElement<T> {
        return createRegistryBlock<T>(
            modId = modId,
            name = name,
            metadata = metadata,
            wildcard = wildcard,
        )
    }

    public fun registryBlockOr(
        modId: String,
        name: String,
        metadata: Int,
        fallback: IStructureElement<T>,
    ): IStructureElement<T> {
        return createRegistryBlockOr<T>(
            modId = modId,
            name = name,
            metadata = metadata,
            fallback = fallback,
        )
    }

    public fun blocksFlat(
        blocks: Map<Block, Int>,
        defaultBlock: Block,
        defaultMetadata: Int = 0,
    ): IStructureElement<T> {
        return createBlocksFlat<T>(
            blocks = blocks,
            defaultBlock = defaultBlock,
            defaultMetadata = defaultMetadata,
        )
    }

    public fun blocksMap(
        blocks: Map<Block, Collection<Int>>,
        defaultBlock: Block,
        defaultMetadata: Int = 0,
    ): IStructureElement<T> {
        return createBlocksMap<T>(
            blocks = blocks,
            defaultBlock = defaultBlock,
            defaultMetadata = defaultMetadata,
        )
    }

    public fun blockHint(
        block: Block,
        metadata: Int,
        hintBlock: Block = block,
        hintMetadata: Int = metadata,
    ): IStructureElementNoPlacement<T> {
        return createBlockHint<T>(
            block = block,
            metadata = metadata,
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
        )
    }

    public fun blocksFlatHint(
        blocks: Map<Block, Int>,
        hintBlock: Block,
        hintMetadata: Int = 0,
    ): IStructureElementNoPlacement<T> {
        return createBlocksFlatHint<T>(
            blocks = blocks,
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
        )
    }

    public fun blocksMapHint(
        blocks: Map<Block, Collection<Int>>,
        hintBlock: Block,
        hintMetadata: Int = 0,
    ): IStructureElementNoPlacement<T> {
        return createBlocksMapHint<T>(
            blocks = blocks,
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
        )
    }

    public fun hint(color: HintColor): IStructureElementNoPlacement<T> {
        return createHint<T>(color)
    }

    public fun withHint(
        element: IStructureElement<T>,
        color: HintColor,
    ): IStructureElement<T> {
        return createWithHint(
            element = element,
            color = color,
        )
    }

    public fun hintDeferred(
        icons: () -> Array<IIcon>,
        tint: ShortArray? = null,
    ): IStructureElementNoPlacement<T> {
        return createHintDeferred<T>(
            icons = icons,
            tint = tint,
        )
    }

    public fun air(): IStructureElement<T> {
        return createAir<T>()
    }

    public fun notAir(): IStructureElement<T> {
        return createNotAir<T>()
    }

    public fun error(): IStructureElement<T> {
        return createError<T>()
    }

    public fun chain(vararg elements: IStructureElement<T>): IStructureElementChain<T> {
        return createChain<T>(*elements)
    }

    public fun chain(elements: List<IStructureElement<T>>): IStructureElementChain<T> {
        return createChain<T>(elements)
    }

    public fun blockAdder(
        hintBlock: Block,
        hintMetadata: Int = 0,
        adder: T.(Block, Int) -> Boolean,
    ): IStructureElement<T> {
        return createBlockAdder<T>(
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
            adder = adder,
        )
    }

    public fun blockAdder(
        color: HintColor,
        adder: T.(Block, Int) -> Boolean,
    ): IStructureElement<T> {
        return createBlockAdder<T>(
            color = color,
            adder = adder,
        )
    }

    public fun blockAdderHint(
        hintBlock: Block,
        hintMetadata: Int = 0,
        adder: T.(Block, Int) -> Boolean,
    ): IStructureElementNoPlacement<T> {
        return createBlockAdderHint<T>(
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
            adder = adder,
        )
    }

    public fun tileAdder(
        hintBlock: Block,
        hintMetadata: Int = 0,
        adder: T.(TileEntity?) -> Boolean,
    ): IStructureElementNoPlacement<T> {
        return createTileAdder<T>(
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
            adder = adder,
        )
    }

    public fun <Tile : TileEntity> specificTileAdder(
        tileClass: Class<Tile>,
        hintBlock: Block,
        hintMetadata: Int = 0,
        adder: T.(Tile) -> Boolean,
    ): IStructureElementNoPlacement<T> {
        return createSpecificTileAdder<T, Tile>(
            tileClass = tileClass,
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
            adder = adder,
        )
    }

    public fun onPass(
        element: IStructureElement<T>,
        action: T.() -> Unit,
    ): IStructureElement<T> {
        return createOnPass<T>(
            element = element,
            action = action,
        )
    }

    public fun onFail(
        element: IStructureElement<T>,
        action: T.() -> Unit,
    ): IStructureElement<T> {
        return createOnFail<T>(
            element = element,
            action = action,
        )
    }

    public fun described(
        element: IStructureElement<T>,
        description: List<String>?,
    ): IStructureElement<T> {
        return createDescribed<T>(
            element = element,
            description = description,
        )
    }

    public fun onlyIf(
        element: IStructureElement<T>,
        whenDisabled: PlaceResult = PlaceResult.REJECT_CONTINUE,
        predicate: T.() -> Boolean,
    ): IStructureElement<T> {
        return createOnlyIf<T>(
            element = element,
            whenDisabled = whenDisabled,
            predicate = predicate,
        )
    }

    public fun channeled(
        element: IStructureElement<T>,
        channel: String,
    ): IStructureElement<T> {
        return createChanneled<T>(
            element = element,
            channel = channel,
        )
    }

    public fun step(
        a: Int = 0,
        b: Int = 0,
        c: Int = 0,
    ): IStructureElement<T> {
        return createStep<T>(
            a = a,
            b = b,
            c = c,
        )
    }

    public fun lazyElement(factory: () -> IStructureElement<T>): IStructureElementDeferred<T> {
        return createLazyElement<T>(factory)
    }

    public fun lazyElementFor(factory: T.() -> IStructureElement<T>): IStructureElementDeferred<T> {
        return createLazyElementFor<T>(factory)
    }

    public fun deferredElement(factory: () -> IStructureElement<T>): IStructureElementDeferred<T> {
        return createDeferredElement<T>(factory)
    }

    public fun deferredElementFor(factory: T.() -> IStructureElement<T>): IStructureElementDeferred<T> {
        return createDeferredElementFor<T>(factory)
    }

    public fun deferredElementWithTrigger(
        factory: T.(ItemStack?) -> IStructureElement<T>,
    ): IStructureElementDeferred<T> {
        return createDeferredElementWithTrigger<T>(factory)
    }

    public fun deferredElementWithCheck(
        checkFactory: T.() -> IStructureElement<T>,
        factory: T.(ItemStack?) -> IStructureElement<T>,
    ): IStructureElementDeferred<T> {
        return createDeferredElementWithCheck<T>(
            checkFactory = checkFactory,
            factory = factory,
        )
    }

    public fun <Key> partitionBy(
        elements: Map<Key, IStructureElement<T>>,
        fallback: IStructureElement<T>? = null,
        key: T.() -> Key,
    ): IStructureElementDeferred<T> {
        return createPartitionBy<T, Key>(
            elements = elements,
            fallback = fallback,
            key = key,
        )
    }

    public fun partitionByIndex(
        elements: List<IStructureElement<T>>,
        index: T.() -> Int,
    ): IStructureElementDeferred<T> {
        return createPartitionByIndex<T>(
            elements = elements,
            index = index,
        )
    }

    public fun <Key> partitionByWithTrigger(
        elements: Map<Key, IStructureElement<T>>,
        fallback: IStructureElement<T>? = null,
        key: T.(ItemStack?) -> Key,
    ): IStructureElementDeferred<T> {
        return createPartitionByWithTrigger<T, Key>(
            elements = elements,
            fallback = fallback,
            key = key,
        )
    }

    public fun partitionByIndexWithTrigger(
        elements: List<IStructureElement<T>>,
        index: T.(ItemStack?) -> Int,
    ): IStructureElementDeferred<T> {
        return createPartitionByIndexWithTrigger<T>(
            elements = elements,
            index = index,
        )
    }

    public fun <Tier> tiered(
        extractor: (Block, Int) -> Tier?,
        notSet: Tier,
        getter: T.() -> Tier,
        setter: T.(Tier) -> Unit,
        knownTiers: List<Pair<Block, Int>>? = null,
        description: List<String>? = null,
    ): IStructureElement<T> {
        return createTiered<T, Tier>(
            extractor = extractor,
            notSet = notSet,
            getter = getter,
            setter = setter,
            knownTiers = knownTiers,
            description = description,
        )
    }

    public fun <Tier> tiered(
        extractor: (Block, Int) -> Tier?,
        notSet: Tier,
        property: KMutableProperty1<T, Tier>,
        knownTiers: List<Pair<Block, Int>>? = null,
        description: List<String>? = null,
    ): IStructureElement<T> {
        return createTiered<T, Tier>(
            extractor = extractor,
            notSet = notSet,
            property = property,
            knownTiers = knownTiers,
            description = description,
        )
    }

    public fun <Tier> tieredCheckOnly(
        extractor: (Block, Int) -> Tier?,
        notSet: Tier,
        property: KMutableProperty1<T, Tier>,
    ): IStructureElementCheckOnly<T> {
        return createTieredCheckOnly<T, Tier>(
            extractor = extractor,
            notSet = notSet,
            property = property,
        )
    }

    public fun <Tier> tieredCheckOnly(
        extractor: (Block, Int) -> Tier?,
        notSet: Tier,
        getter: T.() -> Tier,
        setter: T.(Tier) -> Unit,
    ): IStructureElementCheckOnly<T> {
        return createTieredCheckOnly<T, Tier>(
            extractor = extractor,
            notSet = notSet,
            getter = getter,
            setter = setter,
        )
    }

    public fun customElement(
        check: CheckCallback<T>,
        hint: HintCallback<T>? = null,
        place: HintCallback<T>? = null,
        survival: SurvivalCallback<T>? = null,
        description: (T.() -> List<String>?)? = null,
        channels: Collection<String> = emptySet(),
        couldBeValid: HintCallback<T>? = null,
        blocksToPlace: BlocksCallback<T>? = null,
    ): IStructureElement<T> {
        return createCustomElement<T>(
            check = check,
            hint = hint,
            place = place,
            survival = survival,
            description = description,
            channels = channels,
            couldBeValid = couldBeValid,
            blocksToPlace = blocksToPlace,
        )
    }

    public fun <Context> contextElement(
        element: IStructureElement<Context>,
        context: T.() -> Context,
    ): IStructureElement<T> {
        return createContextElement<T, Context>(
            element = element,
            context = context,
        )
    }

    public fun <Key> partitionByCheckAndTrigger(
        elements: Map<Key, IStructureElement<T>>,
        checkKey: T.() -> Key,
        triggerKey: T.(ItemStack?) -> Key,
        fallback: IStructureElement<T>? = null,
    ): IStructureElementDeferred<T> {
        return createPartitionByCheckAndTrigger<T, Key>(
            elements = elements,
            checkKey = checkKey,
            triggerKey = triggerKey,
            fallback = fallback,
        )
    }

    public fun partitionByCheckAndTriggerIndex(
        elements: List<IStructureElement<T>>,
        checkIndex: T.() -> Int,
        triggerIndex: T.(ItemStack?) -> Int,
    ): IStructureElementDeferred<T> {
        return createPartitionByCheckAndTriggerIndex<T>(
            elements = elements,
            checkIndex = checkIndex,
            triggerIndex = triggerIndex,
        )
    }
}
