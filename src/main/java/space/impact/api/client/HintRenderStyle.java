package space.impact.api.client;

import space.impact.api.hint.HintMarkerKind;

/** Shared geometry and appearance of client-side holograms. */
public final class HintRenderStyle {

    public static final double BLOCK_SIZE = 1.0;
    public static final double FRAME_MARGIN = 0.002;

    private HintRenderStyle() {}

    public static int markerColor(HintMarkerKind kind) {
        switch (kind) {
            case ERROR: return 0xFF4545;
            case AIR: return 0x75CFFF;
            case NOT_AIR: return 0xFFC45C;
            case GENERIC: return 0xC8D0DC;
            default: throw new IllegalArgumentException("Unknown marker kind: " + kind);
        }
    }

    public static int markerOpacity(int hintOpacity) {
        return Math.max(0, Math.min(255, hintOpacity)) / 4;
    }

    public static boolean rendersThroughBlocks(HintMarkerKind kind) {
        return kind == HintMarkerKind.ERROR;
    }
}
