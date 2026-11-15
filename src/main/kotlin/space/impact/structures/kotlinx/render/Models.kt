package space.impact.structures.kotlinx.render

import space.impact.structures.animation.Animations
import space.impact.structures.animation.IAnimation
import space.impact.structures.render.BlockModel
import space.impact.structures.render.RenderPart
import space.impact.structures.render.StructureRenderBinding

public fun blockModel(configure: BlockModelBuilder.() -> Unit): BlockModel {
    val builder = BlockModelBuilder()
    configure.invoke(builder)
    return builder.build()
}

public fun BlockModel.animated(animation: IAnimation = Animations.STILL): RenderPart {
    return RenderPart(
        this,
        animation,
    )
}

public fun StructureRenderBinding.animated(
    animation: IAnimation = Animations.STILL,
    hideOriginals: Boolean = true,
): RenderPart {
    return RenderPart(
        this,
        animation,
        hideOriginals,
    )
}
