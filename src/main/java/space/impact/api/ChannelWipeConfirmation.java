package space.impact.api;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side-only confirmation gate for "wipe all channels" shift+middle-click gesture.
 */
@SideOnly(Side.CLIENT)
public final class ChannelWipeConfirmation {

    private static final long CONFIRM_WINDOW_MS = 5000;
    private static long armedAtMs = -1;

    private ChannelWipeConfirmation() {}

    /**
     * Call this before invoking {@link ChannelPickHandler} whenever the wipe gesture (shift+middle-click) is detected.
     *
     * @return true if this is a confirming second press within the window and the wipe should proceed now; false if
     *         this press only armed the confirmation.
     */
    public static boolean confirm(EntityPlayer player) {
        long now = Minecraft.getSystemTime();
        if (now - armedAtMs > CONFIRM_WINDOW_MS) {
            armedAtMs = now;
            player.addChatMessage(new ChatComponentTranslation("impactapi.pickchannel.confirmwipe"));
            return false;
        }
        armedAtMs = -1;
        return true;
    }
}
