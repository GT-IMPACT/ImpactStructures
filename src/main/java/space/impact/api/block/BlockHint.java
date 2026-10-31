package space.impact.api.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import space.impact.api.Main;
import space.impact.api.ImpactStructures;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHint extends Block {

    private static final IIcon[] hint = new IIcon[16];

    public BlockHint() {
        super(Material.iron);
        setBlockName("impactapi.blockhint");
        setCreativeTab(Main.creativeTab);
    }

    @Override
    public void registerBlockIcons(IIconRegister aIconRegister) {
        for (int metadata = 0; metadata < hint.length; metadata++) {
            hint[metadata] = aIconRegister.registerIcon(ImpactStructures.MOD_ID + ":" + (metadata + 1));
        }
    }

    @Override
    public IIcon getIcon(int aSide, int aMeta) {
        return hint[aMeta & 15];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess aWorld, int xCoord, int yCoord, int zCoord, int aSide) {
        int tMeta = aWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        return getIcon(aSide, tMeta);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (int i = 0; i <= 15; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}
