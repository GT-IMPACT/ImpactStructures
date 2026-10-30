package space.impact.api.structure;

import net.minecraft.block.Block;

public interface ISpecialItemBlock {

    int getItemMetaFromBlockMeta(Block block, int blockMeta);
}
