package space.impact.structures.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.rwtema.extrautils.item.ItemGoldenBag;

import space.impact.structures.util.InventoryUtility;
import space.impact.structures.util.InventoryUtility.ItemStackCounter;
import space.impact.structures.util.InventoryUtility.ItemStackExtractor;

/** Extra Utilities 1.2.12 stores each Golden Bag slot directly in the item's NBT. */
public final class ExtraUtilitiesIntegration implements ItemStackExtractor {
    public static void register() {
        InventoryUtility.registerStackExtractor("1000-extrautilities-golden-bag", new ExtraUtilitiesIntegration());
    }

    @Override
    public boolean isAPIImplemented(APIType type) {
        return type == APIType.MAIN || type == APIType.IS_VALID_SOURCE;
    }

    @Override
    public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
        return source != null && source.getItem() instanceof ItemGoldenBag;
    }

    @Override
    public int takeFromStack(Predicate<ItemStack> predicate, boolean simulate, int count, ItemStackCounter counter,
            ItemStack source, ItemStack filter, EntityPlayerMP player) {
        if (count <= 0 || !isValidSource(source, player) || !source.hasTagCompound()) return 0;
        NBTTagCompound contents = source.getTagCompound();
        int taken = 0;
        for (int slot = 0; slot < 54 && taken < count; slot++) {
            String key = "items_" + slot;
            if (!contents.hasKey(key, 10)) continue;
            ItemStack stack = ItemStack.loadItemStackFromNBT(contents.getCompoundTag(key));
            if (stack == null || stack.stackSize <= 0 || !predicate.test(stack)) continue;
            int amount = Math.min(count - taken, stack.stackSize);
            counter.add(stack.copy(), amount);
            taken += amount;
            if (!simulate) {
                stack.stackSize -= amount;
                if (stack.stackSize == 0) contents.removeTag(key);
                else contents.setTag(key, stack.writeToNBT(new NBTTagCompound()));
            }
        }
        return taken;
    }
}
