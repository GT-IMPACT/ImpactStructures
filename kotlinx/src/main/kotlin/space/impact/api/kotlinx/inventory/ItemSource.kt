package space.impact.api.kotlinx.inventory

import java.util.function.Predicate
import net.minecraft.item.ItemStack
import space.impact.api.structure.IItemSource

internal class ItemSource(
    private val extract: (
        predicate: (ItemStack) -> Boolean,
        simulate: Boolean,
        count: Int,
    ) -> Map<ItemStack, Int>,
) : IItemSource {

    public override fun take(
        predicate: Predicate<ItemStack>,
        simulate: Boolean,
        count: Int,
    ): Map<ItemStack, Int> {
        require(count >= 0)
        return extract(
            { stack ->
                predicate.test(stack)
            },
            simulate,
            count,
        )
    }
}
