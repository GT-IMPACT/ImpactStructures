package space.impact.api.net;

import static org.junit.jupiter.api.Assertions.*;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

class PacketValidationTest {
    @Test
    void channelPayloadRoundTripsWithoutLosingNamesOrValues() {
        ByteBuf input = Unpooled.buffer();
        input.writeBoolean(true);
        ByteBufUtils.writeVarShort(input, 2);
        ByteBufUtils.writeUTF8String(input, "casing tier");
        ByteBufUtils.writeVarInt(input, Integer.MAX_VALUE, 5);
        ByteBufUtils.writeUTF8String(input, "show_error");
        ByteBufUtils.writeVarInt(input, 1, 5);
        byte[] expected = new byte[input.readableBytes()];
        input.getBytes(0, expected);
        SetChannelDataMessage decoded = new SetChannelDataMessage();
        decoded.fromBytes(input);
        ByteBuf output = Unpooled.buffer();
        decoded.toBytes(output);
        byte[] actual = new byte[output.readableBytes()];
        output.readBytes(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    void rejectsOversizedChannelsAndInvalidNamesBeforeApplyingAnything() {
        ByteBuf oversized = Unpooled.buffer().writeBoolean(false);
        ByteBufUtils.writeVarShort(oversized, 257);
        assertThrows(IllegalArgumentException.class, () -> new SetChannelDataMessage().fromBytes(oversized));
        ByteBuf invalid = Unpooled.buffer().writeBoolean(false);
        ByteBufUtils.writeVarShort(invalid, 1);
        ByteBufUtils.writeUTF8String(invalid, "UPPERCASE");
        ByteBufUtils.writeVarInt(invalid, 1, 5);
        assertThrows(IllegalArgumentException.class, () -> new SetChannelDataMessage().fromBytes(invalid));
        ByteBuf blank = Unpooled.buffer().writeBoolean(false);
        ByteBufUtils.writeVarShort(blank, 1);
        ByteBufUtils.writeUTF8String(blank, "   ");
        ByteBufUtils.writeVarInt(blank, 1, 5);
        assertThrows(IllegalArgumentException.class, () -> new SetChannelDataMessage().fromBytes(blank));
    }

    @Test
    void rejectsUnboundedRegistryOrdering() {
        ByteBuf input = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(input, "inventories");
        ByteBufUtils.writeVarInt(input, 1025, 2);
        assertThrows(IllegalArgumentException.class, () -> new RegistryOrderSyncMessage().fromBytes(input));
    }

    @Test
    void rejectsOutOfRangeSavedOrientation() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("posy", 64);
        tag.setInteger("rotf", 96);
        ByteBuf input = Unpooled.buffer();
        ByteBufUtils.writeTag(input, tag);
        assertThrows(IllegalArgumentException.class, () -> new AlignmentMessage.AlignmentData().fromBytes(input));
    }
}
