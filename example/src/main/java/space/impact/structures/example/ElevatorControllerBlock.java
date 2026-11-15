package space.impact.structures.example;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public final class ElevatorControllerBlock extends BlockContainer {

    public ElevatorControllerBlock() {
        super(Material.iron);
        setBlockName("impactstructures_example.elevator_controller");
        setBlockTextureName("minecraft:command_block");
        setHardness(3);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new ElevatorController();
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
            ElevatorController controller = (ElevatorController) world.getTileEntity(x, y, z);
            if (!controller.toggle()) {
                player.addChatMessage(new ChatComponentText("Elevator structure is incomplete."));
            } else {
                player.addChatMessage(new ChatComponentText(controller.isRunning()
                        ? "Elevator started. Right-click again to return to dock." : "Elevator returning to dock."));
            }
        }
        return true;
    }
}
