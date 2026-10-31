package space.impact.api;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import space.impact.api.net.ErrorHintParticleMessage;
import space.impact.api.net.UpdateHintParticleMessage;
import space.impact.api.net.HintMarkerMessage;
import space.impact.api.hint.HintColor;
import space.impact.api.hint.HintMarkerKind;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class CommonProxy {

    public CommonProxy() {
        super();
        FMLCommonHandler.instance().bus().register(new ForgeEventHandler());
    }

    public void hintParticleTinted(World w, int x, int y, int z, IIcon[] icons, short[] RGBa) {}

    public void hintParticleTinted(World w, int x, int y, int z, IIcon[] icons, short[] RGBa,
            ItemStack displayStack) {}

    public void hintParticleTinted(World w, int x, int y, int z, Block block, int meta, short[] RGBa) {}

    public void hintParticle(World w, int x, int y, int z, IIcon[] icons) {}

    public void hintParticle(World w, int x, int y, int z, IIcon[] icons, ItemStack displayStack) {}

    public void hintParticle(World w, int x, int y, int z, Block block, int meta) {}

    public void hintFrame(World w, int x, int y, int z, HintColor color) {}

    /** Client tasks are intentionally ignored by a dedicated server proxy. */
    public void enqueueClientTask(Runnable task) {}

    public void hintMarker(World w, int x, int y, int z, HintMarkerKind kind) {
        if (w == null || w.isRemote) return;
        Main.net.sendToAllAround(new HintMarkerMessage(w.provider.dimensionId, x, y, z, kind),
                new NetworkRegistry.TargetPoint(w.provider.dimensionId, x + 0.5, y + 0.5, z + 0.5, 64));
    }

    public boolean updateHintParticleTint(EntityPlayer player, World w, int x, int y, int z, short[] rgBa) {
        if (rgBa == null || rgBa.length != 4) throw new IllegalArgumentException("Four tint components required");
        if (w != null && player instanceof EntityPlayerMP && player.worldObj == w) {
            Main.net.sendTo(
                    new UpdateHintParticleMessage(w.provider.dimensionId, x, (short) y, z, rgBa[0], rgBa[1], rgBa[2], rgBa[3]),
                    (EntityPlayerMP) player);
            return true;
        } else {
            return false;
        }
    }

    public EntityPlayer getCurrentPlayer() {
        return null;
    }

    public boolean isCurrentPlayer(EntityPlayer player) {
        return false;
    }

    public void addClientSideChatMessages(String... messages) {}

    public void startHinting(World w) {}

    public void endHinting(World w) {}

    public void preInit(FMLPreInitializationEvent e) {}

    public void init(FMLInitializationEvent e) {}

    public long getOverworldTime() {
        return MinecraftServer.getServer().getEntityWorld().getTotalWorldTime();
    }

    public void uploadChannels(ItemStack trigger) {}

    public boolean markHintParticleError(EntityPlayer player, World w, int x, int y, int z) {
        if (w != null && player instanceof EntityPlayerMP && player.worldObj == w) {
            Main.net.sendTo(new ErrorHintParticleMessage(w.provider.dimensionId, x, (short) y, z), (EntityPlayerMP) player);
            return true;
        } else {
            return false;
        }
    }

    public void loadComplete(FMLLoadCompleteEvent e) {
        ConfigurationHandler.INSTANCE.loadRegistryOrder();
    }

    private final Map<EntityPlayerMP, Map<Object, Long>> throttleMap = new WeakHashMap<>();

    public void addThrottledChat(Object throttleKey, EntityPlayer player, IChatComponent text, short intervalRequired,
            boolean forceUpdateLastSend) {
        if (player instanceof EntityPlayerMP) {
            Map<Object, Long> submap = throttleMap.computeIfAbsent((EntityPlayerMP) player, p -> new HashMap<>());
            addThrottledChat(throttleKey, player, text, intervalRequired, forceUpdateLastSend, submap);
        }
    }

    protected static void addThrottledChat(Object throttleKey, EntityPlayer player, IChatComponent text,
            short intervalRequired, boolean forceUpdateLastSend, Map<Object, Long> submap) {
        long now = System.currentTimeMillis();
        Long old;
        if (forceUpdateLastSend) {
            old = submap.put(throttleKey, now);
        } else {
            old = submap.get(throttleKey);
        }
        if (old == null || now - old >= intervalRequired) {
            player.addChatComponentMessage(text);
            if (!forceUpdateLastSend) submap.put(throttleKey, now);
        }
    }

    public void displayConfigGUI(String category) {}

    public static class ForgeEventHandler {

        private int ticksSinceLastPurge;

        @SubscribeEvent
        public void onServerTickEnd(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            if (++ticksSinceLastPurge < 20 * 60 * 10) return;
            ticksSinceLastPurge = 0;
            MinecraftServer server = MinecraftServer.getServer();
            // it's hard to tell why server could be null in a server tick handler, but who knows?
            if (server == null) return;
            SortedRegistry.cleanup(server);
        }
    }
}
