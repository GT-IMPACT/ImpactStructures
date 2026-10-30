package space.impact.api.hint;

/** Stable saved-world metadata of IMPACT's sixteen colored hints. */
public enum HintColor {
    WHITE(0, 0xCFD5D6), ORANGE(1, 0xE16100), MAGENTA(2, 0xA9309F), LIGHT_BLUE(3, 0x2389C7),
    YELLOW(4, 0xF1AF15), LIME(5, 0x5EA918), PINK(6, 0xD6658F), GRAY(7, 0x373A3E),
    LIGHT_GRAY(8, 0x7D7D73), CYAN(9, 0x157788), PURPLE(10, 0x65209D), BLUE(11, 0x2C2E8F),
    BROWN(12, 0x613C20), GREEN(13, 0x495B24), RED(14, 0x8E2020), BLACK(15, 0x090B10);

    private final int metadata;
    private final int rgb;

    HintColor(int metadata, int rgb) {
        this.metadata = metadata;
        this.rgb = rgb;
    }

    public int getMetadata() { return metadata; }

    public int getRGB() { return rgb; }

    public static HintColor fromMetadata(int metadata) {
        for (HintColor color : values()) if (color.metadata == metadata) return color;
        throw new IllegalArgumentException("Hint metadata must be between 0 and 15: " + metadata);
    }
}
