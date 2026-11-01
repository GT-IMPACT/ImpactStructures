package space.impact.structures.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class ChannelListModelTest {
    private static List<String> channels() {
        return IntStream.range(0, 40).mapToObj(i -> String.format("channel-%02d", i)).collect(Collectors.toList());
    }

    private static ChannelListModel list() {
        ChannelListModel list = new ChannelListModel();
        list.setBounds(20, 50, 200, 90);
        list.setChannels(channels(), "");
        return list;
    }

    @Test
    void wheelCanLeaveSelectionOffscreenAndClickUsesScrolledRow() {
        ChannelListModel list = list();
        assertEquals("channel-00", list.press(25, 55));
        list.wheel(25, 55, -120);
        list.wheel(25, 55, -120);
        assertEquals(6, list.firstRow());
        assertEquals("channel-00", list.selected());
        // The renderer's reads and an unchanged filter must not scroll back to the selection.
        list.thumbTop();
        list.channelAt(25, 55);
        list.setChannels(channels(), "");
        assertEquals(6, list.firstRow());
        assertEquals("channel-07", list.press(25, 73));
        assertEquals("channel-07", list.selected());
    }

    @Test
    void boundsAndWheelDoNotAffectOtherControlsOrSelectEmptySpace() {
        ChannelListModel list = list();
        assertFalse(list.wheel(19, 55, -120));
        assertFalse(list.wheel(25, 140, -120));
        assertNull(list.press(220, 55));
        assertNull(list.press(25, 49));
        assertEquals(0, list.firstRow());
        list.setChannels(Arrays.asList("one", "two"), "");
        assertNull(list.press(25, 100));
        list.wheel(25, 55, -120);
        assertEquals(0, list.firstRow());
        assertFalse(list.canScroll());
    }

    @Test
    void scrollbarDragsToBothEndsWithoutSelectingAndStopsAfterRelease() {
        ChannelListModel list = list();
        list.select("channel-02", false);
        assertNull(list.press(216, 54));
        assertTrue(list.isDragging());
        list.drag(1000);
        assertEquals(35, list.firstRow());
        assertEquals("channel-39", list.channelAt(25, 139));
        list.drag(-100);
        assertEquals(0, list.firstRow());
        list.drag(1000);
        list.release();
        list.drag(50);
        assertEquals(35, list.firstRow());
        assertEquals("channel-02", list.selected());
    }

    @Test
    void clickingScrollbarTrackPagesToPointerAndFilteringResetsScroll() {
        ChannelListModel list = list();
        list.press(216, 139);
        assertEquals(35, list.firstRow());
        list.setChannels(channels(), " CHANNEL-01 ");
        assertEquals(0, list.firstRow());
        assertEquals(1, list.size());
        assertFalse(list.isDragging());
        assertEquals("channel-01", list.press(25, 55));
        list.setChannels(channels(), "not-found");
        assertNull(list.selected());
        assertNull(list.moveSelection(1));
        assertNull(list.press(25, 55));
    }

    @Test
    void keyboardRevealsSelectionOnlyWhenMovedAndResizeClampsScroll() {
        ChannelListModel list = list();
        list.select("channel-04", true);
        assertEquals("channel-05", list.moveSelection(1));
        assertEquals(1, list.firstRow());
        assertEquals("channel-10", list.moveSelection(5));
        assertEquals(6, list.firstRow());
        list.select("channel-39", true);
        list.setBounds(20, 50, 200, 180);
        assertEquals(30, list.firstRow());
        list.setChannels(Collections.singleton("channel-39"), "");
        assertEquals(0, list.firstRow());
        assertEquals("channel-39", list.selected());
    }
}
