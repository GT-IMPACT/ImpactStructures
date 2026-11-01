package space.impact.structures.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import space.impact.structures.ChannelDescription;

class ChannelCompletionTest {
    @Test
    void clickAcceptsCompletionBeforeTheKeyFieldLosesFocus() throws Exception {
        Field locale = I18n.class.getDeclaredField("i18nLocale");
        locale.setAccessible(true);
        Object previousLocale = locale.get(null);
        locale.set(null, new Locale());
        try {
            ChannelDescription.get("test-click-channel");
            GuiScreenConfigureChannels gui = new GuiScreenConfigureChannels(
                    mock(Container.class), new ItemStack(new Item()));
            FontRenderer font = mock(FontRenderer.class);
            when(font.trimStringToWidth(anyString(), anyInt())).thenAnswer(call -> call.getArgument(0));
            when(font.trimStringToWidth(anyString(), anyInt(), anyBoolean())).thenAnswer(call -> call.getArgument(0));
            GuiTextField key = new GuiTextField(font, 45, 119, 118, 12);
            GuiTextField value = new GuiTextField(font, 45, 139, 118, 12);
            set(gui, "key", key);
            set(gui, "value", value);
            for (int id = 0; id < 3; id++) gui.addButton(new GuiButton(id, 0, 157, ""));
            key.setText("test-click");
            key.setFocused(true);
            Method update = GuiScreenConfigureChannels.class.getDeclaredMethod("updateButtons");
            update.setAccessible(true);
            update.invoke(gui);
            gui.mouseClicked(46, 134, 0);
            assertEquals("test-click-channel", key.getText());
            assertEquals("1", value.getText());
            assertFalse(key.isFocused());
            assertTrue(value.isFocused());
        } finally {
            locale.set(null, previousLocale);
        }
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = GuiScreenConfigureChannels.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
