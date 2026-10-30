package space.impact.api.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraft.world.WorldProvider
import net.minecraft.world.WorldSettings
import net.minecraft.world.chunk.IChunkProvider
import sun.misc.Unsafe

/** Avoids starting Forge or constructing chunks; only explicit block lookups are exercised. */
internal class StructureTestWorld private constructor() : World(
    null,
    "kotlinx-structure-test",
    null as WorldSettings?,
    null as WorldProvider?,
    null,
) {

    lateinit var testedBlock: Block
    var testedMetadata: Int = 0

    override fun getBlock(
        x: Int,
        y: Int,
        z: Int,
    ): Block {
        return testedBlock
    }

    override fun getBlockMetadata(
        x: Int,
        y: Int,
        z: Int,
    ): Int {
        return testedMetadata
    }

    override fun createChunkProvider(): IChunkProvider? {
        return null
    }

    override fun func_152379_p(): Int {
        return 0
    }

    override fun getEntityByID(id: Int): Entity? {
        return null
    }

    companion object {

        fun create(block: Block): StructureTestWorld {
            val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
            unsafeField.isAccessible = true
            val unsafe = unsafeField.get(null) as Unsafe
            val world = unsafe.allocateInstance(StructureTestWorld::class.java) as StructureTestWorld
            world.testedBlock = block
            return world
        }
    }
}
