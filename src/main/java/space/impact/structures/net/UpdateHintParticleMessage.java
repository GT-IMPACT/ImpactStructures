package space.impact.structures.net;

import static space.impact.structures.Main.LOGGER;

import space.impact.structures.Main;
import space.impact.structures.ImpactStructures;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class UpdateHintParticleMessage implements IMessage {

    private int dimension;
    private int x;
    private short y;
    private int z;
    private short r;
    private short g;
    private short b;
    private short a;

    public UpdateHintParticleMessage() {}

    public UpdateHintParticleMessage(int dimension, int x, short y, int z, short r, short g, short b, short a) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readShort();
        z = buf.readInt();
        r = buf.readUnsignedByte();
        g = buf.readUnsignedByte();
        b = buf.readUnsignedByte();
        a = buf.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeShort(y);
        buf.writeInt(z);
        buf.writeByte(r);
        buf.writeByte(g);
        buf.writeByte(b);
        buf.writeByte(a);
    }

    public static class Handler implements IMessageHandler<UpdateHintParticleMessage, IMessage> {

        @Override
        public IMessage onMessage(UpdateHintParticleMessage msg, MessageContext ctx) {
            if (!ctx.side.isClient() || Main.instance() == null) return null;
            Main.instance().proxy().enqueueClientTask(() -> {
                EntityPlayer player = Main.getCurrentPlayer();
                if (player == null || player.worldObj == null || player.worldObj.provider.dimensionId != msg.dimension
                        || msg.y < 0 || msg.y >= 256 || Math.abs((long) msg.x) > 30000000
                        || Math.abs((long) msg.z) > 30000000) return;
                boolean updated = ImpactStructures.updateHintParticleTint(player, player.worldObj,
                        msg.x, msg.y, msg.z, new short[] { msg.r, msg.g, msg.b, msg.a });
                if (ImpactStructures.isDebugEnabled()) LOGGER.debug("Updated hint at ({}, {}, {}): {}", msg.x, msg.y, msg.z, updated);
            });
            return null;
        }
    }
}
