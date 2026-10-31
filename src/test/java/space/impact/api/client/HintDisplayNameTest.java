package space.impact.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import space.impact.api.hint.HintColor;
import space.impact.api.hint.HintMarkerKind;

class HintDisplayNameTest {

    private Field localeField;
    private Object previousLocale;

    @BeforeEach
    void installHeadlessTranslations() throws Exception {
        localeField = I18n.class.getDeclaredField("i18nLocale");
        localeField.setAccessible(true);
        previousLocale = localeField.get(null);
        localeField.set(null, new Locale() {
            @Override
            public String formatMessage(String key, Object... arguments) {
                if ("impactapi.hint.error".equals(key)) return arguments[0] + " - Error";
                if ("impactapi.hint.unnamed".equals(key)) return "Structure hint";
                return key;
            }
        });
    }

    @AfterEach
    void restoreTranslations() throws Exception {
        localeField.set(null, previousLocale);
    }

    @Test
    void customTexturesUseMetadataAndNbtFromAnIndependentItemSnapshot() throws Exception {
        Item item = new Item() {
            @Override
            public String getItemStackDisplayName(ItemStack stack) {
                return "Frame " + stack.getItemDamage() + " " + stack.getTagCompound().getString("material");
            }
        };
        ItemStack source = new ItemStack(item, 32, 7);
        source.setTagCompound(new NBTTagCompound());
        source.getTagCompound().setString("material", "Steel");
        Object hint = hint(null, source);

        source.setItemDamage(2);
        source.getTagCompound().setString("material", "Copper");
        source.stackSize = 1;

        assertEquals("Frame 7 Steel", displayName(hint));
        ItemStack snapshot = (ItemStack) field(hint, "displayStack").get(hint);
        assertNotSame(source, snapshot);
        assertSame(item, snapshot.getItem());
        assertEquals(32, snapshot.stackSize);
        assertEquals(1, source.stackSize);
        assertEquals("Copper", source.getTagCompound().getString("material"));
    }

    @Test
    void customItemNameSurvivesAColoredFrameAndAnErrorMarker() throws Exception {
        ItemStack source = new ItemStack(new Item());
        source.setStackDisplayName("Titanium frame");
        Object hint = hint(null, source);
        field(hint, "frameColor").set(hint, HintColor.BLUE);
        assertEquals("Titanium frame", displayName(hint));

        field(hint, "markerKind").set(hint, HintMarkerKind.ERROR);
        assertEquals("Titanium frame - Error", displayName(hint));
        assertEquals(HintColor.BLUE, field(hint, "frameColor").get(hint));
    }

    @Test
    void omittedItemKeepsGenericLabelAndExistingBlockFallback() throws Exception {
        assertEquals("Structure hint", displayName(hint(null, null)));
        Block block = new Block(Material.rock) {
            @Override
            public String getLocalizedName() { return "Existing casing"; }
        };
        assertEquals("Existing casing", displayName(hint(block, null)));
    }

    private static Object hint(Block block, ItemStack stack) throws Exception {
        Class<?> type = Class.forName("space.impact.api.ClientProxy$HintParticleInfo");
        Constructor<?> constructor = type.getDeclaredConstructor(World.class, int.class, int.class, int.class,
                IIcon[].class, short[].class, Block.class, int.class, ItemStack.class);
        constructor.setAccessible(true);
        IIcon[] icons = new IIcon[6];
        icons[0] = new TextureAtlasSprite("test:frame") {};
        return constructor.newInstance(null, 1, 2, 3, icons, new short[] { 80, 120, 160, 255 }, block, 0, stack);
    }

    private static String displayName(Object hint) throws Exception {
        Method method = hint.getClass().getDeclaredMethod("getDisplayName");
        method.setAccessible(true);
        return (String) method.invoke(hint);
    }

    private static Field field(Object hint, String name) throws Exception {
        Field field = hint.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
