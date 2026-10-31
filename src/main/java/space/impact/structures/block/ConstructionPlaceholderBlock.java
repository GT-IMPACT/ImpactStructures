package space.impact.structures.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import space.impact.structures.Main;

/** A neutral creative-build filler; it is not part of the sixteen-color hint palette. */
public class ConstructionPlaceholderBlock extends Block {

    public ConstructionPlaceholderBlock() {
        super(Material.glass);
        setBlockName("impactapi.construction_placeholder");
        setBlockTextureName("minecraft:glass");
        setHardness(0.3F);
        setCreativeTab(Main.creativeTab);
    }

    @Override
    public boolean isOpaqueCube() { return false; }

    @Override
    public int getRenderBlockPass() { return 1; }

    @Override
    public boolean renderAsNormalBlock() { return false; }
}
