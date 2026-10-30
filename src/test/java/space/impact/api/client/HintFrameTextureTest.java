package space.impact.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HintFrameTextureTest {

    @Test
    void whiteAtlasEntryKeepsEveryMipLevelNeutralAndOpaque() {
        HintFrameTexture texture = HintFrameTexture.INSTANCE;
        assertTrue(texture.hasCustomLoader(null, null));
        assertFalse(texture.load(null, null));
        assertEquals(16, texture.getIconWidth());
        assertEquals(16, texture.getIconHeight());
        assertEquals(1, texture.getFrameCount());
        int[][] levels = texture.getFrameTextureData(0);
        assertEquals(5, levels.length);
        for (int level = 0; level < levels.length; level++) {
            int size = 16 >> level;
            assertEquals(size * size, levels[level].length);
            for (int pixel : levels[level]) assertEquals(0xFFFFFFFF, pixel);
        }
        assertFalse(texture.load(null, null));
        assertEquals(1, texture.getFrameCount());
    }
}
