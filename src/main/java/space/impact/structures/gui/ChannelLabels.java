package space.impact.structures.gui;

import java.util.TreeMap;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

import space.impact.structures.ChannelDescription;

/** Client-only presentation; channel identifiers remain unchanged in saved data. */
final class ChannelLabels {

    private ChannelLabels() {
    }

    static String get(String channel) {
        ChannelDescription description = ChannelDescription.getAll().get(channel);
        if (description != null) {
            for (String descriptionKey : new TreeMap<>(description.getDescriptions()).values()) {
                String label = translated(descriptionKey + ".label");
                if (label != null) {
                    return label;
                }
            }
        }
        String readable = channel.replaceAll("[_:./-]+", " ").trim().replaceAll("\\s+", " ");
        return readable.isEmpty() ? I18n.format("item.impactapi.constructableTrigger.gui.unnamed")
                : Character.toUpperCase(readable.charAt(0)) + readable.substring(1);
    }

    private static String translated(String key) {
        String text = EnumChatFormatting.getTextWithoutFormattingCodes(I18n.format(key)).trim();
        return text.isEmpty() || text.equals(key) ? null : text;
    }
}
