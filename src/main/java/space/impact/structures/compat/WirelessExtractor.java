package space.impact.structures.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.items.contents.WirelessTerminalViewCells;
import appeng.items.storage.ItemViewCell;
import appeng.tile.networking.TileWireless;
import appeng.util.prioitylist.IPartitionList;

abstract class WirelessExtractor extends MeExtractor {
    @Override
    Session connect(ItemStack source, EntityPlayerMP player) {
        IWirelessTermHandler handler = AEApi.instance().registries().wireless().getWirelessTerminalHandler(source);
        if (handler == null) return null;
        long key;
        try {
            key = Long.parseLong(handler.getEncryptionKey(source));
        } catch (NumberFormatException e) {
            return null;
        }
        ILocatable target = AEApi.instance().registries().locatable().getLocatableBy(key);
        if (!(target instanceof IGridHost)) return null;
        IGridNode node = ((IGridHost) target).getGridNode(ForgeDirection.UNKNOWN);
        if (node == null || !node.isActive()) return null;
        IGrid grid = node.getGrid();
        if (grid == null) return null;
        if (!canAccess(grid, player, hasInfiniteRange(source, handler))) return null;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        if (storage == null || storage.getItemInventory() == null) return null;
        if (!(source.getItem() instanceof IAEItemPowerStorage)) return null;
        IAEItemPowerStorage battery = (IAEItemPowerStorage) source.getItem();
        IEnergySource energy = (amount, action, multiplier) -> {
            if (handler.hasInfinityPower(source)) return amount;
            double available = Math.min(multiplier.multiply(amount), battery.getAECurrentPower(source));
            if (available <= 0 || !handler.hasPower(player, available, source)) return 0;
            if (action == Actionable.MODULATE && !handler.usePower(player, available, source)) return 0;
            return multiplier.divide(available);
        };
        // Read view cells from a detached stack: these inventory constructors can normalize NBT.
        IInventory cells = viewCells(source.copy());
        ItemStack[] contents = new ItemStack[cells.getSizeInventory()];
        for (int i = 0; i < contents.length; i++) contents[i] = cells.getStackInSlot(i);
        IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(contents);
        return new Session(storage.getItemInventory(), energy,
                item -> filter == null || filter.isEmpty() || filter.isListed(item));
    }

    boolean hasInfiniteRange(ItemStack source, IWirelessTermHandler handler) {
        return handler.hasInfinityRange(source);
    }

    IInventory viewCells(ItemStack source) {
        return new WirelessTerminalViewCells(source);
    }

    static boolean canAccess(IGrid grid, EntityPlayerMP player, boolean infiniteRange) {
        ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        return security != null && security.hasPermission(player, SecurityPermissions.EXTRACT)
                && (infiniteRange || inRange(grid, player));
    }

    static boolean inRange(IGrid grid, EntityPlayerMP player) {
        for (IGridNode node : grid.getMachines(TileWireless.class)) {
            if (!(node.getMachine() instanceof IWirelessAccessPoint)) continue;
            IWirelessAccessPoint access = (IWirelessAccessPoint) node.getMachine();
            if (!access.isActive()) continue;
            DimensionalCoord position = access.getLocation();
            if (position == null || !position.isInWorld(player.worldObj)) continue;
            double dx = player.posX - position.x;
            double dy = player.posY - position.y;
            double dz = player.posZ - position.z;
            double range = access.getRange();
            if (dx * dx + dy * dy + dz * dz < range * range) return true;
        }
        return false;
    }
}
