package space.impact.structures.kotlinx.structure

import net.minecraft.item.ItemStack
import net.minecraft.world.World
import space.impact.structures.structure.AutoPlaceEnvironment
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.IStructureElement.BlocksToPlace
import space.impact.structures.structure.IStructureElement.PlaceResult

/** Callbacks receive primitive coordinates directly and allocate no position wrappers. */
public fun <T> customElement(
    check: CheckCallback<T>,
    hint: HintCallback<T>? = null,
    place: HintCallback<T>? = null,
    survival: SurvivalCallback<T>? = null,
    description: (T.() -> List<String>?)? = null,
    channels: Collection<String> = emptySet(),
    couldBeValid: HintCallback<T>? = null,
    blocksToPlace: BlocksCallback<T>? = null,
): IStructureElement<T> {
    val channelNames = channels.toList()
    return object : IStructureElement<T> {
        override fun check(
            context: T,
            world: World,
            x: Int,
            y: Int,
            z: Int,
        ): Boolean {
            return check.invoke(
                context,
                world,
                x,
                y,
                z,
            )
        }

        override fun spawnHint(
            context: T,
            world: World,
            x: Int,
            y: Int,
            z: Int,
            trigger: ItemStack?,
        ): Boolean {
            return hint?.invoke(
                context,
                world,
                x,
                y,
                z,
                trigger,
            ) ?: false
        }

        override fun placeBlock(
            context: T,
            world: World,
            x: Int,
            y: Int,
            z: Int,
            trigger: ItemStack?,
        ): Boolean {
            return place?.invoke(
                context,
                world,
                x,
                y,
                z,
                trigger,
            ) ?: false
        }

        override fun couldBeValid(
            context: T,
            world: World,
            x: Int,
            y: Int,
            z: Int,
            trigger: ItemStack?,
        ): Boolean {
            return couldBeValid?.invoke(
                context,
                world,
                x,
                y,
                z,
                trigger,
            ) ?: true
        }

        override fun getChannels(): Collection<String> {
            return channelNames
        }

        override fun getDescription(context: T): List<String>? {
            return description?.invoke(context)
        }

        override fun getBlocksToPlace(
            context: T,
            world: World,
            x: Int,
            y: Int,
            z: Int,
            trigger: ItemStack?,
            environment: AutoPlaceEnvironment,
        ): BlocksToPlace? {
            return blocksToPlace?.invoke(
                context,
                world,
                x,
                y,
                z,
                trigger,
                environment,
            )
        }

        override fun survivalPlaceBlock(
            context: T,
            world: World,
            x: Int,
            y: Int,
            z: Int,
            trigger: ItemStack?,
            environment: AutoPlaceEnvironment,
        ): PlaceResult {
            if (survival != null) {
                return survival.invoke(
                    context,
                    world,
                    x,
                    y,
                    z,
                    trigger,
                    environment,
                )
            }
            return super<IStructureElement>.survivalPlaceBlock(
                context,
                world,
                x,
                y,
                z,
                trigger,
                environment,
            )
        }
    }
}
