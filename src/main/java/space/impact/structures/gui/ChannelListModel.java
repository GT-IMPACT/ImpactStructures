package space.impact.structures.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.Function;

/** Event-driven list state. Rendering only reads this state and never changes the scroll position. */
final class ChannelListModel {
    static final int ROW_HEIGHT = 18;
    static final int SCROLLBAR_WIDTH = 8;

    private final List<String> channels = new ArrayList<>();
    private String query = "";
    private String selected;
    private int x, y, width, visibleRows = 1, firstRow;
    private int dragOffset = -1;

    void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        visibleRows = Math.max(1, height / ROW_HEIGHT);
        scrollTo(firstRow);
        release();
    }

    void setChannels(Collection<String> available, String filter) {
        setChannels(available, filter, Function.identity());
    }

    void setChannels(Collection<String> available, String filter, Function<String, String> label) {
        String nextQuery = filter.trim().toLowerCase(Locale.ROOT);
        channels.clear();
        for (String channel : new TreeSet<>(available)) {
            if (channel.toLowerCase(Locale.ROOT).contains(nextQuery)
                    || label.apply(channel).toLowerCase(Locale.ROOT).contains(nextQuery)) channels.add(channel);
        }
        channels.sort(Comparator.comparing(label, String.CASE_INSENSITIVE_ORDER).thenComparing(Function.identity()));
        if (!channels.contains(selected)) selected = null;
        scrollTo(nextQuery.equals(query) ? firstRow : 0);
        query = nextQuery;
        release();
    }

    int size() { return channels.size(); }
    int firstRow() { return firstRow; }
    int visibleRows() { return visibleRows; }
    int height() { return visibleRows * ROW_HEIGHT; }
    String selected() { return selected; }
    String channel(int index) { return channels.get(index); }
    boolean canScroll() { return size() > visibleRows; }
    int scrollbarX() { return x + width - SCROLLBAR_WIDTH; }

    boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height();
    }

    String channelAt(int mouseX, int mouseY) {
        if (!contains(mouseX, mouseY) || (canScroll() && mouseX >= scrollbarX())) return null;
        int index = firstRow + (mouseY - y) / ROW_HEIGHT;
        return index < size() ? channels.get(index) : null;
    }

    String press(int mouseX, int mouseY) {
        release();
        if (!contains(mouseX, mouseY)) return null;
        if (canScroll() && mouseX >= scrollbarX()) {
            int localY = mouseY - y;
            int top = thumbTop();
            dragOffset = localY >= top && localY < top + thumbHeight() ? localY - top : thumbHeight() / 2;
            drag(mouseY);
            return null;
        }
        String channel = channelAt(mouseX, mouseY);
        if (channel != null) selected = channel;
        return channel;
    }

    void drag(int mouseY) {
        if (dragOffset < 0 || !canScroll()) return;
        int travel = height() - thumbHeight();
        scrollTo((int) Math.round((double) (mouseY - y - dragOffset) * maxFirstRow() / travel));
    }

    void release() { dragOffset = -1; }
    boolean isDragging() { return dragOffset >= 0; }

    boolean wheel(int mouseX, int mouseY, int delta) {
        if (!contains(mouseX, mouseY) || delta == 0) return false;
        scrollTo(firstRow + (delta > 0 ? -3 : 3));
        return true;
    }

    String moveSelection(int offset) {
        if (channels.isEmpty()) return null;
        int index = channels.indexOf(selected);
        if (index < 0) index = firstRow;
        else index = Math.max(0, Math.min(size() - 1, index + offset));
        select(channels.get(index), true);
        return selected;
    }

    void select(String channel, boolean reveal) {
        int index = channels.indexOf(channel);
        selected = index < 0 ? null : channel;
        if (reveal && index >= 0) {
            if (index < firstRow) scrollTo(index);
            else if (index >= firstRow + visibleRows) scrollTo(index - visibleRows + 1);
        }
    }

    int thumbHeight() {
        return canScroll() ? Math.max(12, height() * visibleRows / size()) : height();
    }

    int thumbTop() {
        return canScroll() ? (int) Math.round((double) firstRow * (height() - thumbHeight()) / maxFirstRow()) : 0;
    }

    private int maxFirstRow() { return Math.max(0, size() - visibleRows); }
    private void scrollTo(int row) { firstRow = Math.max(0, Math.min(maxFirstRow(), row)); }
}
