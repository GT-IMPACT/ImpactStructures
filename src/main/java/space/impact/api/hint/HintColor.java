package space.impact.api.hint;

/** Stable saved-world metadata of IMPACT's sixteen colored hints. */
public enum HintColor {
    WHITE(0), ORANGE(1), MAGENTA(2), LIGHT_BLUE(3), YELLOW(4), LIME(5), PINK(6), GRAY(7),
    LIGHT_GRAY(8), CYAN(9), PURPLE(10), BLUE(11), BROWN(12), GREEN(13), RED(14), BLACK(15);

    private final int metadata;

    HintColor(int metadata) { this.metadata = metadata; }

    public int getMetadata() { return metadata; }

    public static HintColor fromMetadata(int metadata) {
        for (HintColor color : values()) if (color.metadata == metadata) return color;
        throw new IllegalArgumentException("Hint metadata must be between 0 and 15: " + metadata);
    }
}
