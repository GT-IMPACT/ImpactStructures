package space.impact.structures.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import space.impact.structures.ChannelDescription;
import space.impact.structures.Main;
import space.impact.structures.alignment.constructable.ChannelDataAccessor;
import space.impact.structures.gui.GuiScrollableList.IGuiScreen;

public class GuiScreenConfigureChannels extends GuiContainer implements IGuiScreen {
    private static final String I18N_PREFIX = "item.impactapi.constructableTrigger.gui.";
    private static final int ADD_BTN = 0, UNSET_BTN = 1, WIPE_BTN = 2, DONE_BTN = 3, CUSTOM_BTN = 4;
    private static final long WIPE_CONFIRM_TIMEOUT_MS = 3000;

    private final ItemStack trigger;
    private final ChannelListModel list = new ChannelListModel();
    private final Map<String, Integer> configured = new HashMap<>();
    private GuiTextField search, key, value;
    private String editingChannel;
    private int listX, listY, listWidth, editorY;
    private boolean listFocused;
    private boolean listMouseDown;
    private long wipePendingUntil;

    public GuiScreenConfigureChannels(Container container, ItemStack trigger) {
        super(container);
        this.trigger = trigger;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        initializeControls();
    }

    // Separate from LWJGL initialization to test input without opening a game window.
    void initializeControls() {
        String previousSearch = search == null ? "" : search.getText();
        String previousKey = key == null ? "" : key.getText();
        String previousValue = value == null ? "1" : value.getText();
        int focused = key != null && key.isFocused() ? 1 : value != null && value.isFocused() ? 2 : 0;
        xSize = Math.min(420, width - 16);
        ySize = Math.min(310, height - 16);
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
        listX = guiLeft + 12;
        listY = guiTop + 66;
        listWidth = xSize - 24;
        editorY = guiTop + ySize - 82;
        list.setBounds(listX, listY, listWidth, editorY - listY - 8);
        listMouseDown = false;
        search = new GuiTextField(fontRendererObj, listX + 1, guiTop + 32, listWidth - 2, 18);
        search.setMaxStringLength(128);
        search.setText(previousSearch);
        key = new GuiTextField(fontRendererObj, listX + 1, editorY + 12, listWidth - 124, 18) {
            @Override
            public void writeText(String text) {
                super.writeText(text.toLowerCase(Locale.ROOT));
            }
        };
        key.setMaxStringLength(128);
        key.setText(editingChannel == null ? previousKey : ChannelLabels.get(editingChannel));
        key.setEnabled(editingChannel == null);
        key.setDisabledTextColour(0xDDDDDD);
        value = new GuiTextField(fontRendererObj, listX + listWidth - 88, editorY + 12, 87, 18) {
            @Override
            public void writeText(String text) {
                if (text.matches("[0-9]*")) super.writeText(text);
            }
        };
        value.setMaxStringLength(10);
        value.setText(previousValue);
        search.setFocused(focused == 0 && !listFocused);
        key.setFocused(focused == 1);
        value.setFocused(focused == 2);
        buttonList.clear();
        int buttonWidth = (listWidth - 12) / 4;
        for (int id = 0; id < 4; id++) {
            addButton(new GuiButton(id, listX + id * (buttonWidth + 4), editorY + 38, buttonWidth, 20, ""));
        }
        addButton(new GuiButton(CUSTOM_BTN, value.xPosition - 24, editorY + 11, 20, 20, "+"));
        refreshChannels();
        updateButtons();
    }

    private void refreshChannels() {
        configured.clear();
        ChannelDataAccessor.iterateChannelData(trigger).forEach(e -> configured.put(e.getKey(), e.getValue()));
        Collection<String> channels = new TreeSet<>(ChannelDescription.getAll().keySet());
        channels.addAll(configured.keySet());
        list.setChannels(channels, search.getText(), ChannelLabels::get);
    }

    private String channelName() {
        return editingChannel == null ? key.getText().trim().toLowerCase(Locale.ROOT) : editingChannel;
    }

