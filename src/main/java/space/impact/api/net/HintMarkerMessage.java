package space.impact.api.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import space.impact.api.ImpactStructures;
import space.impact.api.Main;
import space.impact.api.hint.HintMarkerKind;

/** A service frame is sent separately from the sixteen-color block palette. */
public class HintMarkerMessage implements IMessage {
    private int dimension;
    private int x, y, z;
    private HintMarkerKind kind;

    public HintMarkerMessage() {}

    public HintMarkerMessage(int dimension, int x, int y, int z, HintMarkerKind kind) {
        if (kind == null) throw new IllegalArgumentException("Marker kind is required");
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.kind = kind;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int ordinal = buf.readUnsignedByte();
        kind = ordinal < HintMarkerKind.values().length ? HintMarkerKind.values()[ordinal] : null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(kind == null ? 255 : kind.ordinal());
    }

    public static class Handler implements IMessageHandler<HintMarkerMessage, IMessage> {
        @Override
        public IMessage onMessage(HintMarkerMessage msg, MessageContext ctx) {
            if (!ctx.side.isClient() || Main.instance() == null || msg.kind == null) return null;
            Main.instance().proxy().enqueueClientTask(() -> {
                EntityPlayer player = Main.getCurrentPlayer();
                if (player == null || player.worldObj == null || player.worldObj.provider.dimensionId != msg.dimension
                        || msg.y < 0 || msg.y >= 256 || Math.abs((long) msg.x) > 30000000
                        || Math.abs((long) msg.z) > 30000000) return;
                ImpactStructures.hintMarker(player.worldObj, msg.x, msg.y, msg.z, msg.kind);
            });
            return null;
        }
    }
}
