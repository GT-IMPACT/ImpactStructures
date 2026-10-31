package space.impact.api;

import static space.impact.api.Main.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import space.impact.api.hint.HintColor;
import space.impact.api.hint.HintMarkerKind;
import space.impact.api.alignment.IAlignment;
import space.impact.api.alignment.IAlignmentProvider;
import space.impact.api.alignment.enumerable.ExtendedFacing;
import space.impact.api.net.AlignmentMessage;
import space.impact.api.structure.AutoPlaceEnvironment;
import space.impact.api.structure.IStructureDefinition;

import cpw.mods.fml.common.network.NetworkRegistry;

/**
 * Public API for IMPACT structure definitions, alignment and holograms.
 * Colored hints and service markers are independent.
 */
public class ImpactAPI {

    public static final String MOD_ID = "impactapi";
    public static final String CHANNEL_SHOW_ERROR = "show_error";
    static final ThreadLocal<Object> instrument = new ThreadLocal<>();

    /**
     * Start instrumenting <b>for the current thread only</b>. When instrumentation is enabled,
     * {@link IStructureDefinition}'s builtin iteration methods will send {@link StructureEvent} and its subclasses when
     * appropriate. Otherwise, these events will not be sent at all, regardless of whether a listener for these events
     * are registered.
     * <p>
     * Identifiers are required to be value-comparable, i.e. overrides {@link Object#equals(Object)}. Suggested
     * identifier includes
     * <ul>
     * <li>Mod Instance or {@link cpw.mods.fml.common.ModContainer}</li>
     * <li>{@link space.impact.api.alignment.constructable.IConstructable} and its friends, if you are
     * triggering structure check via this interface.</li>
     * <li>{@link net.minecraft.util.ResourceLocation}</li>
     * </ul>
     * <p>
     * It's an API abuse for normal {@link IStructureDefinition} users to depend on this feature. They have full control
     * and full knowledge of the {@link IStructureDefinition} they are using. Using events to instrument the structure
     * check/autoplace is completely unnecessary and does nothing but add overhead.
     * <p>
     * Instruments are required to call {@link #disableInstrument()} whenever it is done with its business, as there can
     * be only one instrument for any given thread.
     *
     * @param identifier an identifier sent along the actual event.
     * @throws IllegalStateException if instrument has already been enabled.
     */
    public static void enableInstrument(Object identifier) {
        if (isInstrumentEnabled()) throw new IllegalStateException();
        instrument.set(identifier);
    }

    /**
     * Stop instrumenting.
     *
     * @throws IllegalStateException if instrumenting hasn't been enabled.
     */
    public static void disableInstrument() {
        if (!isInstrumentEnabled()) throw new IllegalStateException();
        instrument.set(null);
    }

    /**
     * Check if instrumenting is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public static boolean isInstrumentEnabled() {
        return instrument.get() != null;
    }

    /**
     * Start a batch of hinting. All hints particles generated during one batch will be considered to belong to one
     * hologram.
     * <p>
     * You don't need to call this unless your constructable tool didn't call
     * {@link space.impact.api.alignment.constructable.ConstructableUtility#handle(ItemStack, EntityPlayer, World, int, int, int, int)}
     */
    public static void startHinting(World w) {
        proxy.startHinting(w);
    }

    /**
     * End current batch of hinting. All hints particles generated during one batch will be considered to belong to one
     * hologram.
     * <p>
     * You don't need to call this unless your constructable tool didn't call
     * {@link space.impact.api.alignment.constructable.ConstructableUtility#handle(ItemStack, EntityPlayer, World, int, int, int, int)}
     */
    public static void endHinting(World w) {
        proxy.endHinting(w);
    }

    /**
     * Generate a new hint particle on client side at given location using given textures with a tint.
     *
     * @param w     World to spawn. Usually the client world.
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     * @param icons 6 texture. in forge direction order.
     * @param RGBa  a 4 short array. tint in rgba form. currently alpha channel is ignored, but we might change this
     *              later on.
     */
    public static void hintParticleTinted(World w, int x, int y, int z, IIcon[] icons, short[] RGBa) {
        proxy.hintParticleTinted(w, x, y, z, icons, RGBa);
    }

