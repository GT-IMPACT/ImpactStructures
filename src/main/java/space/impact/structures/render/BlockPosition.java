package space.impact.structures.render;

public final class BlockPosition {

    public final int x;
    public final int y;
    public final int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BlockPosition)) {
            return false;
        }
        BlockPosition position = (BlockPosition) other;
        return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }

    /** Minecraft's world-coordinate range fits into 26 + 12 + 26 bits. */
    public static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | (y & 0xFFFL);
    }
}
