package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cpw.mods.fml.common.Loader;
import de.eydamos.backpack.Backpack;
import de.eydamos.backpack.handler.SaveFileHandler;
import de.eydamos.backpack.inventory.InventoryBackpack;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.saves.BackpackSave;
import space.impact.structures.util.ExtractionScope;

class BackpackInventoryTest {
    @TempDir Path directory;

    @Test
    void diskBackpackSimulationIsReadOnlyAndRealChangesPersistWithoutDuplicateCounting() throws Exception {
        Field loaderInstance = Loader.class.getDeclaredField("instance");
        loaderInstance.setAccessible(true);
        Object previousLoader = loaderInstance.get(null);
        Loader loader = mock(Loader.class);
        when(loader.getIndexedModList()).thenReturn(Collections.emptyMap());
        loaderInstance.set(null, loader);
        String previousThread = Thread.currentThread().getName();
        Thread.currentThread().setName("Server thread");
        SaveFileHandler previousHandler = null;
        try {
            previousHandler = Backpack.saveFileHandler;
            Backpack.saveFileHandler = new TestSaveHandler(directory.toFile());
            Item material = new Item();
            TestItems.register(31002, "backpack_material", material);
            ItemBackpackBase bag = mock(ItemBackpackBase.class);
            TestItems.initializeMock(bag);
            TestItems.register(31003, "backpack_equipped", bag);
            ItemStack source = new ItemStack(bag, 1, 0);
            source.setTagCompound(new NBTTagCompound());
            source.getTagCompound().setString("backpack-UID", "test-bag");
            NBTTagCompound data = new NBTTagCompound();
            data.setString("backpack-UID", "test-bag");
            data.setInteger("size", 54);
            data.setInteger("slotsPerRow", 9);
            data.setByte("type", (byte) 1);
            data.setTag("backpackInventories", new NBTTagCompound());
            BackpackSave save = new BackpackSave(data);
            InventoryBackpack contents = new InventoryBackpack("test", "");
            contents.readFromNBT(save);
            contents.setInventorySlotContents(40, new ItemStack(material, 6, 2));
            save.setManualSaving();
            contents.writeToNBT(save);
            save.save();
            Path savedFile = directory.resolve("test-bag.dat");
            byte[] before = Files.readAllBytes(savedFile);
            EntityPlayerMP player = mock(EntityPlayerMP.class);
            UUID playerId = UUID.randomUUID();
            when(player.getUniqueID()).thenReturn(playerId);
            NBTTagCompound playerData = new NBTTagCompound();
            playerData.setTag("personalBackpack", source.writeToNBT(new NBTTagCompound()));
            Backpack.saveFileHandler.savePlayer(playerData, playerId.toString());
            Path playerFile = directory.resolve(playerId + ".dat");
            byte[] playerBefore = Files.readAllBytes(playerFile);
            BackpackIntegration.EquippedBackpack equipped = BackpackIntegration.equippedProvider().getInventory(player);
            assertEquals(bag, equipped.getInventory().getStackInSlot(0).getItem());
            BackpackIntegration adapter = new BackpackIntegration();
            try (ExtractionScope scope = ExtractionScope.open()) {
                assertEquals(6, adapter.takeFromStack(s -> s.getItem() == material, true, 8,
                        (s, n) -> {}, source, null, null));
                assertEquals(0, adapter.takeFromStack(s -> s.getItem() == material, true, 8,
                        (s, n) -> {}, equipped.getInventory().getStackInSlot(0), null, player));
            }
            assertArrayEquals(before, Files.readAllBytes(savedFile));
            assertArrayEquals(playerBefore, Files.readAllBytes(playerFile));
            try (ExtractionScope scope = ExtractionScope.open()) {
                assertEquals(4, adapter.takeFromStack(s -> s.getItem() == material, false, 4,
                        (s, n) -> {}, equipped.getInventory().getStackInSlot(0), null, player));
            }
            BackpackIntegration.equippedProvider().markDirty(equipped);
            InventoryBackpack reopened = new InventoryBackpack("test", "");
            reopened.readFromNBT(new BackpackSave("test-bag"));
            assertEquals(2, reopened.getStackInSlot(40).stackSize);
            assertEquals(2, reopened.getStackInSlot(40).getItemDamage());

            ItemStack uninitialized = new ItemStack(bag);
            assertEquals(0, adapter.takeFromStack(s -> true, true, 1, (s, n) -> {}, uninitialized, null, null));
            assertNull(uninitialized.getTagCompound());
            when(player.getUniqueID()).thenReturn(UUID.randomUUID());
            long fileCount = directory.toFile().list().length;
            assertNull(BackpackIntegration.equippedProvider().getInventory(player));
            assertEquals(fileCount, directory.toFile().list().length);
        } finally {
            if (previousHandler != null) Backpack.saveFileHandler = previousHandler;
            Thread.currentThread().setName(previousThread);
            loaderInstance.set(null, previousLoader);
        }
    }

    private static final class TestSaveHandler extends SaveFileHandler {
        TestSaveHandler(File directory) { backpackDir = directory; playerDir = directory; }
    }
}
