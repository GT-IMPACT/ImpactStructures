package space.impact.structures;

import static space.impact.structures.Main.RANDOM;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import space.impact.structures.client.AngelicaCompat;
import space.impact.structures.client.HintFrameGeometry;
import space.impact.structures.client.HintFrameTexture;
import space.impact.structures.client.HintRenderStyle;
import space.impact.structures.client.MarkerRayTrace;
import space.impact.structures.entity.fx.WeightlessParticleFX;
import space.impact.structures.hint.HintColor;
import space.impact.structures.hint.HintMarkerKind;
import space.impact.structures.item.ItemConstructableTrigger;
import space.impact.structures.net.RegistryOrderSyncMessage;
import space.impact.structures.net.SetChannelDataMessage;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy {

    private static final short[] RGBA_NO_TINT = { 255, 255, 255, 255 };
    private static final Queue<Runnable> clientTasks = new ConcurrentLinkedQueue<>();
    private static final Map<HintParticleInfo, HintGroup> allHints = new HashMap<>();
    /**
     * All batches of hints.
     */
    private static final List<HintGroup> allGroups = new ArrayList<>();

    /**
     * Current batch of hints. Belongs to the same logical multiblock.
     */
    private static HintGroup currentHints;
    /**
     * Collection of all hints. Sorted from farthest (index 0) to nearest (index size - 1)
     * <p>
     * 10k should be more than enough, if not a bit wasteful
     */
    private static final List<HintParticleInfo> allHintsForRender = new ArrayList<>(10000);
    private static final List<HintParticleInfo> visibleMarkers = new ArrayList<>();
    private static final Set<IIcon> usedTextures = Collections.newSetFromMap(new IdentityHashMap<IIcon, Boolean>());
    private static boolean angelicaLoaded;
    private static long lightingRevision;
    /**
     * If the diff to current player position is too great sort the allHints. Initial value is very far off below y=0
     * but not huge enough to make a NaN.
     * <p>
     * We are using this supposedly immutable object as a mutable object here. So be aware.
     */
    private static final Vec3 lastPlayerPos = Vec3.createVectorHelper(0, -1e30, 0);
    /**
     * if true rebuild the allHints list from all hintOwners (and sort it)
     */
    private static boolean allHintsDirty = false;
    /**
     * A throttle map for local player. Integrated server will use the throttle map in super class instead.
     */
    private static final Map<Object, Long> localThrottleMap = new HashMap<>();

    private static void clearHints() {
        allHints.clear();
        allGroups.clear();
        allHintsForRender.clear();
        visibleMarkers.clear();
        usedTextures.clear();
        currentHints = null;
        allHintsDirty = false;
        lastPlayerPos.yCoord = -1e30;
        localThrottleMap.clear();
    }

    private static void endOpenHintBatch() {
        Main.instance().proxy().endHinting(Minecraft.getMinecraft().theWorld);
    }

    @Override
    public void enqueueClientTask(Runnable task) {
        if (task == null) throw new IllegalArgumentException("Client task must not be null");
        clientTasks.add(task);
    }

    @Override
    public void hintMarker(World w, int x, int y, int z, HintMarkerKind kind) {
        if (w == null) return;
        if (!w.isRemote) {
            super.hintMarker(w, x, y, z, kind);
            return;
        }
        if (w != Minecraft.getMinecraft().theWorld) return;
        HintParticleInfo existing = getHintParticleInfo(w, x, y, z);
        if (existing != null) {
            existing.markerKind = kind;
            return;
        }
        hintParticleTinted(w, x, y, z, new IIcon[6], RGBA_NO_TINT);
        HintParticleInfo created = getHintParticleInfo(w, x, y, z);
        if (created != null) {
            created.markerKind = kind;
        }
    }

    @Override
    public void hintFrame(World w, int x, int y, int z, HintColor color) {
        if (w == null || !w.isRemote || w != Minecraft.getMinecraft().theWorld) return;
        HintParticleInfo hint = getHintParticleInfo(w, x, y, z);
        if (hint == null) {
            hintParticleTinted(w, x, y, z, new IIcon[6], RGBA_NO_TINT);
            hint = getHintParticleInfo(w, x, y, z);
        }
        if (hint != null) hint.frameColor = color;
    }

    @Override
    public void hintParticleTinted(World w, int x, int y, int z, IIcon[] icons, short[] RGBa) {
        hintParticleTinted(w, x, y, z, icons, RGBa, null);
    }

    @Override
    public void hintParticleTinted(World w, int x, int y, int z, IIcon[] icons, short[] RGBa,
            ItemStack displayStack) {
        addHint(w, x, y, z, icons, RGBa, null, 0, displayStack);
    }

    private void addHint(World w, int x, int y, int z, IIcon[] icons, short[] RGBa, Block block, int metadata,
            ItemStack displayStack) {
        if (w == null || !w.isRemote || w != Minecraft.getMinecraft().theWorld) return;
        if (icons == null || icons.length != 6 || RGBa == null || RGBa.length != 4)
            throw new IllegalArgumentException("A hint needs six face icons and four tint components");
        ensureHinting();
        HintParticleInfo info = new HintParticleInfo(w, x, y, z, icons, RGBa, block, metadata, displayStack);
        HintParticleInfo samePosition = getHintParticleInfo(w, x, y, z);
        if (samePosition != null && allHints.get(samePosition) == currentHints) {
            info.markerKind = samePosition.markerKind;
            info.frameColor = samePosition.frameColor;
        }

        // check and remove colliding holograms
        if (ConfigurationHandler.INSTANCE.isRemoveCollidingHologram()) {
            HintGroup dupe = allHints.get(info);
            if (dupe != null && dupe != currentHints) {
                allGroups.remove(dupe);
                removeGroup(dupe);
            }
        }
        allHints.put(info, currentHints);
        currentHints.hints.put(info, info);
        allHintsDirty = true;

        if (!info.textured) return;
        EntityFX particle = new WeightlessParticleFX(
                w,
                x + RANDOM.nextFloat() * 0.5F,
                y + RANDOM.nextFloat() * 0.5F,
                z + RANDOM.nextFloat() * 0.5F,
                0,
                0,
                0);
        particle.setRBGColorF(0, 0.6F * RANDOM.nextFloat(), 0.8f);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    @Override
    public void hintParticleTinted(World w, int x, int y, int z, Block block, int meta, short[] RGBa) {
        if (w == null || !w.isRemote || w != Minecraft.getMinecraft().theWorld) return;
        addHint(w, x, y, z, createIIconFromBlock(block, meta), RGBa, block, meta, null);
    }

    @Override
    public void hintParticle(World w, int x, int y, int z, IIcon[] icons) {
        hintParticleTinted(w, x, y, z, icons, RGBA_NO_TINT);
    }

    @Override
    public void hintParticle(World w, int x, int y, int z, IIcon[] icons, ItemStack displayStack) {
        hintParticleTinted(w, x, y, z, icons, RGBA_NO_TINT, displayStack);
    }

    @Override
    public void hintParticle(World w, int x, int y, int z, Block block, int meta) {
        hintParticleTinted(w, x, y, z, block, meta, RGBA_NO_TINT);
    }

    @Override
    public boolean updateHintParticleTint(EntityPlayer player, World w, int x, int y, int z, short[] rgBa) {
        if (player instanceof EntityPlayerMP) return super.updateHintParticleTint(player, w, x, y, z, rgBa);
        if (player != getCurrentPlayer()) return false; // how?
        HintParticleInfo hint = getHintParticleInfo(w, x, y, z);
        if (hint == null) {
            return false;
        }
        hint.setTint(rgBa);
        return true;
    }

    private static HintParticleInfo getHintParticleInfo(World w, int x, int y, int z) {
        HintParticleInfo info = new HintParticleInfo(w, x, y, z, null, null);
        HintGroup existing = allHints.get(info);
        return existing == null ? null : existing.hints.get(info);
    }

    @Override
    public boolean markHintParticleError(EntityPlayer player, World w, int x, int y, int z) {
        if (player instanceof EntityPlayerMP) return super.markHintParticleError(player, w, x, y, z);
        if (player == null || player != getCurrentPlayer() || w == null || !w.isRemote
                || w != Minecraft.getMinecraft().theWorld) return false;
        // Error packets can arrive after endHinting; keep added markers in the existing hologram.
        boolean reuseGroup = currentHints == null && !allGroups.isEmpty();
        if (reuseGroup) currentHints = allGroups.get(allGroups.size() - 1);
        try {
            hintMarker(w, x, y, z, HintMarkerKind.ERROR);
            return true;
        } finally {
            if (reuseGroup) currentHints = null;
        }
    }

    @Override
    public void addThrottledChat(Object throttleKey, EntityPlayer player, IChatComponent text, short intervalRequired,
            boolean forceUpdateLastSend) {
        if (player instanceof EntityPlayerMP)
            super.addThrottledChat(throttleKey, player, text, intervalRequired, forceUpdateLastSend);
        else if (player != null)
            addThrottledChat(throttleKey, player, text, intervalRequired, forceUpdateLastSend, localThrottleMap);
    }

    static void removeGroup(HintGroup group) {
        for (HintParticleInfo hintParticleInfo : group.getHints()) {
            allHints.remove(hintParticleInfo, group);
        }
        allHintsDirty = true;
    }

    private static IIcon[] createIIconFromBlock(Block block, int meta) {
        IIcon[] ret = new IIcon[6];
        for (int i = 0; i < 6; i++) {
            ret[i] = block.getIcon(i, meta);
        }
        return ret;
    }

    @Override
    public void addClientSideChatMessages(String... messages) {
        GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        for (String s : messages) {
            chat.printChatMessage(new ChatComponentText(s));
        }
    }

    @Override
    public EntityPlayer getCurrentPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public boolean isCurrentPlayer(EntityPlayer player) {
        return player == Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public void startHinting(World w) {
        if (!w.isRemote) return;
        if (currentHints != null) endHinting(w);
        currentHints = new HintGroup();
    }

    private void ensureHinting() {
        if (currentHints == null) currentHints = new HintGroup();
    }

    @Override
    public void endHinting(World w) {
        if (!w.isRemote || currentHints == null) return;
        while (!allGroups.isEmpty() && allGroups.size() >= ConfigurationHandler.INSTANCE.getMaxCoexistingHologram()) {
            removeGroup(allGroups.remove(0));
        }
        if (!currentHints.getHints().isEmpty()) allGroups.add(currentHints);
        // we use the player existence time here as some worlds don't really advance the time ticker
        currentHints.setCreationTime(getCurrentPlayer() == null ? 0 : getCurrentPlayer().ticksExisted);
        currentHints = null;
    }

    @Override
    public long getOverworldTime() {
        // there is no overworld, better just hope current world time is ok...
        return Minecraft.getMinecraft().theWorld.getTotalWorldTime();
    }

    @Override
    public void uploadChannels(ItemStack trigger) {
        Main.net.sendToServer(new SetChannelDataMessage(trigger));
    }

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        angelicaLoaded = Loader.isModLoaded("angelica");
        FMLCommonHandler.instance().bus().register(new FMLEventHandler());
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
    }

    static void markTextureUsed(IIcon icon) {
        if (!usedTextures.add(icon)) return;
        if (angelicaLoaded) AngelicaCompat.markTextureUsed(icon);
        if (Main.COMPAT instanceof IStructureCompat)
            ((IStructureCompat) Main.COMPAT).markTextureUsed(icon);
    }

    static void sendRegistryOrderToServer() {
        if (FMLEventHandler.configSent) return;
        FMLEventHandler.configSent = true;
        for (String s : SortedRegistry.ALL_REGISTRIES.keySet()) {
            Pair<List<String>, List<String>> registryOrder = ConfigurationHandler.INSTANCE.getRegistryOrder(s);
            if (registryOrder == null || registryOrder.getKey().isEmpty() && registryOrder.getValue().isEmpty())
                continue;
            Main.net
                    .sendToServer(new RegistryOrderSyncMessage(s, registryOrder.getKey(), registryOrder.getValue()));
        }
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent evt) {
        super.loadComplete(evt);
        for (Map.Entry<String, WeakReference<SortedRegistry<?>>> e : SortedRegistry.ALL_REGISTRIES.entrySet()) {
            SortedRegistry<?> r = e.getValue().get();
            if (r == null) continue;
            Pair<List<String>, List<String>> p = ConfigurationHandler.INSTANCE.getRegistryOrder(e.getKey());
            r.registerOrdering(getSPPlayerUUID(), p.getKey(), p.getValue());
        }
    }

    public UUID getSPPlayerUUID() {
        return EntityPlayer.func_146094_a(Minecraft.getMinecraft().getSession().func_148256_e());
    }

    public void displayConfigGUI(String category) {
        ConfigElement<Object> element = new ConfigElement<>(
                ConfigurationHandler.INSTANCE.getConfig().getCategory(category));
        GuiConfig guiConfig = new GuiConfig(
                null,
                element.getChildElements(),
                ImpactStructures.MOD_ID,
                null,
                false,
                false,
                I18n.format(element.getLanguageKey()));
        Minecraft.getMinecraft().displayGuiScreen(guiConfig);
    }

    private static class HintGroup {

        // Position lookup must stay O(1) when thousands of error/color updates arrive together.
        private final Map<HintParticleInfo, HintParticleInfo> hints = new LinkedHashMap<>();
        private int creationTime = -1;

        public Collection<HintParticleInfo> getHints() {
            return hints.values();
        }

        public void setCreationTime(int creationTime) {
            this.creationTime = creationTime;
        }

        public int getCreationTime() {
            return creationTime;
        }
    }

    private static class HintParticleInfo {

        private final World w;
        // these are the block coordinate for e.g. w.getBlock()
        private final int x, y, z;
        private final IIcon[] icons;
        private final boolean textured;
        private final Block block;
        private final int metadata;
        private ItemStack displayStack;
        private short[] tint;
        private HintMarkerKind markerKind;
        private HintColor frameColor;
        private long lastLightingRevision = -1;
        private int brightness;
        private double sortDistance;

        private final long creationTime = System.currentTimeMillis(); // use tick time instead maybe

        public HintParticleInfo(World w, int x, int y, int z, IIcon[] icons, short[] tint) {
            this(w, x, y, z, icons, tint, null, 0);
        }

        public HintParticleInfo(World w, int x, int y, int z, IIcon[] icons, short[] tint, Block block, int metadata) {
            this(w, x, y, z, icons, tint, block, metadata, null);
        }

        public HintParticleInfo(World w, int x, int y, int z, IIcon[] icons, short[] tint, Block block, int metadata,
                ItemStack displayStack) {
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
            this.icons = icons == null ? null : icons.clone();
            this.tint = tint == null ? null : tint.clone();
            this.block = block;
            this.metadata = metadata;
            this.displayStack = displayStack == null ? null : displayStack.copy();
            boolean hasTexture = false;
            if (icons != null) {
                for (IIcon icon : icons) if (icon != null) { hasTexture = true; break; }
            }
            this.textured = hasTexture;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HintParticleInfo)) return false;
            HintParticleInfo that = (HintParticleInfo) o;
            return w == that.w && x == that.x && y == that.y && z == that.z;
        }

        public void setTint(short[] tint) {
            if (tint == null || tint.length != 4) throw new IllegalArgumentException("Four tint components required");
            this.tint = tint.clone();
        }

        public boolean rendersThroughBlocks() {
            return HintRenderStyle.rendersThroughBlocks(markerKind);
        }

        public String getDisplayName() {
            if (markerKind != null && (markerKind != HintMarkerKind.ERROR || (!textured && displayStack == null))) {
                return I18n.format(markerKind.getTranslationKey());
            }
            if (displayStack == null && block != null) {
                Item item = Item.getItemFromBlock(block);
                if (item != null) displayStack = new ItemStack(item, 1, metadata);
            }
            String name;
            if (displayStack != null) {
                name = displayStack.getDisplayName();
            } else if (block != null) {
                name = block.getLocalizedName();
            } else {
                name = I18n.format("impactapi.hint.unnamed");
            }
            return markerKind == HintMarkerKind.ERROR ? I18n.format("impactapi.hint.error", name) : name;
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(w);
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }

        public boolean isInFrustrum(Frustrum frustrum) {
            double size = HintRenderStyle.displaySize(markerKind, frameColor != null);
            double inset = (HintRenderStyle.BLOCK_SIZE - size) / 2;
            double margin = HintRenderStyle.FRAME_MARGIN;
            return frustrum.isBoxInFrustum(x + inset - margin, y + inset - margin, z + inset - margin,
                    x + inset + size + margin, y + inset + size + margin, z + inset + size + margin);
        }

        public void draw(Tessellator tes, double eyeX, double eyeY, double eyeZ, int eyeXint, int eyeYint,
                int eyeZint, boolean markerFill) {
            HintMarkerKind geometryKind = markerFill ? markerKind : null;
            double size = HintRenderStyle.cubeSize(geometryKind);
            double inset = HintRenderStyle.cubeInset(geometryKind);

            tes.setBrightness(markerFill ? 0x00F000F0 : getBrightness());

            int opacity = ConfigurationHandler.INSTANCE.getHintTransparency();
            if (markerFill) {
                int color = HintRenderStyle.markerColor(markerKind);
                tes.setColorRGBA(color >> 16 & 255, color >> 8 & 255, color & 255,
                        HintRenderStyle.markerOpacity(opacity));
            } else {
                tes.setColorRGBA((int) (tint[0] * .9F), (int) (tint[1] * .95F), tint[2], opacity);
            }

            double X = x - eyeXint + inset;
            double Y = y - eyeYint + inset;
            double Z = z - eyeZint + inset;
            double worldX = x + inset;
            double worldY = y + inset;
            double worldZ = z + inset;

            // cube is a very special model. its facings can be rendered correctly by viewer distance without using
            // surface normals and view vector
            // here we do a 2 pass render.
            // first pass we draw obstructed faces (i.e. faces that are further away from player)
            // second pass we draw unobstructed faces
            for (int j = 0; j < 2; j++) {
                boolean unobstructedPass = j == 1;
                for (int i = 0; i < 6; i++) {
                    if (!markerFill && icons[i] == null) continue;
                    IIcon icon = markerFill ? HintFrameTexture.INSTANCE : icons[i];
                    if (j == 0) markTextureUsed(icon);
                    // White atlas texels keep marker fills in the same batch as textured hints.
                    double u = icon.getMinU();
                    double U = icon.getMaxU();
                    double v = icon.getMinV();
                    double V = icon.getMaxV();
                    switch (i) { // {DOWN, UP, NORTH, SOUTH, WEST, EAST}
                        case 0:
                            // all these ifs is in form if ((is face unobstructed) != (is in unobstructred pass))
                            if ((worldY >= eyeY) != unobstructedPass) continue;
                            tes.setNormal(0, -1, 0);
                            tes.addVertexWithUV(X, Y, Z + size, u, V);
                            tes.addVertexWithUV(X, Y, Z, u, v);
                            tes.addVertexWithUV(X + size, Y, Z, U, v);
                            tes.addVertexWithUV(X + size, Y, Z, U, v);
                            tes.addVertexWithUV(X + size, Y, Z + size, U, V);
                            tes.addVertexWithUV(X, Y, Z + size, u, V);
                            break;
                        case 1:
                            if ((worldY + size <= eyeY) != unobstructedPass) continue;
                            tes.setNormal(0, 1, 0);
                            tes.addVertexWithUV(X, Y + size, Z, u, v);
                            tes.addVertexWithUV(X, Y + size, Z + size, u, V);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z, U, v);
                            tes.addVertexWithUV(X, Y + size, Z, u, v);
                            break;
                        case 2:
                            if ((worldZ >= eyeZ) != unobstructedPass) continue;
                            tes.setNormal(0, 0, -1);
                            tes.addVertexWithUV(X, Y, Z, U, V);
                            tes.addVertexWithUV(X, Y + size, Z, U, v);
                            tes.addVertexWithUV(X + size, Y + size, Z, u, v);
                            tes.addVertexWithUV(X + size, Y + size, Z, u, v);
                            tes.addVertexWithUV(X + size, Y, Z, u, V);
                            tes.addVertexWithUV(X, Y, Z, U, V);
                            break;
                        case 3:
                            if ((worldZ + size <= eyeZ) != unobstructedPass) continue;
                            tes.setNormal(0, 0, 1);
                            tes.addVertexWithUV(X + size, Y, Z + size, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, U, v);
                            tes.addVertexWithUV(X, Y + size, Z + size, u, v);
                            tes.addVertexWithUV(X, Y + size, Z + size, u, v);
                            tes.addVertexWithUV(X, Y, Z + size, u, V);
                            tes.addVertexWithUV(X + size, Y, Z + size, U, V);
                            break;
                        case 4:
                            if ((worldX >= eyeX) != unobstructedPass) continue;
                            tes.setNormal(-1, 0, 0);
                            tes.addVertexWithUV(X, Y, Z + size, U, V);
                            tes.addVertexWithUV(X, Y + size, Z + size, U, v);
                            tes.addVertexWithUV(X, Y + size, Z, u, v);
                            tes.addVertexWithUV(X, Y + size, Z, u, v);
                            tes.addVertexWithUV(X, Y, Z, u, V);
                            tes.addVertexWithUV(X, Y, Z + size, U, V);
                            break;
                        case 5:
                            if ((worldX + size <= eyeX) != unobstructedPass) continue;
                            tes.setNormal(1, 0, 0);
                            tes.addVertexWithUV(X + size, Y, Z, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z, U, v);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, u, v);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, u, v);
                            tes.addVertexWithUV(X + size, Y, Z + size, u, V);
                            tes.addVertexWithUV(X + size, Y, Z, U, V);
                            break;
                    }
                }
            }
        }

        public long getCreationTime() {
            return creationTime;
        }

        private int getBrightness() {
            if (lastLightingRevision != lightingRevision) {
                brightness = w.blockExists(x, 0, z) ? w.getLightBrightnessForSkyBlocks(x, y, z, 0) : 0;
                lastLightingRevision = lightingRevision;
            }
            return brightness;
        }

        public int drawColorFrame(Tessellator tes, double eyeX, double eyeY, double eyeZ,
                int cameraX, int cameraY, int cameraZ) {
            IIcon icon = HintFrameTexture.INSTANCE;
            markTextureUsed(icon);
            double u = (icon.getMinU() + icon.getMaxU()) / 2;
            double v = (icon.getMinV() + icon.getMaxV()) / 2;
            int color = frameColor.getRGB();
            tes.setBrightness(getBrightness());
            tes.setColorRGBA(color >> 16 & 255, color >> 8 & 255, color & 255,
                    ConfigurationHandler.INSTANCE.getHintTransparency());
            double localX = x - cameraX, localY = y - cameraY, localZ = z - cameraZ;
            int vertices = 0;
            for (int face = 0; face < 6; face++) {
                if (!HintFrameGeometry.isFaceVisible(face, eyeX - x, eyeY - y, eyeZ - z)) continue;
                for (int strip = 0; strip < 4; strip++) {
                    for (int vertex = 0; vertex < 6; vertex++) {
                        tes.addVertexWithUV(localX + HintFrameGeometry.coordinate(face, strip, vertex, 0),
                                localY + HintFrameGeometry.coordinate(face, strip, vertex, 1),
                                localZ + HintFrameGeometry.coordinate(face, strip, vertex, 2), u, v);
                        vertices++;
                    }
                }
            }
            return vertices;
        }

        public double getSquareDistanceTo(Vec3 point) {
            return point.squareDistanceTo(x + 0.5, y + 0.5, z + 0.5);
        }
    }

    public static class FMLEventHandler {

        static boolean connected = false;
        static boolean configSent = false;

        private void resetPlayerLocation() {
            lastPlayerPos.xCoord = Minecraft.getMinecraft().thePlayer.posX;
            lastPlayerPos.yCoord = Minecraft.getMinecraft().thePlayer.posY;
            lastPlayerPos.zCoord = Minecraft.getMinecraft().thePlayer.posZ;
        }

        @SubscribeEvent
        public void onPlayerLogIn(FMLNetworkEvent.ClientConnectedToServerEvent e) {
            connected = true;
            configSent = false;
        }

        @SubscribeEvent
        public void onPlayerLogOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
            connected = false;
            clientTasks.clear();
            clientTasks.add(ClientProxy::clearHints);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onConfigChange(ConfigChangedEvent.PostConfigChangedEvent e) {
            if (connected && e.modID.equals(ImpactStructures.MOD_ID)) {
                configSent = false;
                sendRegistryOrderToServer();
            }
        }

        @SubscribeEvent
        public void onClientTick(ClientTickEvent e) {
            if (e.phase == Phase.END) {
                lightingRevision++;
                Runnable task;
                while ((task = clientTasks.poll()) != null) task.run();
            }
            if (e.phase == Phase.END && Minecraft.getMinecraft().thePlayer != null) {
                if (currentHints != null) endOpenHintBatch();
                Vec3 playerPos = Minecraft.getMinecraft().thePlayer.getPosition(1);
                boolean sortRequired = false;
                // The allGroups is implicitly sorted by creationTime
                // here we exploit this ordering to reduce iteration size
                int deadline = Minecraft.getMinecraft().thePlayer.ticksExisted
                        - ConfigurationHandler.INSTANCE.getHintLifespan();
                int i;
                for (i = 0; i < allGroups.size(); i++) {
                    if (allGroups.get(i).getCreationTime() > deadline) {
                        break;
                    }
                }
                if (i != 0) {
                    sortRequired = true;
                    // clear this block and go no further
                    List<HintGroup> toRemove = allGroups.subList(0, i);
                    toRemove.forEach(ClientProxy::removeGroup);
                    toRemove.clear();
                }
                if (allHintsDirty) {
                    allHintsForRender.clear();
                    // we need greatly efficient sequential access and mildly efficient sorting
                    // rebuild is sort of rare, and it's expected to lag anyway
                    // so no min-heap here
                    for (HintGroup c : allGroups) allHintsForRender.addAll(c.getHints());
                    allHintsDirty = false;
                    sortRequired = true;
                }
                if (sortRequired || playerPos.squareDistanceTo(lastPlayerPos) > 1e-2) {
                    // only redo sort if player moved some distance
                    // default is 0.1 block
                    // if there was a full rebuild, go sort it as well
                    for (HintParticleInfo hint : allHintsForRender) {
                        hint.sortDistance = hint.getSquareDistanceTo(playerPos);
                    }
                    allHintsForRender.sort(Comparator.comparingDouble(info -> -info.sortDistance));
                    resetPlayerLocation();
                }
            }
        }
    }

    public static class ForgeEventHandler {

        @SubscribeEvent
        public void onTextureStitch(TextureStitchEvent.Pre event) {
            if (event.map.getTextureType() == 0) {
                HintFrameTexture texture = HintFrameTexture.INSTANCE;
                event.map.setTextureEntry(texture.getIconName(), texture);
            }
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onMouseEvent(MouseEvent event) {
            final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            if (player == null || player.isDead) {
                return;
            }

            final ItemStack heldItem = player.getHeldItem();

            if (heldItem == null || !(heldItem.getItem() instanceof ItemConstructableTrigger)) return;

            if (event.button != 2 || !event.buttonstate) return;

            // Sink the middle click as soon as a projector is held, so vanilla pick block does not run
            event.setCanceled(true);

            final boolean isSneaking = player.isSneaking();
            if (isSneaking && !ChannelWipeConfirmation.confirm(player)) return;

            ChannelPickHandler.handleWorldPick(
                    player.worldObj,
                    player,
                    heldItem,
                    Minecraft.getMinecraft().objectMouseOver,
                    isSneaking);
        }

        @SubscribeEvent
        public void onEntityJoinWorld(EntityJoinWorldEvent e) {
            if (e.world.isRemote && e.entity instanceof EntityClientPlayerMP) {
                sendRegistryOrderToServer();
            }
        }

        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load e) {
            if (e.world.isRemote) {
                clearHints();
            }
        }

        @SubscribeEvent
        public void onRenderOverlay(RenderGameOverlayEvent.Post e) {
            if (allHintsForRender.isEmpty()) return;
            Minecraft mc = Minecraft.getMinecraft();
            if (e.type != RenderGameOverlayEvent.ElementType.ALL || mc.thePlayer == null || mc.theWorld == null
                    || mc.currentScreen != null || mc.gameSettings.hideGUI) return;
            EntityLivingBase view = mc.renderViewEntity;
            if (view == null) return;
            Vec3 from = view.getPosition(e.partialTicks);
            double eyeX = from.xCoord, eyeY = from.yCoord, eyeZ = from.zCoord;
            Vec3 look = view.getLook(e.partialTicks);
            Vec3 to = from.addVector(look.xCoord * 64, look.yCoord * 64, look.zCoord * 64);
            double obstructionDistance = MarkerRayTrace.obstructionDistance(mc.theWorld, from, to);
            double nearest = Double.POSITIVE_INFINITY;
            HintParticleInfo selected = null;
            for (HintParticleInfo hint : allHintsForRender) {
                if (hint.w != mc.theWorld || !hint.textured && hint.markerKind == null && hint.frameColor == null) continue;
                double distance = MarkerRayTrace.hintIntersection(eyeX, eyeY, eyeZ,
                        look.xCoord, look.yCoord, look.zCoord, hint.x, hint.y, hint.z,
                        hint.markerKind, hint.frameColor != null, 64);
                if (!hint.rendersThroughBlocks() && distance > obstructionDistance + 0.01) continue;
                if (distance < nearest) {
                    nearest = distance;
                    selected = hint;
                }
            }
            if (selected == null) return;
            String label = selected.getDisplayName();
            String colorLabel = selected.frameColor == null ? null
                    : I18n.format("tile.impactapi.blockhint." + selected.frameColor.getMetadata() + ".name");
            int labelWidth = mc.fontRenderer.getStringWidth(label);
            int colorWidth = colorLabel == null ? 0 : mc.fontRenderer.getStringWidth(colorLabel);
            int width = Math.max(labelWidth, colorWidth);
            int centerX = e.resolution.getScaledWidth() / 2;
            int y = e.resolution.getScaledHeight() / 2 + 14;
            int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
            int height = mc.fontRenderer.FONT_HEIGHT + (colorLabel == null ? 0 : lineHeight);
            Gui.drawRect(centerX - width / 2 - 4, y - 3,
                    centerX + (width + 1) / 2 + 4, y + height + 3, 0xB0000000);
            int color = selected.markerKind == null ? 0xEEEEEE : HintRenderStyle.markerColor(selected.markerKind);
            mc.fontRenderer.drawStringWithShadow(label, centerX - labelWidth / 2, y, color);
            if (colorLabel != null) {
                mc.fontRenderer.drawStringWithShadow(colorLabel, centerX - colorWidth / 2, y + lineHeight, 0xDDDDDD);
            }
        }

        @SubscribeEvent
        public void onRenderWorldLast(RenderWorldLastEvent e) {
            if (allHintsForRender.isEmpty()) return;

            Profiler p = Minecraft.getMinecraft().mcProfiler;

            p.startSection("HintParticle");
            p.startSection("Prepare");
            Frustrum frustrum = new Frustrum();
            Entity entitylivingbase = Minecraft.getMinecraft().renderViewEntity;
            double d0 = entitylivingbase.lastTickPosX
                    + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * e.partialTicks;
            double d1 = entitylivingbase.lastTickPosY
                    + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * e.partialTicks;
            double d2 = entitylivingbase.lastTickPosZ
                    + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * e.partialTicks;
            int i0 = (int) d0, i1 = (int) d1, i2 = (int) d2;
            frustrum.setPosition(d0, d1, d2);
            visibleMarkers.clear();
            usedTextures.clear();

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
                    | GL11.GL_CURRENT_BIT | GL11.GL_POLYGON_BIT);
            // we need the back facing rendered because the thing is transparent
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND); // enable blend so it is transparent
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(-1, -1);
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            GL11.glTranslated(-d0 + i0, -d1 + i1, -d2 + i2);
            Tessellator tes = Tessellator.instance;
            tes.startDrawing(GL11.GL_TRIANGLES);
            int verticesDrawn = 0;
            for (int i = 0, allHintsForRenderSize = allHintsForRender.size(); i < allHintsForRenderSize; i++) {
                HintParticleInfo hint = allHintsForRender.get(i);
                if (!hint.isInFrustrum(frustrum)) continue;
                if (hint.markerKind != null) visibleMarkers.add(hint);
                for (int pass = 0; pass < 3; pass++) {
                    if (pass == 0 && !hint.textured || pass == 1 && hint.markerKind == null
                            || pass == 2 && hint.frameColor == null) continue;
                    int maxVertices = pass == 2 ? 6 * 4 * 6 : 6 * 2 * 3;
                    // Preserve far-to-near order without flushing between textures, markers and frames.
                    if (verticesDrawn > 0 && verticesDrawn + maxVertices > 32760) {
                        p.endStartSection("Draw");
                        tes.draw();
                        tes.startDrawing(GL11.GL_TRIANGLES);
                        verticesDrawn = 0;
                        p.endStartSection("Prepare");
                    }
                    if (pass == 2) {
                        verticesDrawn += hint.drawColorFrame(tes, d0, d1, d2, i0, i1, i2);
                    } else {
                        hint.draw(tes, d0, d1, d2, i0, i1, i2, pass == 1);
                        verticesDrawn += maxVertices;
                    }
                }
            }
            p.endStartSection("Draw");
            tes.draw();
            p.endSection();

            GL11.glPopAttrib();
            GL11.glPopMatrix();
            drawServiceMarkers(d0, d1, d2);
            p.endSection();
        }

        private static void drawServiceMarkers(double eyeX, double eyeY, double eyeZ) {
            if (visibleMarkers.isEmpty()) return;
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
                    | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            int cameraX = (int) eyeX, cameraY = (int) eyeY, cameraZ = (int) eyeZ;
            GL11.glTranslated(-eyeX + cameraX, -eyeY + cameraY, -eyeZ + cameraZ);
            Tessellator tes = Tessellator.instance;

            // Only hidden error edges pass GREATER; no filled faces or textures are drawn through walls.
            GL11.glDepthFunc(GL11.GL_GREATER);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineStipple(1, (short) 0x0F0F);
            GL11.glLineWidth(1.5F);
            tes.startDrawing(GL11.GL_LINES);
            for (HintParticleInfo hint : visibleMarkers) {
                if (!hint.rendersThroughBlocks()) continue;
                int color = HintRenderStyle.markerColor(hint.markerKind);
                tes.setBrightness(0x00F000F0);
                tes.setColorRGBA(color >> 16 & 255, color >> 8 & 255, color & 255, 140);
                drawFrame(tes, hint.x - cameraX, hint.y - cameraY, hint.z - cameraZ, hint.markerKind);
            }
            tes.draw();

            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            for (int pass = 0; pass < 2; pass++) {
                GL11.glLineWidth(pass == 0 ? 3 : 1);
                tes.startDrawing(GL11.GL_LINES);
                for (HintParticleInfo hint : visibleMarkers) {
                    int color = pass == 0 ? 0x141414 : HintRenderStyle.markerColor(hint.markerKind);
                    tes.setBrightness(0x00F000F0);
                    tes.setColorRGBA(color >> 16 & 255, color >> 8 & 255, color & 255, 230);
                    drawFrame(tes, hint.x - cameraX, hint.y - cameraY, hint.z - cameraZ, hint.markerKind);
                }
                tes.draw();
            }
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        private static void drawFrame(Tessellator tes, int x, int y, int z, HintMarkerKind kind) {
            drawFrame(tes, x, y, z, HintRenderStyle.cubeSize(kind));
        }

        private static void drawFrame(Tessellator tes, int x, int y, int z, double size) {
            double inset = (HintRenderStyle.BLOCK_SIZE - size) / 2;
            double minX = x + inset - HintRenderStyle.FRAME_MARGIN;
            double minY = y + inset - HintRenderStyle.FRAME_MARGIN;
            double minZ = z + inset - HintRenderStyle.FRAME_MARGIN;
            double maxX = x + inset + size + HintRenderStyle.FRAME_MARGIN;
            double maxY = y + inset + size + HintRenderStyle.FRAME_MARGIN;
            double maxZ = z + inset + size + HintRenderStyle.FRAME_MARGIN;
            for (int corner = 0; corner < 8; corner++) {
                double cx = (corner & 1) == 0 ? minX : maxX;
                double cy = (corner & 2) == 0 ? minY : maxY;
                double cz = (corner & 4) == 0 ? minZ : maxZ;
                if ((corner & 1) == 0) { tes.addVertex(cx, cy, cz); tes.addVertex(maxX, cy, cz); }
                if ((corner & 2) == 0) { tes.addVertex(cx, cy, cz); tes.addVertex(cx, maxY, cz); }
                if ((corner & 4) == 0) { tes.addVertex(cx, cy, cz); tes.addVertex(cx, cy, maxZ); }
            }
        }
    }
}
