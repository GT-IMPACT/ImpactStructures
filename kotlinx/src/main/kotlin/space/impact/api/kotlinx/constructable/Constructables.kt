package space.impact.api.kotlinx.constructable

import net.minecraft.item.ItemStack
import space.impact.api.alignment.constructable.IConstructable
import space.impact.api.alignment.constructable.IConstructableProvider
import space.impact.api.alignment.constructable.ISurvivalConstructable
import space.impact.api.structure.IStructureDefinition
import space.impact.api.structure.ISurvivalBuildEnvironment

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
