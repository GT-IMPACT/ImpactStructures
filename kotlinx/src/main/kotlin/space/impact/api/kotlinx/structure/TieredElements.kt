package space.impact.api.kotlinx.structure

import java.util.function.BiConsumer
import java.util.function.Function
import kotlin.reflect.KMutableProperty1
import net.minecraft.block.Block
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.IStructureElementCheckOnly
import space.impact.api.structure.ITierConverter
import space.impact.api.structure.StructureUtility
import org.apache.commons.lang3.tuple.Pair as JavaPair

/** Null extracted tiers are rejected by the core; a null notSet sentinel remains supported. */
public fun <T, Tier> tiered(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    getter: T.() -> Tier,
    setter: T.(Tier) -> Unit,
    knownTiers: List<Pair<Block, Int>>? = null,
    description: List<String>? = null,
): IStructureElement<T> {
    val tiers = knownTiers?.map { (block, metadata) ->
        JavaPair.of(
            block,
            metadata,
        )
    }
    val converter = ITierConverter<Tier?> { block, metadata ->
        extractor.invoke(
            block,
            metadata,
        )
    }
    val setTier = BiConsumer<T, Tier?> { context, tier ->
        val validTier = checkNotNull(tier)
        setter.invoke(
            context,
            validTier,
        )
    }
    val getTier = Function<T, Tier?> { context ->
        getter.invoke(context)
    }
    if (description == null) {
        return StructureUtility.ofBlocksTiered(
            converter,
            tiers,
            notSet,
            setTier,
            getTier,
        )
    }
    return StructureUtility.ofBlocksTiered(
        converter,
        tiers,
        notSet,
        setTier,
        getTier,
        description,
    )
}

public fun <T, Tier> tiered(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    property: KMutableProperty1<T, Tier>,
    knownTiers: List<Pair<Block, Int>>? = null,
    description: List<String>? = null,
): IStructureElement<T> {
    return tiered(
        extractor = extractor,
        notSet = notSet,
        getter = property::get,
        setter = property::set,
        knownTiers = knownTiers,
        description = description,
    )
}

public fun <T, Tier> tieredCheckOnly(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    getter: T.() -> Tier,
    setter: T.(Tier) -> Unit,
): IStructureElementCheckOnly<T> {
    return StructureUtility.ofBlocksTiered(
        ITierConverter<Tier?> { block, metadata ->
            extractor.invoke(
                block,
                metadata,
            )
        },
        notSet,
        BiConsumer<T, Tier?> { context, tier ->
            val validTier = checkNotNull(tier)
            setter.invoke(
                context,
                validTier,
            )
        },
        Function<T, Tier?> { context ->
            getter.invoke(context)
        },
    )
}

public fun <T, Tier> tieredCheckOnly(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    property: KMutableProperty1<T, Tier>,
): IStructureElementCheckOnly<T> {
    return tieredCheckOnly(
        extractor = extractor,
        notSet = notSet,
        getter = property::get,
        setter = property::set,
    )
}

public fun <T, Tier> ofBlocksTiered(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    property: KMutableProperty1<T, Tier>,
    knownTiers: List<Pair<Block, Int>>? = null,
    description: List<String>? = null,
): IStructureElement<T> {
    return tiered(
        extractor = extractor,
        notSet = notSet,
        property = property,
        knownTiers = knownTiers,
        description = description,
    )
}

public fun <T, Tier> ofBlocksTiered(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    getter: T.() -> Tier,
    setter: T.(Tier) -> Unit,
    knownTiers: List<Pair<Block, Int>>?,
    description: List<String>? = null,
): IStructureElement<T> {
    return tiered(
        extractor = extractor,
        notSet = notSet,
        getter = getter,
        setter = setter,
        knownTiers = knownTiers,
        description = description,
    )
}

public fun <T, Tier> ofBlocksTiered(
    extractor: (Block, Int) -> Tier?,
    notSet: Tier,
    getter: T.() -> Tier,
    setter: T.(Tier) -> Unit,
): IStructureElementCheckOnly<T> {
    return tieredCheckOnly(
        extractor = extractor,
        notSet = notSet,
        getter = getter,
        setter = setter,
    )
}
