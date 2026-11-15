package space.impact.structures.kotlinx.constructable

import net.minecraft.item.ItemStack
import space.impact.structures.alignment.constructable.ISurvivalConstructable
import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.structure.ISurvivalBuildEnvironment

internal class SurvivalConstructable<T>(
    context: T,
    description: T.(ItemStack) -> List<String>,
    definition: IStructureDefinition<*>?,
    private val survivalAction: T.(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
    ) -> Int,
    constructAction: T.(
        trigger: ItemStack,
        hintsOnly: Boolean,
    ) -> Unit,
) : Constructable<T>(
    context = context,
    description = description,
    definition = definition,
    constructAction = constructAction,
), ISurvivalConstructable {

    public override fun survivalConstruct(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
    ): Int {
        require(budget >= 0)
        return survivalAction(
            context,
            trigger,
            budget,
            environment,
        )
    }
}
