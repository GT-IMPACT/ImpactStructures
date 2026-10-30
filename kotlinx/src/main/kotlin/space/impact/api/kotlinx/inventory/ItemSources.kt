package space.impact.api.kotlinx.inventory

import java.util.function.Predicate
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import space.impact.api.structure.IItemSource

public fun itemSource(
    extract: (
        predicate: (ItemStack) -> Boolean,
        simulate: Boolean,
        count: Int,
    ) -> Map<ItemStack, Int>,
): IItemSource {
    return ItemSource(extract)
}

public fun IInventory.itemSource(): IItemSource {
    return IItemSource.fromInventory(this)
}

public fun EntityPlayerMP.itemSource(): IItemSource {
    return IItemSource.fromPlayer(this)
}

public fun IItemSource.take(
    count: Int,
    simulate: Boolean = false,
    predicate: (ItemStack) -> Boolean,
): Map<ItemStack, Int> {
    require(value = count >= 0) {
        "The requested item count must not be negative"
    }
    return take(
        Predicate { stack ->
            predicate(stack)
        },
        simulate,
        count,
    )
}

public fun IItemSource.takeOne(
    simulate: Boolean = false,
    predicate: (ItemStack) -> Boolean,
): ItemStack? {
    return takeOne(
        Predicate { stack ->
            predicate(stack)
        },
        simulate,
    )
}

public fun IItemSource.takeAll(
    count: Int,
    simulate: Boolean = false,
    predicate: (ItemStack) -> Boolean,
): Boolean {
    require(value = count >= 0) {
        "The requested item count must not be negative"
    }
    return takeAll(
        Predicate { stack ->
            predicate(stack)
        },
        simulate,
        count,
    )
}
