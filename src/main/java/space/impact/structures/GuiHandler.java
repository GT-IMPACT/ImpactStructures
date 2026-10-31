package space.impact.structures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import space.impact.structures.gui.EmptyContainer;
import space.impact.structures.gui.GuiScreenConfigureChannels;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import space.impact.structures.compat.nei.NeiChannelGui;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 0:
                return new EmptyContainer(player);
            default:
                return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (x < 0 || x >= player.inventory.mainInventory.length) return null;
        if (player.inventory.getStackInSlot(x) == null) return null;
        switch (ID) {
            case 0:
                if (Loader.isModLoaded("NotEnoughItems")) {
                    return new NeiChannelGui(new EmptyContainer(player), player.inventory.getStackInSlot(x));
                }
                return new GuiScreenConfigureChannels(new EmptyContainer(player), player.inventory.getStackInSlot(x));
            default:
                return null;
        }
    }
}
