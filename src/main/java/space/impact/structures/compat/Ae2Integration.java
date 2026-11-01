package space.impact.structures.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import space.impact.structures.util.InventoryUtility;

public final class Ae2Integration {
    private Ae2Integration() {}

    public static void register() {
        InventoryUtility.registerStackExtractor("1000-ae2-wireless", new WirelessExtractor() {
            @Override
            public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
                // Addon terminals belong exclusively to their own adapter, even if that adapter failed to load.
                return source != null && AEApi.instance().definitions().items().wirelessTerminal().isSameAs(source);
            }
        });
        InventoryUtility.registerStackExtractor("1000-ae2-portable-cell", new PortableExtractor());
    }

    static final class PortableExtractor extends MeExtractor {
        @Override
        public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
            return source != null && AEApi.instance().definitions().items().portableCell().isSameAs(source);
        }

        @Override
        @SuppressWarnings("unchecked")
        Session connect(ItemStack source, EntityPlayerMP player) {
            if (!(source.getItem() instanceof IAEItemPowerStorage)) return null;
            IAEItemPowerStorage battery = (IAEItemPowerStorage) source.getItem();
            IEnergySource energy = (amount, action, multiplier) -> {
                double requested = multiplier.multiply(amount);
                double available = Math.min(requested, battery.getAECurrentPower(source));
                return multiplier.divide(action == Actionable.SIMULATE
                        ? available : battery.extractAEPower(source, available));
            };
            IMEInventory<IAEItemStack> inventory = AEApi.instance().registries().cell()
                    .getCellInventory(source, null, StorageChannel.ITEMS);
            return inventory == null ? null : new Session(inventory, energy, item -> true);
        }
    }
}
