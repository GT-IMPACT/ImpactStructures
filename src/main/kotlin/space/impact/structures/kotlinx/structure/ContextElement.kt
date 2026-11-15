package space.impact.structures.kotlinx.structure

import java.util.function.Consumer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.util.IChatComponent
import net.minecraft.world.World
import space.impact.structures.structure.AutoPlaceEnvironment
import space.impact.structures.structure.IItemSource
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.IStructureElement.BlocksToPlace
import space.impact.structures.structure.IStructureElement.PlaceResult

internal class ContextElement<T, Context>(
    private val element: IStructureElement<Context>,
    private val context: T.() -> Context,
) : IStructureElement<T> {

    override fun check(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
    ): Boolean {
        return element.check(
            context.invoke(owner),
            world,
            x,
            y,
            z,
        )
    }

    override fun spawnHint(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        trigger: ItemStack?,
    ): Boolean {
        return element.spawnHint(
            context.invoke(owner),
            world,
            x,
            y,
            z,
            trigger,
        )
    }

    override fun placeBlock(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        trigger: ItemStack?,
    ): Boolean {
        return element.placeBlock(
            context.invoke(owner),
            world,
            x,
            y,
            z,
            trigger,
        )
    }

    override fun couldBeValid(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        trigger: ItemStack?,
    ): Boolean {
        return element.couldBeValid(
            context.invoke(owner),
            world,
            x,
            y,
            z,
            trigger,
        )
    }

    override fun getDescription(owner: T): List<String>? {
        return element.getDescription(context.invoke(owner))
    }

    override fun getChannels(): Collection<String> {
        return element.channels
    }

    override fun getBlocksToPlace(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        trigger: ItemStack?,
        environment: AutoPlaceEnvironment,
    ): BlocksToPlace? {
        return element.getBlocksToPlace(
            context.invoke(owner),
            world,
            x,
            y,
            z,
            trigger,
            environment,
        )
    }

    override fun survivalPlaceBlock(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        trigger: ItemStack?,
        environment: AutoPlaceEnvironment,
    ): PlaceResult {
        return element.survivalPlaceBlock(
            context.invoke(owner),
            world,
            x,
            y,
            z,
            trigger,
            environment,
        )
    }

    @Deprecated("Use the environment overload.")
    @Suppress("DEPRECATION")
    override fun survivalPlaceBlock(
        owner: T,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        trigger: ItemStack?,
        source: IItemSource,
        actor: EntityPlayerMP?,
        chatter: Consumer<IChatComponent>?,
    ): PlaceResult {
        return element.survivalPlaceBlock(
            context.invoke(owner),
            world,
            x,
            y,
            z,
            trigger,
            source,
            actor,
            chatter,
        )
    }
}
