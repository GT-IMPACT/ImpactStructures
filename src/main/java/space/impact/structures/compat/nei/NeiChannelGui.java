package space.impact.structures.compat.nei;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import space.impact.structures.gui.GuiScreenConfigureChannels;

/** Loaded only on a client with NEI installed. */
@SideOnly(Side.CLIENT)
public final class NeiChannelGui extends GuiScreenConfigureChannels implements INEIGuiHandler {
    public NeiChannelGui(Container container, ItemStack trigger) {
        super(container, trigger);
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return null;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return Collections.emptyList();
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack stack, int button) {
        if (!acceptIndicatorDrop(mousex, mousey, stack, button)) return false;
        // NEI passes a temporary dragged stack, rather than an inventory slot.
        stack.stackSize = 0;
        return true;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int width, int height) {
        return false;
    }
}
