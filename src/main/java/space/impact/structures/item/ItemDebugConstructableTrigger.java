package space.impact.structures.item;

import static space.impact.structures.ImpactStructures.MOD_ID;

/**
 * Hologram projector that always performs a creative build, whatever the player's game mode.
 */
public class ItemDebugConstructableTrigger extends ItemConstructableTrigger {

    public ItemDebugConstructableTrigger() {
        setUnlocalizedName("impactapi.debugConstructableTrigger");
        setTextureName(MOD_ID + ":itemDebugConstructableTrigger");
    }
}
