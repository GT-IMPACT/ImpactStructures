package space.impact.structures.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryViewCell;

import appeng.api.features.IWirelessTermHandler;
import space.impact.structures.util.InventoryUtility;

public final class WctIntegration extends WirelessExtractor {
    public static void register() {
        InventoryUtility.registerStackExtractor("0990-ae2wct", new WctIntegration());
    }

    @Override
    public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
        return source != null && source.getItem() instanceof IWirelessCraftingTerminalItem;
    }

    @Override
    boolean hasInfiniteRange(ItemStack source, IWirelessTermHandler handler) {
        return ((IWirelessCraftingTerminalItem) source.getItem()).checkForBooster(source)
                || super.hasInfiniteRange(source, handler);
    }

    @Override
    IInventory viewCells(ItemStack source) {
        return new WCTInventoryViewCell(source);
    }
}
