package space.impact.api.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.block.material.Material

/** Loaded by an isolated loader that deliberately refuses the kotlin-reflect implementation. */
internal object PropertyReferenceProbe {

    fun run(): Boolean {
        val world = StructureTestWorld.create(object : Block(Material.rock) {})
        world.testedMetadata = 6
        val controller = StructureController()
        val element = tieredCheckOnly(
            extractor = { _, metadata ->
                metadata
            },
            notSet = null,
            property = StructureController::tier,
        )
        val accepted = element.check(
            controller,
            world,
            0,
            0,
            0,
        )
        return accepted && controller.tier == 6
    }
}
