package space.impact.structures.client;

import java.util.Arrays;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/** White atlas entry lets colored frames share the block texture batch and its transparency ordering. */
public final class HintFrameTexture extends TextureAtlasSprite {

    public static final HintFrameTexture INSTANCE = new HintFrameTexture();

    private HintFrameTexture() {
        super("impactapi:hint_frame");
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        setIconWidth(16);
        setIconHeight(16);
        int[][] pixels = new int[5][];
        for (int level = 0; level < pixels.length; level++) {
            int size = 16 >> level;
            pixels[level] = new int[size * size];
            Arrays.fill(pixels[level], 0xFFFFFFFF);
        }
        framesTextureData.clear();
        framesTextureData.add(pixels);
        // TextureMap stitches a custom sprite when its loader returns false.
        return false;
    }
}
