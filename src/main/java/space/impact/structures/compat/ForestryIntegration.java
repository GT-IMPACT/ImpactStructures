package space.impact.structures.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;
import space.impact.structures.util.InventoryUtility;
import space.impact.structures.util.InventoryUtility.ItemStackCounter;
import space.impact.structures.util.InventoryUtility.ItemStackExtractor;

public final class ForestryIntegration implements ItemStackExtractor {
    public static void register() {
        InventoryUtility.registerStackExtractor("1000-forestry-backpack", new ForestryIntegration());
    }

    @Override
    public boolean isAPIImplemented(APIType type) {
        return type == APIType.MAIN || type == APIType.IS_VALID_SOURCE;
    }

    @Override
    public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
        return source != null && source.getItem() instanceof ItemBackpack;
    }

    @Override
    public int takeFromStack(Predicate<ItemStack> predicate, boolean simulate, int count, ItemStackCounter counter,
            ItemStack source, ItemStack filter, EntityPlayerMP player) {
        if (count <= 0 || !isValidSource(source, player)) return 0;
        // Forestry creates a UID and normalizes legacy NBT while opening an inventory.
        final ItemStack working = source.copy();
        ItemInventoryBackpack inventory = new ItemInventoryBackpack(
                null, ((ItemBackpack) source.getItem()).getBackpackSize(), working) {
            @Override
            protected ItemStack getParent() {
                return working;
            }
        };
        int taken = InventoryTransfer.take(inventory, predicate, simulate, count, counter);
        if (!simulate && taken > 0) {
            inventory.markDirty();
            source.setTagCompound(working.getTagCompound());
        }
        return taken;
    }
}
