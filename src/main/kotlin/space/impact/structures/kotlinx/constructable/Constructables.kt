package space.impact.structures.kotlinx.constructable

import net.minecraft.item.ItemStack
import space.impact.structures.alignment.constructable.IConstructable
import space.impact.structures.alignment.constructable.IConstructableProvider
import space.impact.structures.alignment.constructable.ISurvivalConstructable
import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.structure.ISurvivalBuildEnvironment

public fun <T> constructable(
    context: T,
    description: T.(ItemStack) -> List<String> = {
        emptyList()
    },
    definition: IStructureDefinition<*>? = null,
    construct: T.(
        trigger: ItemStack,
        hintsOnly: Boolean,
    ) -> Unit,
): IConstructable {
    return Constructable(
        context = context,
        description = description,
        definition = definition,
        constructAction = construct,
    )
}

public fun <T> survivalConstructable(
    context: T,
    description: T.(ItemStack) -> List<String> = {
        emptyList()
    },
    definition: IStructureDefinition<*>? = null,
    survival: T.(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
    ) -> Int,
    construct: T.(
        trigger: ItemStack,
        hintsOnly: Boolean,
    ) -> Unit,
): ISurvivalConstructable {
    return SurvivalConstructable(
        context = context,
        description = description,
        definition = definition,
        survivalAction = survival,
        constructAction = construct,
    )
}

public fun constructableProvider(resolve: () -> IConstructable?): IConstructableProvider {
    return IConstructableProvider {
        resolve()
    }
}