    /**
     * Draw custom textures with a tint and an item name under the crosshair.
     *
     * @param icons six face textures in Forge direction order; individual faces may be null
     * @param RGBa four tint components, as in {@link #hintParticleTinted(World, int, int, int, IIcon[], short[])}
     * @param displayStack optional item used only for the name, including metadata and NBT; copied on the client.
     *                     Null keeps the generic hint label. Its textures are not used and it is not consumed.
     */
    public static void hintParticleTinted(World w, int x, int y, int z, IIcon[] icons, short[] RGBa,
            ItemStack displayStack) {
        proxy.hintParticleTinted(w, x, y, z, icons, RGBa, displayStack);
    }

    /**
     * Generate a new hint particle on client side at given location using textures from given block with a tint.
     *
     * @param w     World to spawn. Usually the client world.
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     * @param block block to take texture from
     * @param meta  the meta of block to take texture from
     * @param RGBa  a 4 short array. tint in rgba form. currently alpha channel is ignored, but we might change this
     *              later on.
     */
    public static void hintParticleTinted(World w, int x, int y, int z, Block block, int meta, short[] RGBa) {
        proxy.hintParticleTinted(w, x, y, z, block, meta, RGBa);
    }

    /**
     * Generate a new hint particle on client side at given location using given textures.
     *
     * @param w     World to spawn. Usually the client world.
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     * @param icons 6 texture. in forge direction order.
     */
    public static void hintParticle(World w, int x, int y, int z, IIcon[] icons) {
        proxy.hintParticle(w, x, y, z, icons);
    }

    /**
     * Draw custom textures with an item name under the crosshair, independently of the textures.
     *
     * @param icons six face textures in Forge direction order; individual faces may be null
     * @param displayStack optional item used only for the name, including metadata and NBT; copied on the client.
     *                     Null keeps the generic hint label. Its textures are not used and it is not consumed.
     */
    public static void hintParticle(World w, int x, int y, int z, IIcon[] icons, ItemStack displayStack) {
        proxy.hintParticle(w, x, y, z, icons, displayStack);
    }

    /**
     * Generate a new hint particle on client side at given location using textures from given block.
     *
     * @param w     World to spawn. Usually the client world.
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     * @param block block to take texture from
     * @param meta  the meta of block to take texture from
     */
    public static void hintParticle(World w, int x, int y, int z, Block block, int meta) {
        proxy.hintParticle(w, x, y, z, block, meta);
    }

    /** Draw a colored hint. Every palette color is available for ordinary structure elements. */
    public static void hintParticle(World w, int x, int y, int z, HintColor color) {
        hintParticle(w, x, y, z, getBlockHint(), color.getMetadata());
    }

    /** Add a separate colored frame to a client hint, preserving its textures, tint and block identity. */
    public static void hintFrame(World w, int x, int y, int z, HintColor color) {
        if (color == null) throw new IllegalArgumentException("A frame color is required");
        proxy.hintFrame(w, x, y, z, color);
    }

    /** Draw a translucent service cube without reserving any colored block metadata. */
    public static void hintMarker(World w, int x, int y, int z, HintMarkerKind kind) {
        if (kind == null) throw new IllegalArgumentException("A service marker kind is required");
        proxy.hintMarker(w, x, y, z, kind);
    }

    /** Neutral, real-world placeholder used only for creative construction. */
    public static Block getConstructionPlaceholder() {
        return Main.constructionPlaceholder;
    }

    /**
     * Add a translucent red error cube and outline without changing the hint's icons or color.
     * Create an ERROR marker at the given position if no hint exists there.
     * Occluded error edges use a dashed outline; filled faces remain hidden behind world blocks.
     * <p>
     * Can be called on either client side or server side. Server side will schedule a network message to update it for
     * this player only.
     *
     * @return true if a hint was updated, a marker was created, or an update instruction was sent;
     *         false if the player or world is not applicable.
     */
    public static boolean markHintParticleError(EntityPlayer player, World w, int x, int y, int z) {
        return proxy.markHintParticleError(player, w, x, y, z);
    }

