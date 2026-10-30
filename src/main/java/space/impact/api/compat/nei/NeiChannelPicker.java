package space.impact.api.compat.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The optional implementation is resolved only after checking that NEI is present. */
@SideOnly(Side.CLIENT)
public final class NeiChannelPicker {
    private NeiChannelPicker() {}

    public static ItemStack getStackMouseOver(GuiContainer gui) {
        return Loader.isModLoaded("NotEnoughItems") ? Available.getStackMouseOver(gui) : null;
    }

    private static final class Available {
        private static ItemStack getStackMouseOver(GuiContainer gui) {
            return codechicken.nei.NEIClientConfig.isHidden() ? null
                    : codechicken.nei.guihook.GuiContainerManager.getStackMouseOver(gui);
        }
    }
}
