package space.impact.structures.kotlinx.render

import net.minecraft.block.Block
import space.impact.structures.render.BlockAppearance
import space.impact.structures.render.BlockModel

public class BlockModelBuilder {

    private val builder: BlockModel.Builder = BlockModel.builder()

    public fun block(
        x: Int,
        y: Int,
        z: Int,
        block: Block,
        metadata: Int = 0,
    ) {
        builder.block(
            x,
            y,
            z,
            block,
            metadata,
        )
    }

    public fun block(
        x: Int,
        y: Int,
        z: Int,
        appearance: BlockAppearance,
    ) {
        builder.block(
            x,
            y,
            z,
            appearance,
        )
    }

    public fun build(): BlockModel {
        return builder.build()
    }
}
