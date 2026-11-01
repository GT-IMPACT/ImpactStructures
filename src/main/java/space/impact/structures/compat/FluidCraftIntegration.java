package space.impact.structures.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;

import space.impact.structures.util.InventoryUtility;

public final class FluidCraftIntegration extends WirelessExtractor {
    public static void register() {
        InventoryUtility.registerStackExtractor("0991-ae2fc", new FluidCraftIntegration());
    }

    @Override
    public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
        return source != null && source.getItem() instanceof ItemBaseWirelessTerminal;
    }
}
