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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.input.Keyboard;

import space.impact.structures.ChannelDescription;
import space.impact.structures.alignment.constructable.ChannelDataAccessor;

class ChannelScreenTest {
    private Field localeField;
    private Object previousLocale;
    private ItemStack trigger;
    private GuiScreenConfigureChannels gui;
    private Map<String, String> translations;

    @BeforeEach
    void setup() throws Exception {
        localeField = I18n.class.getDeclaredField("i18nLocale");
        localeField.setAccessible(true);
        previousLocale = localeField.get(null);
        Locale locale = new Locale();
        localeField.set(null, locale);
        Field properties = Locale.class.getDeclaredField("field_135032_a");
        properties.setAccessible(true);
        translations = (Map<String, String>) properties.get(locale);
        Properties russian = new Properties();
        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(
                "/assets/impactapi/lang/ru_RU.lang"), StandardCharsets.UTF_8)) {
            russian.load(reader);
        }
        for (String name : russian.stringPropertyNames()) translations.put(name, russian.getProperty(name));
        trigger = new ItemStack(new Item());
        for (int i = 0; i < 40; i++) ChannelDescription.get(String.format("screen-channel-%02d", i));
        gui = new GuiScreenConfigureChannels(mock(Container.class), trigger);
        FontRenderer font = mock(FontRenderer.class);
        when(font.trimStringToWidth(anyString(), anyInt())).thenAnswer(call -> call.getArgument(0));
        when(font.trimStringToWidth(anyString(), anyInt(), anyBoolean())).thenAnswer(call -> call.getArgument(0));
        Field renderer = GuiScreen.class.getDeclaredField("fontRendererObj");
        renderer.setAccessible(true);
        renderer.set(gui, font);
        gui.width = 480;
        gui.height = 360;
        gui.initializeControls();
        field("search").setText("screen-channel-");
        invoke("refreshChannels");
    }

    @AfterEach
    void restoreLocale() throws Exception {
        localeField.set(null, previousLocale);
    }

    @Test
    void clickingAfterScrollSelectsVisibleChannelAndPreservesSavedValue() throws Exception {
        ChannelDataAccessor.setChannelData(trigger, "screen-channel-07", 42);
        invoke("refreshChannels");
        int x = gui.getGuiLeft() + 17, y = gui.getGuiTop() + 71;
        gui.mouseClicked(x, y, 0);
        list().wheel(x, y, -120);
        list().wheel(x, y, -120);
        gui.mouseClicked(x, y + ChannelListModel.ROW_HEIGHT, 0);
        assertEquals("Screen channel 07", field("key").getText());
        assertEquals("42", field("value").getText());
        assertEquals(6, list().firstRow());
        assertFalse(field("search").isFocused());
        assertTrue(gui.getButtonList().get(1).enabled);
    }

    @Test
    void screenRoutesScrollbarDraggingWithoutChangingEditor() throws Exception {
        int y = gui.getGuiTop() + 66;
        gui.mouseClicked(gui.getGuiLeft() + 17, y + 5, 0);
        String selected = field("key").getText();
        gui.mouseClicked(list().scrollbarX() + 2, y + 3, 0);
        gui.mouseClickMove(0, y + list().height() + 100, 0, 100);
        assertEquals(list().size() - list().visibleRows(), list().firstRow());
        assertEquals(selected, field("key").getText());
        gui.mouseMovedOrUp(0, 0, 0);
        assertFalse(list().isDragging());
        assertEquals(selected, field("key").getText());
    }

    @Test
    void editingAddsUpdatesRemovesAndRejectsInvalidValues() throws Exception {
        field("key").setText("custom-channel");
        for (String invalid : new String[] {"", "0", "-1", "2147483648", "garbage"}) {
            field("value").setText(invalid);
            invoke("updateButtons");
            assertFalse(gui.getButtonList().get(0).enabled);
            gui.doActionPerformed(gui.getButtonList().get(0));
            assertFalse(ChannelDataAccessor.hasSubChannel(trigger, "custom-channel"));
        }
        field("value").setText("2147483647");
        invoke("updateButtons");
        gui.doActionPerformed(gui.getButtonList().get(0));
        assertEquals(Integer.MAX_VALUE, ChannelDataAccessor.getChannelData(trigger, "custom-channel"));
        field("value").setText("5");
        gui.doActionPerformed(gui.getButtonList().get(0));
        assertEquals(5, ChannelDataAccessor.getChannelData(trigger, "custom-channel"));
        gui.doActionPerformed(gui.getButtonList().get(1));
        assertFalse(ChannelDataAccessor.hasSubChannel(trigger, "custom-channel"));
    }

    @Test
    void searchKeyboardSelectionAndEnterApplyWithoutRegisteringUnknownQueries() throws Exception {
        field("search").setText("screen-channel-19");
        invoke("refreshChannels");
        gui.keyTyped('\0', Keyboard.KEY_DOWN);
        assertEquals("Screen channel 19", field("key").getText());
        gui.keyTyped('\r', Keyboard.KEY_RETURN);
        assertTrue(field("value").isFocused());
        assertFalse(field("search").isFocused());
        field("value").setText("12");
        gui.keyTyped('\r', Keyboard.KEY_RETURN);
        assertEquals(12, ChannelDataAccessor.getChannelData(trigger, "screen-channel-19"));
        field("search").setText("screen-nonexistent-query");
        invoke("refreshChannels");
        assertEquals(0, list().size());
        assertFalse(ChannelDescription.has("screen-nonexistent-query"));
    }

    @Test
    void indicatorDropClearsFilterSelectsChannelAndPreservesOtherChannels() throws Exception {
        ChannelDataAccessor.setChannelData(trigger, "existing-custom", 3);
        ItemStack indicator = new ItemStack(new Item());
        ChannelDescription.item("screen-indicator", 9, indicator);
        assertFalse(gui.acceptIndicatorDrop(0, 0, indicator, 0));
        assertFalse(gui.acceptIndicatorDrop(gui.getGuiLeft() + 5, gui.getGuiTop() + 5, indicator, 1));
        assertTrue(gui.acceptIndicatorDrop(gui.getGuiLeft() + 5, gui.getGuiTop() + 5, indicator, 0));
        assertEquals("", field("search").getText());
        assertEquals("Screen indicator", field("key").getText());
        assertEquals("screen-indicator", list().selected());
        assertEquals("9", field("value").getText());
        assertEquals(3, ChannelDataAccessor.getChannelData(trigger, "existing-custom"));
    }

    @Test
    void clearRequiresConfirmationAndResizingKeepsEditorAndValidBounds() throws Exception {
        ChannelDataAccessor.setChannelData(trigger, "existing-custom", 3);
        invoke("refreshChannels");
        invoke("updateButtons");
        gui.doActionPerformed(gui.getButtonList().get(2));
        assertTrue(ChannelDataAccessor.hasSubChannel(trigger, "existing-custom"));
        gui.doActionPerformed(gui.getButtonList().get(2));
        assertFalse(ChannelDataAccessor.hasSubChannel(trigger));
        field("key").setText("unsaved-channel");
        field("value").setText("17");
        gui.width = 320;
        gui.height = 240;
        gui.initializeControls();
        assertEquals("unsaved-channel", field("key").getText());
        assertEquals("17", field("value").getText());
        assertTrue(list().visibleRows() >= 3);
        assertTrue(gui.getGuiLeft() >= 0 && gui.getGuiTop() >= 0);
        assertTrue(gui.getGuiLeft() + gui.getXSize() <= 320);
        assertTrue(gui.getGuiTop() + gui.getYSize() <= 240);
        assertEquals(5, gui.getButtonList().size());
    }

    @Test
    void localizedNameAndCodeFindSameChannelAndOnlyCodeIsSaved() throws Exception {
        registerLabel("test_owned_channel", "Тестовый канал");
        for (String query : new String[] {"ТЕСТОВЫЙ", "test_OWNED"}) {
            field("search").setText(query);
            invoke("refreshChannels");
            assertEquals(1, list().size());
            assertEquals("test_owned_channel", list().channel(0));
        }
        gui.keyTyped('\0', Keyboard.KEY_DOWN);
        assertEquals("Тестовый канал", field("key").getText());
        assertEquals("Screen channel 01", gui.channelTooltip("screen-channel-01").get(0));
        assertEquals("§7Код канала: screen-channel-01", gui.channelTooltip("screen-channel-01").get(1));
        field("value").setText("8");
        gui.initializeControls();
        gui.doActionPerformed(gui.getButtonList().get(0));
        assertEquals(8, ChannelDataAccessor.getChannelData(trigger, "test_owned_channel"));
        assertEquals("8", field("value").getText());
        assertFalse(ChannelDataAccessor.hasSubChannel(trigger, "тестовый канал"));
        gui.doActionPerformed(gui.getButtonList().get(1));
        assertFalse(ChannelDataAccessor.hasSubChannel(trigger, "test_owned_channel"));
    }

    @Test
    void duplicateLabelsKeepSeparateCodesAndCustomEntryDoesNotRenameSelection() throws Exception {
        registerLabel("duplicate-one", "Общее название");
        registerLabel("duplicate-two", "Общее название");
        field("search").setText("общее название");
        invoke("refreshChannels");
        assertEquals(2, list().size());
        gui.keyTyped('\0', Keyboard.KEY_DOWN);
        gui.keyTyped('\0', Keyboard.KEY_DOWN);
        field("value").setText("7");
        gui.doActionPerformed(gui.getButtonList().get(0));
        assertEquals(7, ChannelDataAccessor.getChannelData(trigger, "duplicate-two"));
        assertFalse(ChannelDataAccessor.hasSubChannel(trigger, "duplicate-one"));
        gui.doActionPerformed(gui.getButtonList().get(4));
        assertEquals("", field("key").getText());
        field("key").writeText("MY_CUSTOM_CODE");
        invoke("updateButtons");
        gui.doActionPerformed(gui.getButtonList().get(0));
        assertTrue(ChannelDataAccessor.hasSubChannel(trigger, "my_custom_code"));
        assertEquals(7, ChannelDataAccessor.getChannelData(trigger, "duplicate-two"));
        assertEquals("My custom code", field("key").getText());
    }

    @SuppressWarnings("unchecked")
    private void registerLabel(String channel, String label) throws Exception {
        // Supply another mod's registry entry without booting Forge's mod loader.
        Field descriptions = ChannelDescription.class.getDeclaredField("descriptions");
        descriptions.setAccessible(true);
        String descriptionKey = "test.owner.channels." + channel;
        ((Map<String, String>) descriptions.get(ChannelDescription.get(channel))).put("test-owner", descriptionKey);
        translations.put(descriptionKey + ".label", label);
    }

    private GuiTextField field(String name) throws Exception { return (GuiTextField) get(name); }
    private ChannelListModel list() throws Exception { return (ChannelListModel) get("list"); }
    private Object get(String name) throws Exception {
        Field field = GuiScreenConfigureChannels.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(gui);
    }
    private void invoke(String name) throws Exception {
        Method method = GuiScreenConfigureChannels.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(gui);
    }
}
