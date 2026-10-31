package space.impact.structures;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import space.impact.structures.util.ItemStackMap;

/** Small local tooltip registry; lookup uses the same item matching as channel bindings. */
public final class ItemTooltipRegistry {
    private static final Map<ItemStack, List<Supplier<String>>> TOOLTIPS = new ItemStackMap<>(true);

    public static synchronized void addItemTooltip(ItemStack stack, Supplier<String> text) {
        if (stack == null || text == null) throw new IllegalArgumentException("Tooltip requires a stack and text");
        TOOLTIPS.computeIfAbsent(stack, key -> new ArrayList<>()).add(text);
    }

    public static Supplier<String> translatedText(String key, Object... arguments) {
        final Object[] copy = arguments.clone();
        return () -> StatCollector.translateToLocalFormatted(key, copy);
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        List<Supplier<String>> texts;
        synchronized (ItemTooltipRegistry.class) {
            texts = TOOLTIPS.get(event.itemStack);
            if (texts == null) return;
            texts = new ArrayList<>(texts);
        }
        for (Supplier<String> text : texts) event.toolTip.add(text.get());
    }
}
