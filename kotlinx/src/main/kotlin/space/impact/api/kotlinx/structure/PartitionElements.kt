package space.impact.api.kotlinx.structure

import java.util.function.BiFunction
import java.util.function.Function
import net.minecraft.item.ItemStack
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.IStructureElementDeferred
import space.impact.api.structure.StructureUtility

/** Uses one selector for check and a trigger-aware selector for hints/placement, exactly as the Java API. */
public fun <T, Key> partitionByCheckAndTrigger(
    elements: Map<Key, IStructureElement<T>>,
    checkKey: T.() -> Key,
    triggerKey: T.(ItemStack?) -> Key,
    fallback: IStructureElement<T>? = null,
): IStructureElementDeferred<T> {
    val checkSelector = Function<T, Key> { context ->
        checkKey.invoke(context)
    }
    val triggerSelector = BiFunction<T, ItemStack?, Key> { context, trigger ->
        triggerKey.invoke(
            context,
            trigger,
        )
    }
    if (fallback == null) {
        return StructureUtility.partitionBy(
            checkSelector,
            triggerSelector,
            elements,
        )
    }
    return StructureUtility.partitionBy(
        checkSelector,
        triggerSelector,
        elements,
        fallback,
    )
}

public fun <T> partitionByCheckAndTriggerIndex(
    elements: List<IStructureElement<T>>,
    checkIndex: T.() -> Int,
    triggerIndex: T.(ItemStack?) -> Int,
): IStructureElementDeferred<T> {
    return StructureUtility.partitionBy(
        Function<T, Int> { context ->
            checkIndex.invoke(context)
        },
        BiFunction<T, ItemStack?, Int> { context, trigger ->
            triggerIndex.invoke(
                context,
                trigger,
            )
        },
        elements,
    )
}

public fun <T> partitionByIndex(
    vararg elements: IStructureElement<T>,
    index: T.() -> Int,
): IStructureElementDeferred<T> {
    return StructureUtility.partitionBy(
        Function<T, Int> { context ->
            index.invoke(context)
        },
        *elements,
    )
}

public fun <T> partitionByIndexWithTrigger(
    vararg elements: IStructureElement<T>,
    index: T.(ItemStack?) -> Int,
): IStructureElementDeferred<T> {
    return StructureUtility.defer(
        BiFunction<T, ItemStack?, Int> { context, trigger ->
            index.invoke(
                context,
                trigger,
            )
        },
        *elements,
    )
}
