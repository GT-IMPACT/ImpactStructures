package space.impact.structures.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import space.impact.structures.hint.HintColor;
import space.impact.structures.hint.HintMarkerKind;

class HintRenderStyleTest {

    @Test
    void serviceMarkersHaveDistinctColorsWithRedErrorsAndBlueAir() {
        Set<Integer> colors = new HashSet<>();
        for (HintMarkerKind kind : HintMarkerKind.values()) {
            assertTrue(colors.add(HintRenderStyle.markerColor(kind)));
        }
        int error = HintRenderStyle.markerColor(HintMarkerKind.ERROR);
        assertTrue((error >> 16 & 255) > (error >> 8 & 255));
        assertTrue((error >> 16 & 255) > (error & 255));
        int air = HintRenderStyle.markerColor(HintMarkerKind.AIR);
        assertTrue((air & 255) > (air >> 16 & 255));
        assertTrue((air & 255) > (air >> 8 & 255));
    }

    @Test
    void fillsStayLessOpaqueThanOrdinaryHintsAcrossAllSettings() {
        for (int opacity = 1; opacity <= 255; opacity++) {
            assertTrue(HintRenderStyle.markerOpacity(opacity) < opacity);
        }
        assertEquals(0, HintRenderStyle.markerOpacity(0));
        assertEquals(48, HintRenderStyle.markerOpacity(192));
    }

    @Test
    void onlyErrorsAreVisibleThroughWorldBlocks() {
        assertFalse(HintRenderStyle.rendersThroughBlocks(null));
        for (HintMarkerKind kind : HintMarkerKind.values()) {
            assertEquals(kind == HintMarkerKind.ERROR, HintRenderStyle.rendersThroughBlocks(kind));
        }
    }

    @Test
    void framePaletteKeepsAllSixteenColorsDistinct() {
        Set<Integer> colors = new HashSet<>();
        for (HintColor color : HintColor.values()) assertTrue(colors.add(color.getRGB()));
    }
}
