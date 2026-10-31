package space.impact.structures.kotlinx.channels

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.structures.structure.IChannelDefinition

internal class ChannelExtensionsTest {

    @Test
    internal fun absentChannelsAreNullableButMasterFallbackIsAvailable() {
        val projector: ItemStack = ItemStack(
            Item(),
            4,
            0,
        )
        assertNull(projector.channelValueOrNull("coil tier"))
        assertEquals(
            4,
            projector.channelValue("coil tier"),
        )
        projector.setChannelValue(
            name = "coil tier",
            value = 7,
        )
        val snapshot: Map<String, Int> = projector.channels
        projector.setChannelValue(
            name = "coil tier",
            value = 9,
        )
        assertEquals(
            7,
            snapshot["coil tier"],
        )
        assertEquals(
            9,
            projector.channelValueOrNull("coil tier"),
        )
        projector.removeChannel("coil tier")
        assertNull(projector.channelValueOrNull("coil tier"))
    }

    @Test
    internal fun channelsSurviveCompressedNbtAndClearKeepsUnrelatedTags() {
        val projector: ItemStack = ItemStack(Item())
        val tag: NBTTagCompound = NBTTagCompound()
        tag.setString(
            "owner",
            "retained",
        )
        projector.tagCompound = tag
        projector.setChannelValue(
            name = "glass",
            value = 3,
        )
        val bytes: ByteArrayOutputStream = ByteArrayOutputStream()
        CompressedStreamTools.writeCompressed(
            projector.tagCompound,
            bytes,
        )
        val restored: ItemStack = ItemStack(Item())
        restored.tagCompound = CompressedStreamTools.readCompressed(ByteArrayInputStream(bytes.toByteArray()))

        assertEquals(
            3,
            restored.channelValue("glass"),
        )
        restored.clearChannels()
        assertTrue(restored.channels.isEmpty())
        assertEquals(
            "retained",
            restored.tagCompound.getString("owner"),
        )
    }

    @Test
    internal fun boundedDefinitionsValidateAndNormalizeInclusiveRanges() {
        val channel: IChannelDefinition = channelDefinition(
            name = "coil tier",
            values = 2..5,
        )
        assertTrue(channel.isValueValid(2))
        assertTrue(channel.isValueValid(5))
        assertFalse(channel.isValueValid(6))
        assertEquals(
            2,
            channel.normalizeValue(-10),
        )
        assertEquals(
            5,
            channel.normalizeValue(10),
        )
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            channelDefinition(
                name = "coil tier",
                values = 5..2,
            )
        }
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            channelDefinition("UPPERCASE")
        }
    }
}
