package space.impact.structures.compat;

import baubles.api.BaublesApi;
import space.impact.structures.util.InventoryUtility;

public final class BaublesIntegration {
    private BaublesIntegration() {}

    public static void register() {
        InventoryUtility.registerInventoryProvider("5001-baubles", player -> BaublesApi.getBaubles(player));
    }
}
