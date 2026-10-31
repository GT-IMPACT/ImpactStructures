package space.impact.api.net;

import static space.impact.api.Main.LOGGER;

import space.impact.api.Main;
import space.impact.api.ImpactStructures;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class ErrorHintParticleMessage implements IMessage {

    private int dimension;
    private int x;
    private short y;
    private int z;

    public ErrorHintParticleMessage() {}

    public ErrorHintParticleMessage(int dimension, int x, short y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readShort();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeShort(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<ErrorHintParticleMessage, IMessage> {

        @Override
        public IMessage onMessage(ErrorHintParticleMessage msg, MessageContext ctx) {
            if (!ctx.side.isClient() || Main.instance() == null) return null;
            Main.instance().proxy().enqueueClientTask(() -> {
                EntityPlayer player = Main.getCurrentPlayer();
                if (player == null || player.worldObj == null || player.worldObj.provider.dimensionId != msg.dimension
                        || msg.y < 0 || msg.y >= 256 || Math.abs((long) msg.x) > 30000000
                        || Math.abs((long) msg.z) > 30000000) return;
                boolean updated = ImpactStructures.markHintParticleError(player, player.worldObj, msg.x, msg.y, msg.z);
                if (ImpactStructures.isDebugEnabled()) LOGGER.debug("Marked hint error at ({}, {}, {}): {}", msg.x, msg.y, msg.z, updated);
            });
            return null;
        }
    }
}
