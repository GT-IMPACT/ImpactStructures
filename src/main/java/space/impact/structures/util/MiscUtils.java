package space.impact.structures.util;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * not related to a certain mod!
 */
public class MiscUtils {

    // A vanilla Item reads the stored damage field without invoking mod-specific overrides.
    // It does not need registration or the Items bootstrap singleton.
    private static final Item RAW_DAMAGE_READER = new Item();

    private MiscUtils() {}

    public static Set<String> getTagKeys(NBTTagCompound tag) {
        return tag.func_150296_c();
    }

    public static int getRawItemDamage(ItemStack stack) {
        return RAW_DAMAGE_READER.getDamage(stack);
    }
}
