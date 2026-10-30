package space.impact.api.structure;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import space.impact.api.ImpactAPI;
import space.impact.api.hint.HintColor;

/** Rendering-only decorator; every structure operation stays with the original element. */
final class HintedStructureElement<T> implements IStructureElement<T> {

    private final IStructureElement<T> element;
    private final HintColor color;

    HintedStructureElement(IStructureElement<T> element, HintColor color) {
        this.element = Objects.requireNonNull(element, "element");
        this.color = Objects.requireNonNull(color, "color");
    }

    @Override
    public boolean check(T context, World world, int x, int y, int z) {
        return element.check(context, world, x, y, z);
    }

    @Override
    public boolean couldBeValid(T context, World world, int x, int y, int z, ItemStack trigger) {
        return element.couldBeValid(context, world, x, y, z, trigger);
    }

    @Override
    public boolean spawnHint(T context, World world, int x, int y, int z, ItemStack trigger) {
        boolean spawned = element.spawnHint(context, world, x, y, z, trigger);
        if (world != null && world.isRemote) ImpactAPI.hintFrame(world, x, y, z, color);
        return spawned;
    }

    @Override
    public boolean placeBlock(T context, World world, int x, int y, int z, ItemStack trigger) {
        return element.placeBlock(context, world, x, y, z, trigger);
    }

    @Override
    public BlocksToPlace getBlocksToPlace(T context, World world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment environment) {
        return element.getBlocksToPlace(context, world, x, y, z, trigger, environment);
    }

    @Override
    public PlaceResult survivalPlaceBlock(T context, World world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment environment) {
        return element.survivalPlaceBlock(context, world, x, y, z, trigger, environment);
    }

    @Override
    @Deprecated
    public PlaceResult survivalPlaceBlock(T context, World world, int x, int y, int z, ItemStack trigger,
            IItemSource source, EntityPlayerMP actor, Consumer<IChatComponent> chatter) {
        return element.survivalPlaceBlock(context, world, x, y, z, trigger, source, actor, chatter);
    }

    @Override
    public Collection<String> getChannels() {
        return element.getChannels();
    }

    @Override
    public List<String> getDescription(T context) {
        return element.getDescription(context);
    }

    @Override
    public int getStepA() { return element.getStepA(); }

    @Override
    public int getStepB() { return element.getStepB(); }

    @Override
    public int getStepC() { return element.getStepC(); }

    @Override
    public boolean resetA() { return element.resetA(); }

    @Override
    public boolean resetB() { return element.resetB(); }

    @Override
    public boolean resetC() { return element.resetC(); }

    @Override
    public boolean isNavigating() { return element.isNavigating(); }
}