    /**
     * Update the tint of given hint particle. Do nothing if particle not found.
     *
     * @return false if nothing updated. true if updated.
     */
    public static boolean updateHintParticleTint(EntityPlayer player, World w, int x, int y, int z, short[] RGBa) {
        return proxy.updateHintParticleTint(player, w, x, y, z, RGBa);
    }

    /**
     * Query the ExtendedFacing of this tile entity from client side. Can be sent only on client side. The
     * ExtendedFacing will later be set onto given tile entity via
     * {@link space.impact.api.alignment.IAlignment#setExtendedFacing(ExtendedFacing)} upon arrival of
     * server reply.
     * <p>
     * The server side will query the {@link ExtendedFacing} of this provider using
     * {@link IAlignment#getExtendedFacing()}
     *
     * @throws IllegalArgumentException if is not tile entity or provided a null alignment
     */
    public static void queryAlignment(IAlignmentProvider provider) {
        Main.net.sendToServer(new AlignmentMessage.AlignmentQuery(provider));
    }

    /**
     * Send the ExtendedFacing of this Tile Entity to all players. Can be called on server side only.
     * <p>
     * The receiving tile entity will receive the {@link ExtendedFacing} via its
     * {@link space.impact.api.alignment.IAlignment#setExtendedFacing(ExtendedFacing)} method.
     *
     * @throws IllegalArgumentException if is not tile entity or provided a null alignment
     */
    public static void sendAlignment(IAlignmentProvider provider) {
        Main.net.sendToAll(new AlignmentMessage.AlignmentData(provider));
    }

    /**
     * Send the ExtendedFacing of this Tile Entity to given player. Can be called on server side only.
     * <p>
     * The receiving tile entity will receive the {@link ExtendedFacing} via its
     * {@link space.impact.api.alignment.IAlignment#setExtendedFacing(ExtendedFacing)} method.
     *
     * @throws IllegalArgumentException if is not tile entity or provided a null alignment
     */
    public static void sendAlignment(IAlignmentProvider provider, EntityPlayerMP player) {
        Main.net.sendTo(new AlignmentMessage.AlignmentData(provider), player);
    }

    /**
     * Send the ExtendedFacing of this Tile Entity to all players around target point. Can be called on server side
     * only.
     * <p>
     * The receiving tile entity will receive the {@link ExtendedFacing} via its
     * {@link space.impact.api.alignment.IAlignment#setExtendedFacing(ExtendedFacing)} method.
     *
     * @throws IllegalArgumentException if is not tile entity or provided a null alignment
     */
    public static void sendAlignment(IAlignmentProvider provider, NetworkRegistry.TargetPoint targetPoint) {
        Main.net.sendToAllAround(new AlignmentMessage.AlignmentData(provider), targetPoint);
    }

    /**
     * Send the ExtendedFacing of this Tile Entity to all players in that dimension. Can be called on server side only.
     * <p>
     * The receiving tile entity will receive the {@link ExtendedFacing} via its
     * {@link space.impact.api.alignment.IAlignment#setExtendedFacing(ExtendedFacing)} method.
     *
     * @throws IllegalArgumentException if is not tile entity or provided a null alignment
     */
    public static void sendAlignment(IAlignmentProvider provider, World dimension) {
        Main.net.sendToDimension(new AlignmentMessage.AlignmentData(provider), dimension.provider.dimensionId);
    }

    /**
     * Get the block holding the sixteen IMPACT hint colors.
     */
    public static Block getBlockHint() {
        return Main.blockHint;
    }

    /**
     * Get the item holding the sixteen IMPACT hint colors.
     */
    public static Item getItemBlockHint() {
        return Main.itemBlockHint;
    }

    /**
     * Get the hologram item.
     */
    public static Item getDefaultHologramItem() {
        return Main.itemConstructableTrigger;
    }

    /**
     * Check if structure debug mode is on.
     *
     * @return true if debug mode is on
     */
    public static boolean isDebugEnabled() {
        return Main.DEBUG_MODE;
    }

    public static void setDebugEnabled(boolean enabled) {
        Main.DEBUG_MODE = enabled;
    }

