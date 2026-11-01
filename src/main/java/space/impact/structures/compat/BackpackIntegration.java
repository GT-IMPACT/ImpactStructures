package space.impact.structures.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import de.eydamos.backpack.Backpack;
import de.eydamos.backpack.inventory.InventoryBackpack;
import de.eydamos.backpack.inventory.InventoryBackpackSlot;
import de.eydamos.backpack.inventory.container.ContainerAdvanced;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.saves.BackpackSave;
import de.eydamos.backpack.saves.PlayerSave;
import de.eydamos.backpack.util.BackpackUtil;
import space.impact.structures.util.ExtractionScope;
import space.impact.structures.util.InventoryIterable;
import space.impact.structures.util.InventoryUtility;
import space.impact.structures.util.InventoryUtility.InventoryProvider;
import space.impact.structures.util.InventoryUtility.ItemStackCounter;
import space.impact.structures.util.InventoryUtility.ItemStackExtractor;

public final class BackpackIntegration implements ItemStackExtractor {
    public static void register() {
        InventoryUtility.registerStackExtractor("1000-backpack", new BackpackIntegration());
        InventoryUtility.registerInventoryProvider("5002-equipped-backpack", equippedProvider());
        InventoryUtility.registerEnableEnderCondition(player -> {
            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack != null && stack.getItem() instanceof ItemBackpackBase
                        && BackpackUtil.isEnderBackpack(stack)) return true;
            }
            PlayerSave save = equippedSave(player);
            ItemStack equipped = save == null ? null : save.getPersonalBackpack();
            return equipped != null && BackpackUtil.isEnderBackpack(equipped);
        });
    }

    static InventoryProvider<EquippedBackpack> equippedProvider() {
        return new InventoryProvider<EquippedBackpack>() {
            @Override
            public EquippedBackpack getInventory(EntityPlayerMP player) {
                PlayerSave save = equippedSave(player);
                return save != null && save.hasPersonalBackpack() ? new EquippedBackpack(save) : null;
            }

            @Override
            public void markDirty(EquippedBackpack inventory) {
                inventory.getInventory().markDirty();
                inventory.getInventory().writeToNBT(inventory.save);
            }
        };
    }

    private static PlayerSave equippedSave(EntityPlayerMP player) {
        if (player == null || !BackpackUtil.isServerSide() || Backpack.saveFileHandler == null
                || player.openContainer instanceof ContainerAdvanced) return null;
        String uid = player.getUniqueID().toString();
        return Backpack.saveFileHandler.playerSaveExists(uid) ? new PlayerSave(uid) : null;
    }

    static final class EquippedBackpack extends InventoryIterable<InventoryBackpackSlot> {
        private final PlayerSave save;

        EquippedBackpack(PlayerSave save) {
            super(new InventoryBackpackSlot(save));
            this.save = save;
        }
    }

    @Override
    public boolean isAPIImplemented(APIType type) {
        return type == APIType.MAIN || type == APIType.IS_VALID_SOURCE;
    }

    @Override
    public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
        return source != null && source.getItem() instanceof ItemBackpackBase;
    }

    @Override
    public int takeFromStack(Predicate<ItemStack> predicate, boolean simulate, int count, ItemStackCounter counter,
            ItemStack source, ItemStack filter, EntityPlayerMP player) {
        if (count <= 0 || !isValidSource(source, player) || BackpackUtil.isEnderBackpack(source)) return 0;
        if (!BackpackUtil.isServerSide() || source.getTagCompound() == null) return 0;
        // An open GUI owns an in-memory copy. Never overwrite its pending changes from a disk snapshot.
        if (player != null && player.openContainer instanceof ContainerAdvanced) return 0;
        String uid = source.getTagCompound().getString("backpack-UID");
        if (uid.isEmpty() || !ExtractionScope.claim("backpack:" + uid)) return 0;
        if (Backpack.saveFileHandler == null || !Backpack.saveFileHandler.backpackSaveExists(uid)) return 0;

        // The String constructor loads only; the ItemStack constructor can create and save a new backpack.
        BackpackSave save = new BackpackSave(uid);
        if (save.isUninitialized() || save.getSize() <= 0) return 0;
        InventoryBackpack inventory = new InventoryBackpack("backpack", "");
        inventory.readFromNBT(save);
        int taken = InventoryTransfer.take(inventory, predicate, simulate, count, counter);
        if (!simulate && taken > 0) {
            save.setManualSaving();
            inventory.markDirty();
            inventory.writeToNBT(save);
            save.save();
        }
        return taken;
    }
}
