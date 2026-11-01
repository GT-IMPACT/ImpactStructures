package space.impact.structures.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import space.impact.structures.util.InventoryUtility.ItemStackCounter;
import space.impact.structures.util.InventoryUtility.ItemStackExtractor;

abstract class MeExtractor implements ItemStackExtractor {
    abstract Session connect(ItemStack source, EntityPlayerMP player);

    @Override
    public boolean isAPIImplemented(APIType type) {
        return type == APIType.MAIN || type == APIType.IS_VALID_SOURCE;
    }

    @Override
    public final int takeFromStack(Predicate<ItemStack> predicate, boolean simulate, int count,
            ItemStackCounter counter, ItemStack source, ItemStack filter, EntityPlayerMP player) {
        if (count <= 0 || player == null || !isValidSource(source, player)) return 0;
        // Opening cells and reading terminal settings may initialize NBT, even for a query.
        Session session = connect(simulate ? source.copy() : source, player);
        if (session == null) return 0;
        return session.take(predicate, filter, simulate, count, counter, new PlayerSource(player, null),
                AEApi.instance().storage());
    }

    static final class Session {
        private final IMEInventory<IAEItemStack> inventory;
        private final IEnergySource energy;
        private final Predicate<IAEItemStack> visible;

        Session(IMEInventory<IAEItemStack> inventory, IEnergySource energy, Predicate<IAEItemStack> visible) {
            this.inventory = inventory;
            this.energy = energy;
            this.visible = visible;
        }

        int take(Predicate<ItemStack> predicate, ItemStack filter, boolean simulate, int count,
                ItemStackCounter counter, PlayerSource actor, IStorageHelper helper) {
            SimulatedEnergy budget = new SimulatedEnergy(energy);
            if (filter != null) {
                IAEItemStack request = helper.createItemStack(filter);
                return extract(request, predicate, simulate, count, counter, actor, budget);
            }
            IItemList<IAEItemStack> available = helper.createItemList();
            inventory.getAvailableItems(available);
            int taken = 0;
            for (IAEItemStack candidate : available) {
                taken += extract(candidate, predicate, simulate, count - taken, counter, actor, budget);
                if (taken >= count) break;
            }
            return taken;
        }

        private int extract(IAEItemStack candidate, Predicate<ItemStack> predicate, boolean simulate, int count,
                ItemStackCounter counter, PlayerSource actor, SimulatedEnergy budget) {
            if (count <= 0 || candidate == null || candidate.getStackSize() <= 0 || !visible.test(candidate)
                    || !predicate.test(candidate.getItemStack())) return 0;
            IAEItemStack request = candidate.copy();
            request.setStackSize(Math.min((long) count, candidate.getStackSize()));
            IAEItemStack extracted = Platform.poweredExtraction(simulate ? budget : energy, inventory, request,
                    actor, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
            if (extracted == null || extracted.getStackSize() <= 0) return 0;
            int amount = (int) Math.min((long) count, extracted.getStackSize());
            if (simulate) budget.reserve(Math.ceil((double) amount / extracted.getAmountPerUnit()));
            counter.add(extracted.getItemStack(), amount);
            return amount;
        }
    }

    /** A multi-item simulation must not spend the same energy again for every item type. */
    static final class SimulatedEnergy implements IEnergySource {
        private final IEnergySource delegate;
        private double reserved;

        SimulatedEnergy(IEnergySource delegate) {
            this.delegate = delegate;
        }

        void reserve(double amount) {
            reserved += PowerMultiplier.CONFIG.multiply(amount);
        }

        @Override
        public double extractAEPower(double amount, Actionable action, PowerMultiplier multiplier) {
            double used = multiplier.divide(reserved);
            return Math.max(0, Math.min(amount,
                    delegate.extractAEPower(amount + used, Actionable.SIMULATE, multiplier) - used));
        }
    }
}
