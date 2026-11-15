package space.impact.structures.kotlinx.execution

import java.lang.reflect.Field
import net.minecraft.entity.Entity
import net.minecraft.profiler.Profiler
import net.minecraft.world.World
import net.minecraft.world.WorldProvider
import net.minecraft.world.WorldSettings
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.storage.ISaveHandler
import sun.misc.Unsafe

/** Opaque identity for delegation tests: its constructor, world setup and chunk access never run. */
internal class WorldToken private constructor() : World(
    null as ISaveHandler?,
    "kotlinx-delegation-token",
    null as WorldSettings?,
    null as WorldProvider?,
    null as Profiler?,
) {

    override fun createChunkProvider(): IChunkProvider {
        throw AssertionError("Delegation tests must not access chunks")
    }

    override fun func_152379_p(): Int {
        throw AssertionError("Delegation tests must not query world state")
    }

    override fun getEntityByID(entityId: Int): Entity? {
        throw AssertionError("Delegation tests must not query entities")
    }

    internal companion object {

        internal fun allocate(): World {
            val accessField: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
            accessField.isAccessible = true
            val allocator: Unsafe = accessField.get(null) as Unsafe
            return allocator.allocateInstance(WorldToken::class.java) as World
        }
    }
}