    private int getValue() {
        try {
            return Integer.parseInt(value.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateButtons() {
        boolean existing = configured.containsKey(channelName());
        getButtonList().get(ADD_BTN).displayString = label(existing ? "set" : "add");
        getButtonList().get(ADD_BTN).enabled = !channelName().isEmpty() && getValue() > 0;
        getButtonList().get(UNSET_BTN).displayString = label("unset");
        getButtonList().get(UNSET_BTN).enabled = existing;
        getButtonList().get(WIPE_BTN).displayString = label(
                System.currentTimeMillis() < wipePendingUntil ? "wipe.confirm" : "wipe");
        getButtonList().get(WIPE_BTN).enabled = !configured.isEmpty();
        getButtonList().get(DONE_BTN).displayString = I18n.format("gui.done");
    }

    private void selectChannel(String channel) {
        if (channel == null) return;
        editingChannel = channel;
        key.setText(ChannelLabels.get(channel));
        key.setEnabled(false);
        value.setText(Integer.toString(configured.getOrDefault(channel, 1)));
        updateButtons();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        list.wheel(mouseX, mouseY, Mouse.getEventDWheel());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (list.contains(mouseX, mouseY)) {
            search.setFocused(false);
            key.setFocused(false);
            value.setFocused(false);
            listFocused = true;
            if (button == 0) {
                listMouseDown = true;
                selectChannel(list.press(mouseX, mouseY));
            }
            return;
        }
        listFocused = false;
        search.mouseClicked(mouseX, mouseY, button);
        key.mouseClicked(mouseX, mouseY, button);
        value.mouseClicked(mouseX, mouseY, button);
        if (search.isFocused() || key.isFocused() || value.isFocused()) return;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (button == 0 && listMouseDown) list.drag(mouseY);
        else super.mouseClickMove(mouseX, mouseY, button, elapsed);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
        if (button == 0 && listMouseDown) {
            listMouseDown = false;
            list.release();
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char character, int code) {
        if (code == Keyboard.KEY_ESCAPE) {
            super.keyTyped(character, code);
            return;
        }
        if (code == Keyboard.KEY_TAB) {
            boolean searchFocused = search.isFocused();
            boolean keyFocused = key.isFocused();
            search.setFocused(!searchFocused && !keyFocused);
            key.setFocused(searchFocused);
            value.setFocused(keyFocused);
            listFocused = false;
            return;
        }
        if ((search.isFocused() || listFocused) && (code == Keyboard.KEY_UP || code == Keyboard.KEY_DOWN
                || code == Keyboard.KEY_PRIOR || code == Keyboard.KEY_NEXT)) {
            int step = code == Keyboard.KEY_PRIOR || code == Keyboard.KEY_NEXT ? list.visibleRows() : 1;
            selectChannel(list.moveSelection(code == Keyboard.KEY_UP || code == Keyboard.KEY_PRIOR ? -step : step));
            return;
        }
        if (code == Keyboard.KEY_RETURN || code == Keyboard.KEY_NUMPADENTER) {
            if (search.isFocused() || listFocused) {
                selectChannel(list.selected() == null ? list.moveSelection(0) : list.selected());
                if (list.selected() != null) {
                    search.setFocused(false);
                    listFocused = false;
                    value.setFocused(true);
                    value.setCursorPositionEnd();
                    value.setSelectionPos(0);
                }
            } else if (getButtonList().get(ADD_BTN).enabled) {
                actionPerformed(getButtonList().get(ADD_BTN));
            }
            return;
        }
        String previousSearch = search.getText();
        if (search.textboxKeyTyped(character, code) || key.textboxKeyTyped(character, code)
                || value.textboxKeyTyped(character, code)) {
            if (!previousSearch.equals(search.getText())) refreshChannels();
            updateButtons();
            return;
        }
        if (!search.isFocused() && !key.isFocused() && !value.isFocused()) super.keyTyped(character, code);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || !button.enabled) return;
        String channel = channelName();
        switch (button.id) {
            case ADD_BTN:
                if (channel.isEmpty() || getValue() <= 0) return;
                ChannelDataAccessor.setChannelData(trigger, channel, getValue());
                break;
            case UNSET_BTN:
                if (channel.isEmpty()) return;
                ChannelDataAccessor.unsetChannelData(trigger, channel);
                value.setText("1");
                break;
            case WIPE_BTN:
                long now = System.currentTimeMillis();
                if (now >= wipePendingUntil) {
                    wipePendingUntil = now + WIPE_CONFIRM_TIMEOUT_MS;
                    updateButtons();
                    return;
                }
                ChannelDataAccessor.wipeChannelData(trigger);
                editingChannel = null;
                key.setText("");
                key.setEnabled(true);
                value.setText("1");
                break;
            case DONE_BTN:
                mc.thePlayer.closeScreen();
                return;
            case CUSTOM_BTN:
                editingChannel = null;
                key.setText("");
                key.setEnabled(true);
                key.setFocused(true);
                search.setFocused(false);
                value.setFocused(false);
                listFocused = false;
                list.select(null, false);
                value.setText("1");
                updateButtons();
                return;
            default:
                return;
        }
        wipePendingUntil = 0;
        refreshChannels();
        list.select(channelName(), false);
        if (button.id == ADD_BTN) selectChannel(channel);
        updateButtons();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        search.updateCursorCounter();
        key.updateCursorCounter();
        value.updateCursorCounter();
        updateButtons();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        Main.instance().proxy().uploadChannels(trigger);
    }

    @Override
    public void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF777777);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + xSize - 1, guiTop + ySize - 1, 0xFF25282D);
        fontRendererObj.drawString(label("title"), listX, guiTop + 9, 0xFFFFFF);
        fontRendererObj.drawString("?", guiLeft + xSize - 18, guiTop + 9, 0xBBBBBB);
        fontRendererObj.drawString(label("search"), listX, guiTop + 21, 0xBBBBBB);
        search.drawTextBox();
        fontRendererObj.drawString(label("channels", list.size(), configured.size()), listX, listY - 12, 0xBBBBBB);
        drawRect(listX - 1, listY - 1, listX + listWidth + 1, listY + list.height() + 1, 0xFF60646A);
        drawRect(listX, listY, listX + listWidth, listY + list.height(), 0xFF15171B);
        int rowRight = listX + listWidth - ChannelListModel.SCROLLBAR_WIDTH;
        for (int row = 0; row < list.visibleRows() && list.firstRow() + row < list.size(); row++) {
            String channel = list.channel(list.firstRow() + row);
            int y = listY + row * ChannelListModel.ROW_HEIGHT;
            boolean selected = channel.equals(list.selected());
            boolean hovered = channel.equals(list.channelAt(mouseX, mouseY));
            if (selected || hovered) drawRect(listX, y, rowRight, y + ChannelListModel.ROW_HEIGHT,
                    selected ? 0xFF365D80 : 0xFF303740);
            Integer number = configured.get(channel);
            String numberText = number == null ? "\u2014" : number.toString();
            int numberWidth = fontRendererObj.getStringWidth(numberText);
            fontRendererObj.drawString(trim(ChannelLabels.get(channel), listWidth - numberWidth - 26), listX + 5, y + 5,
                    number == null ? 0xDDDDDD : 0xA8E6A0);
            fontRendererObj.drawString(numberText, rowRight - numberWidth - 5, y + 5,
                    number == null ? 0x777777 : 0xA8E6A0);
        }
        if (list.size() == 0) {
            fontRendererObj.drawString(trim(label("empty"), listWidth - 10), listX + 5, listY + 6, 0x999999);
        }
        if (list.canScroll()) {
            int barX = list.scrollbarX();
            int barY = listY + list.thumbTop();
            drawRect(barX, listY, barX + ChannelListModel.SCROLLBAR_WIDTH, listY + list.height(), 0xFF33373D);
            drawRect(barX + 1, barY, barX + ChannelListModel.SCROLLBAR_WIDTH - 1,
                    barY + list.thumbHeight(), list.isDragging() ? 0xFFB7D9F4 : 0xFF8899AA);
        }
        fontRendererObj.drawString(label("key"), listX, editorY, 0xDDDDDD);
        fontRendererObj.drawString(label("value"), value.xPosition, editorY, 0xDDDDDD);
        key.drawTextBox();
        value.drawTextBox();
        String hint = getValue() <= 0 ? label("invalid_value") : label("hint");
        fontRendererObj.drawString(trim(hint, listWidth), listX, editorY + 64, getValue() <= 0 ? 0xFF9999 : 0x999999);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        super.drawScreen(mouseX, mouseY, partialTick);
        if (mouseX >= guiLeft + xSize - 22 && mouseX < guiLeft + xSize - 10
                && mouseY >= guiTop + 5 && mouseY < guiTop + 19) {
            drawHoveringText(fontRendererObj.listFormattedStringToWidth(label("info"), Math.min(280, width - 24)),
                    mouseX, mouseY, fontRendererObj);
        }
        String channel = list.channelAt(mouseX, mouseY);
        if (channel == null && editingChannel != null && mouseX >= key.xPosition && mouseX < key.xPosition + key.width
                && mouseY >= key.yPosition && mouseY < key.yPosition + key.height) channel = editingChannel;
        if (channel != null && !list.isDragging()) {
            drawHoveringText(channelTooltip(channel), mouseX, mouseY, fontRendererObj);
        }
        GuiButton custom = getButtonList().get(CUSTOM_BTN);
        if (mouseX >= custom.xPosition && mouseX < custom.xPosition + custom.width
                && mouseY >= custom.yPosition && mouseY < custom.yPosition + custom.height) {
            drawHoveringText(Collections.singletonList(label("custom")), mouseX, mouseY, fontRendererObj);
        }
    }