    /**
     * Determines if given block can be replaced without much effort. The exact predicate clauses is not stable and will
     * be changed, but the general idea will always stay the same.
     * <p>
     * Use this in your
     * {@link space.impact.api.structure.IStructureElement#survivalPlaceBlock(Object, World, int, int, int, ItemStack, AutoPlaceEnvironment)}
     */
    @Deprecated
    public static boolean isBlockTriviallyReplaceable(World w, int x, int y, int z, EntityPlayerMP actor) {
        return isBlockTriviallyReplaceable(w, x, y, z, (EntityPlayer) actor);
    }

    /**
     * Determines if given block can be replaced without much effort. The exact predicate clauses is not stable and will
     * be changed, but the general idea will always stay the same.
     * <p>
     * Use this in your
     * {@link space.impact.api.structure.IStructureElement#survivalPlaceBlock(Object, World, int, int, int, ItemStack, AutoPlaceEnvironment)}
     */
    public static boolean isBlockTriviallyReplaceable(World w, int x, int y, int z, EntityPlayer actor) {
        // TODO extend this function a bit
        Block block = w.getBlock(x, y, z);
        return block.isAir(w, x, y, z) || block.isReplaceable(w, x, y, z);
    }

    /**
     * Send chat to player, but throttled.
     *
     * @param throttleKey      throttle key. Must properly implement {@link Object#hashCode()} and
     *                         {@link Object#equals(Object)} to handle value equality.
     * @param player           player to send chat to
     * @param text             chat to send
     * @param intervalRequired interval required before last recorded time to actually send the message. unit in
     *                         millisecond. we purposefully chose to not use a bigger data type to limit how long this
     *                         interval can be
     */
    public static void addThrottledChat(Object throttleKey, EntityPlayer player, IChatComponent text,
            short intervalRequired) {
        addThrottledChat(throttleKey, player, text, intervalRequired, false);
    }

    /**
     * Send chat to player, but throttled.
     *
     * @param throttleKey         throttle key. Must properly implement {@link Object#hashCode()} and
     *                            {@link Object#equals(Object)} to handle value equality.
     * @param player              player to send chat to
     * @param text                chat to send
     * @param intervalRequired    interval required before last recorded time to actually send the message. unit in
     *                            millisecond. we purposefully chose to not use a bigger data type to limit how long
     *                            this interval can be
     * @param forceUpdateLastSend if true, always update the last send time, even if the message isn't actually sent.
     */
    public static void addThrottledChat(Object throttleKey, EntityPlayer player, IChatComponent text,
            short intervalRequired, boolean forceUpdateLastSend) {
        proxy.addThrottledChat(throttleKey, player, text, intervalRequired, forceUpdateLastSend);
    }

    /**
     * Register a channel description here. The description should describe how a channel will be used inside your mod.
     *
     * @param channel     channel key
     * @param modid       your modid
     * @param description localization key for your description
     */
    public static void registerChannelDescription(final String channel, final String modid, final String description) {
        ChannelDescription.set(channel, modid, description);
    }

    /**
     * Register an item stack as an indicator for a (channel, value) pair.
     *
     * @param channel      channel key
     * @param modid        your modid
     * @param channelValue value
     * @param stack        item stack. having a metadata of
     *                     {@link net.minecraftforge.oredict.OreDictionary#WILDCARD_VALUE} ignores metadata while
     *                     retaining NBT matching. A tag with {@code tag.getBoolean("*") == true} ignores NBT.
     *                     Combine both wildcards to match any stack of the item.
     */
    public static void registerChannelItem(final String channel, final String modid, final int channelValue,
            final ItemStack stack) {
        ChannelDescription.item(channel, channelValue, stack);
        ItemTooltipRegistry.addItemTooltip(
                stack,
                ItemTooltipRegistry.translatedText("impactapi.tooltip.channelvalue", channelValue, channel));
        ItemTooltipRegistry.addItemTooltip(
                stack,
                ItemTooltipRegistry.translatedText("impactapi.tooltip.indicator_dnd", channelValue, channel));
    }
}
