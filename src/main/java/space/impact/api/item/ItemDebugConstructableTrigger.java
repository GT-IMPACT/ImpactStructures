package space.impact.api.item;

import static space.impact.api.ImpactStructures.MOD_ID;

/**
 * Hologram projector that always performs a creative build, whatever the player's game mode.
 */
public class ItemDebugConstructableTrigger extends ItemConstructableTrigger {

    public ItemDebugConstructableTrigger() {
        setUnlocalizedName("impactapi.debugConstructableTrigger");
        setTextureName(MOD_ID + ":itemDebugConstructableTrigger");
    }
}