    List<String> channelTooltip(String channel) {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(ChannelLabels.get(channel));
        tooltip.add(label("code", channel));
        ChannelDescription description = ChannelDescription.getAll().get(channel);
        if (description != null) {
            int wrapWidth = Math.min(280, width - 24);
            for (Entry<String, String> entry : description.getDescriptions().entrySet()) {
                ModContainer mod = Loader.instance().getIndexedModList().get(entry.getKey());
                tooltip.add(label("channels.from", mod == null ? entry.getKey() : mod.getName()));
                for (String line : I18n.format(entry.getValue()).split("\\\\n|\n")) {
                    tooltip.addAll(fontRendererObj.listFormattedStringToWidth(line, wrapWidth));
                }
            }
        }
        return tooltip;
    }

    public String trim(String text, int maxWidth) {
        if (fontRendererObj.getStringWidth(text) <= maxWidth) return text;
        return fontRendererObj.trimStringToWidth(text, Math.max(0, maxWidth - fontRendererObj.getStringWidth("..."))) + "...";
    }

    /** Accept an indicator without exposing an optional mod's types in the base GUI. */
    public boolean acceptIndicatorDrop(int mouseX, int mouseY, ItemStack draggedStack, int button) {
        if (button != 0 || mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + xSize || mouseY >= guiTop + ySize) return false;
        Collection<Entry<String, Integer>> channels = ChannelDescription.iterate(draggedStack);
        if (channels.isEmpty()) return false;
        Entry<String, Integer> entry = channels.iterator().next();
        ChannelDataAccessor.setChannelData(trigger, entry.getKey(), entry.getValue());
        search.setText("");
        refreshChannels();
        list.select(entry.getKey(), true);
        selectChannel(entry.getKey());
        return true;
    }

    private static String label(String key, Object... args) { return I18n.format(I18N_PREFIX + key, args); }

    @Override
    public int getGuiLeft() { return guiLeft; }
    @Override
    public int getGuiTop() { return guiTop; }
    @Override
    public int getXSize() { return xSize; }
    @Override
    public int getYSize() { return ySize; }
    @Override
    public void addButton(GuiButton button) { getButtonList().add(button); }
    @Override
    public void removeButton(GuiButton button) { getButtonList().remove(button); }
    @Override
    public int getOverlayOffsetX() { return 0; }
    @Override
    public void doActionPerformed(GuiButton button) { actionPerformed(button); }
    public List<GuiButton> getButtonList() { return buttonList; }
}
