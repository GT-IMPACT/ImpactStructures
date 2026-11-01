package space.impact.structures.client;

import net.minecraft.util.IIcon;

import com.gtnewhorizons.angelica.utils.AnimationsRenderUtils;

/** Loaded only on clients with Angelica; holograms live outside its chunk visibility tracking. */
public final class AngelicaCompat {

    private AngelicaCompat() {}

    public static void markTextureUsed(IIcon icon) {
        AnimationsRenderUtils.markBlockTextureForUpdate(icon);
    }
}
