package space.impact.structures.compat;

import java.util.function.Predicate;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import space.impact.structures.util.InventoryUtility.ItemStackCounter;

/** Moves only direct contents; container items are never opened recursively. */
final class InventoryTransfer {
    private InventoryTransfer() {}

    static int take(IInventory inventory, Predicate<ItemStack> predicate, boolean simulate, int count,
            ItemStackCounter counter) {
        int taken = 0;
        for (int slot = 0; slot < inventory.getSizeInventory() && taken < count; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0 || !predicate.test(stack)) continue;
            int amount = Math.min(count - taken, stack.stackSize);
            counter.add(stack.copy(), amount);
            if (!simulate) {
                ItemStack remaining = stack.copy();
                remaining.stackSize -= amount;
                inventory.setInventorySlotContents(slot, remaining.stackSize == 0 ? null : remaining);
            }
            taken += amount;
        }
        return taken;
    }
}
