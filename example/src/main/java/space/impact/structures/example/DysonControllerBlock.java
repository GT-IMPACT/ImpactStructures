package space.impact.structures.example;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public final class DysonControllerBlock extends BlockContainer {

    public DysonControllerBlock() {
        super(Material.rock);
        setBlockName("impactstructures_example.dyson_controller");
        setBlockTextureName("minecraft:hardened_clay_stained_black");
        setHardness(3);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new DysonController();
    }

    @Override
    public boolean onBlockActivated(
            World world,
            int x,
            int y,
            int z,
            EntityPlayer player,
            int side,
            float hitX,
            float hitY,
            float hitZ
    ) {
        if (!world.isRemote) {
            DysonController controller = (DysonController) world.getTileEntity(x, y, z);
            if (!controller.toggle()) {
                player.addChatMessage(new ChatComponentText("Dyson frame is incomplete or not fully loaded."));
            } else {
                player.addChatMessage(new ChatComponentText(controller.isRunning()
                        ? "Dyson sphere started." : "Dyson sphere stopped; original blocks restored."));
            }
        }
        return true;
    }
}
