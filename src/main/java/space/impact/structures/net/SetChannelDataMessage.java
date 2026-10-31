package space.impact.structures.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Locale;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

import space.impact.structures.Main;
import space.impact.structures.alignment.constructable.ChannelDataAccessor;
import space.impact.structures.item.ItemConstructableTrigger;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SetChannelDataMessage implements IMessage {

    private final List<Map.Entry<String, Integer>> data = new ArrayList<>();
    private boolean onCursor;

    public SetChannelDataMessage() {}

    public SetChannelDataMessage(ItemStack trigger) {
        this(trigger, false);
    }

    public SetChannelDataMessage(ItemStack trigger, final boolean onCursor) {
        this.onCursor = onCursor;
        ChannelDataAccessor.iterateChannelData(trigger).forEach(data::add);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        onCursor = buf.readBoolean();
        data.clear();
        int size = ByteBufUtils.readVarShort(buf);
        if (size > 256) throw new IllegalArgumentException("Too many projector channels");
        for (int i = 0; i < size; i++) {
            String channel = ByteBufUtils.readUTF8String(buf);
            int value = ByteBufUtils.readVarInt(buf, 5);
            if (!isValidChannel(channel, value)) throw new IllegalArgumentException("Invalid projector channel");
            data.add(Pair.of(channel, value));
        }
    }

    static boolean isValidChannel(String channel, int value) {
        return channel != null && !channel.trim().isEmpty() && channel.length() <= 128
                && channel.equals(channel.toLowerCase(Locale.ROOT)) && value > 0;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(onCursor);
        ByteBufUtils.writeVarShort(buf, data.size());
        for (Entry<String, Integer> e : data) {
            ByteBufUtils.writeUTF8String(buf, e.getKey());
            ByteBufUtils.writeVarInt(buf, e.getValue(), 5);
        }
    }

    public static class Handler implements IMessageHandler<SetChannelDataMessage, IMessage> {

        @Override
        public IMessage onMessage(SetChannelDataMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ServerTasks.enqueue(() -> apply(message, player));
            return null;
        }

        private void apply(SetChannelDataMessage message, EntityPlayerMP player) {
            ItemStack target = message.onCursor ? player.inventory.getItemStack() : player.getHeldItem();
            if (target != null && target.getItem() instanceof ItemConstructableTrigger) {
                ChannelDataAccessor.wipeChannelData(target);
                for (Entry<String, Integer> e : message.data) {
                    ChannelDataAccessor.setChannelData(target, e.getKey(), e.getValue());
                }
                // since this is a set all channel request from the client, we would assume client already know
                // what this would look like on the client, so no sync
            } else {
                Main.LOGGER.warn(
                        "{} trying to set channel data on {}, which is not a hologram projector!",
                        player.getUniqueID(),
                        target);
            }
        }
    }
}
