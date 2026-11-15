package space.impact.structures.kotlinx.constructable

import net.minecraft.item.ItemStack
import space.impact.structures.alignment.constructable.IMultiblockInfoContainer
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.structure.ISurvivalBuildEnvironment

public fun <T> multiblockInfoContainer(
    description: (ItemStack) -> List<String> = {
        emptyList()
    },
    survival: T.(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
        facing: ExtendedFacing,
    ) -> Int = { _, _, _, _ ->
        -2
    },
    construct: T.(
        trigger: ItemStack,
        hintsOnly: Boolean,
        facing: ExtendedFacing,
    ) -> Unit,
): IMultiblockInfoContainer<T> {
    return MultiblockInfoContainer(
        description = description,
        survivalAction = survival,
        constructAction = construct,
    )
}

public inline fun <reified T : Any> registerContainer(container: IMultiblockInfoContainer<in T>) {
    IMultiblockInfoContainer.registerTileClass(
        T::class.java,
        container,
    )
}

public inline fun <reified T : Any> containerOrNull(): IMultiblockInfoContainer<T>? {
    return IMultiblockInfoContainer.get<T>(T::class.java)
}
