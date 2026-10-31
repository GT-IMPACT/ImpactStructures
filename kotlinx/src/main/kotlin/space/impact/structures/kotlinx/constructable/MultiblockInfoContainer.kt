package space.impact.structures.kotlinx.constructable

import net.minecraft.item.ItemStack
import space.impact.structures.alignment.constructable.IMultiblockInfoContainer
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.structure.ISurvivalBuildEnvironment

internal class MultiblockInfoContainer<T>(
    private val description: (ItemStack) -> List<String>,
    private val survivalAction: T.(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
        facing: ExtendedFacing,
    ) -> Int,
    private val constructAction: T.(
        trigger: ItemStack,
        hintsOnly: Boolean,
        facing: ExtendedFacing,
    ) -> Unit,
) : IMultiblockInfoContainer<T> {

    public override fun construct(
        trigger: ItemStack,
        hintsOnly: Boolean,
        context: T,
        facing: ExtendedFacing,
    ) {
        constructAction(
            context,
            trigger,
            hintsOnly,
            facing,
        )
    }

    public override fun survivalConstruct(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
        context: T,
        facing: ExtendedFacing,
    ): Int {
        require(budget >= 0)
        return survivalAction(
            context,
            trigger,
            budget,
            environment,
            facing,
        )
    }

    public override fun getDescription(trigger: ItemStack): Array<String> {
        return description(trigger).toTypedArray()
    }
}
