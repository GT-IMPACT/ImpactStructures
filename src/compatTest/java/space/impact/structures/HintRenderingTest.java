package space.impact.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

import space.impact.structures.client.HintFrameTexture;
import space.impact.structures.hint.HintColor;
import space.impact.structures.hint.HintMarkerKind;

class HintRenderingTest {

    private final Class<?> hintType = Class.forName("space.impact.structures.ClientProxy$HintParticleInfo");
    private Object previousLoader;

    HintRenderingTest() throws Exception {}

    @BeforeEach
    void prepareForgeLoader() throws Exception {
        Field instance = field(Loader.class, "instance");
        previousLoader = instance.get(null);
        Loader loader = mock(Loader.class);
        when(loader.activeModContainer()).thenReturn(mock(ModContainer.class));
        instance.set(null, loader);
    }

    @AfterEach
    void clearTextureTracking() throws Exception {
        usedTextures().clear();
        field(Loader.class, "instance").set(null, previousLoader);
    }

    @Test
    void markerFillUsesOnlyWhiteAtlasTexelsAndNeverQueriesWorldLighting() throws Exception {
        World world = mock(World.class);
        HintFrameTexture icon = HintFrameTexture.INSTANCE;
        icon.load(null, null);
        icon.initSprite(256, 256, 32, 48, false);
        usedTextures().add(icon);
        Object hint = hint(world, new IIcon[6]);
        field(hintType, "markerKind").set(hint, HintMarkerKind.ERROR);
        Tessellator tessellator = mock(Tessellator.class);

        draw(hint, tessellator, true);

        ArgumentCaptor<Double> u = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> v = ArgumentCaptor.forClass(Double.class);
        verify(tessellator, times(36)).addVertexWithUV(anyDouble(), anyDouble(), anyDouble(), u.capture(), v.capture());
        for (double value : u.getAllValues()) assertTrue(value >= icon.getMinU() && value <= icon.getMaxU());
        for (double value : v.getAllValues()) assertTrue(value >= icon.getMinV() && value <= icon.getMaxV());
        verify(tessellator).setBrightness(0x00F000F0);
        verifyNoInteractions(world);
    }

    @Test
    void texturedFacesAndColorFrameShareLightingUntilTheNextTick() throws Exception {
        World world = mock(World.class);
        when(world.blockExists(1, 0, 3)).thenReturn(true);
        when(world.getLightBrightnessForSkyBlocks(1, 2, 3, 0)).thenReturn(123, 456);
        IIcon[] icons = new IIcon[6];
        Arrays.fill(icons, HintFrameTexture.INSTANCE);
        usedTextures().add(HintFrameTexture.INSTANCE);
        Object hint = hint(world, icons);
        field(hintType, "frameColor").set(hint, HintColor.BLUE);
        Tessellator tessellator = mock(Tessellator.class);
        Method frame = hintType.getDeclaredMethod("drawColorFrame", Tessellator.class,
                double.class, double.class, double.class, int.class, int.class, int.class);
        frame.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            draw(hint, tessellator, false);
            frame.invoke(hint, tessellator, 5D, 6D, 7D, 0, 0, 0);
        }
        verify(world).getLightBrightnessForSkyBlocks(1, 2, 3, 0);
        verify(tessellator, times(6)).setBrightness(123);

        // Advance the tick revision without requiring a live Minecraft/GL context.
        Field revision = field(ClientProxy.class, "lightingRevision");
        revision.setLong(null, revision.getLong(null) + 1);
        draw(hint, tessellator, false);
        verify(world, times(2)).getLightBrightnessForSkyBlocks(1, 2, 3, 0);
        verify(tessellator).setBrightness(456);
    }

    @Test
    void unloadedChunksAreNotQueriedForLight() throws Exception {
        World world = mock(World.class);
        Object hint = hint(world, new IIcon[6]);
        Method brightness = hintType.getDeclaredMethod("getBrightness");
        brightness.setAccessible(true);
        assertEquals(0, brightness.invoke(hint));
        verify(world).blockExists(1, 0, 3);
        org.mockito.Mockito.verifyNoMoreInteractions(world);
    }

    private Object hint(World world, IIcon[] icons) throws Exception {
        Constructor<?> constructor = hintType.getDeclaredConstructor(World.class,
                int.class, int.class, int.class, IIcon[].class, short[].class);
        constructor.setAccessible(true);
        return constructor.newInstance(world, 1, 2, 3, icons, new short[] {255, 255, 255, 255});
    }

    private void draw(Object hint, Tessellator tessellator, boolean marker) throws Exception {
        Method draw = hintType.getDeclaredMethod("draw", Tessellator.class,
                double.class, double.class, double.class, int.class, int.class, int.class, boolean.class);
        draw.setAccessible(true);
        draw.invoke(hint, tessellator, 5D, 6D, 7D, 0, 0, 0, marker);
    }

    @SuppressWarnings("unchecked")
    private Set<IIcon> usedTextures() throws Exception {
        return (Set<IIcon>) field(ClientProxy.class, "usedTextures").get(null);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
