package space.impact.api.hint;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HintPaletteTest {
    @Test
    void preservesSavedWorldPaletteAndTextures() {
        HintColor[] savedPalette = { HintColor.WHITE, HintColor.ORANGE, HintColor.MAGENTA, HintColor.LIGHT_BLUE,
                HintColor.YELLOW, HintColor.LIME, HintColor.PINK, HintColor.GRAY, HintColor.LIGHT_GRAY,
                HintColor.CYAN, HintColor.PURPLE, HintColor.BLUE, HintColor.BROWN, HintColor.GREEN, HintColor.RED,
                HintColor.BLACK };
        assertEquals(16, HintColor.values().length);
        for (int metadata = 0; metadata < savedPalette.length; metadata++) {
            assertEquals(metadata, savedPalette[metadata].getMetadata());
            assertEquals(savedPalette[metadata], HintColor.fromMetadata(metadata));
            assertNotNull(getClass().getResource("/assets/impactapi/textures/blocks/" + (metadata + 1) + ".png"));
        }
        assertThrows(IllegalArgumentException.class, () -> HintColor.fromMetadata(-1));
        assertThrows(IllegalArgumentException.class, () -> HintColor.fromMetadata(16));
    }
}
