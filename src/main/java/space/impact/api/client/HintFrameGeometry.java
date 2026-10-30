package space.impact.api.client;

/** Cached single-color face borders; no screen-space outline or per-hint vertex allocations. */
public final class HintFrameGeometry {

    private static final double MIN = (1 - HintRenderStyle.COLOR_FRAME_SIZE) / 2;
    private static final double MAX = 1 - MIN;
    private static final double[] VERTICES = createVertices();

    private HintFrameGeometry() {}

    public static boolean isFaceVisible(int face, double eyeX, double eyeY, double eyeZ) {
        if (eyeX >= MIN && eyeX <= MAX && eyeY >= MIN && eyeY <= MAX && eyeZ >= MIN && eyeZ <= MAX) return true;
        switch (face) {
            case 0: return eyeY <= MIN;
            case 1: return eyeY >= MAX;
            case 2: return eyeZ <= MIN;
            case 3: return eyeZ >= MAX;
            case 4: return eyeX <= MIN;
            case 5: return eyeX >= MAX;
            default: throw new IllegalArgumentException("Unknown face: " + face);
        }
    }

    public static double coordinate(int face, int strip, int vertex, int axis) {
        return VERTICES[((face * 4 + strip) * 6 + vertex) * 3 + axis];
    }

    private static double[] createVertices() {
        double[] vertices = new double[6 * 4 * 6 * 3];
        double thickness = HintRenderStyle.COLOR_FRAME_THICKNESS;
        int index = 0;
        for (int face = 0; face < 6; face++) {
            int uAxis = face < 4 ? 0 : 2;
            int vAxis = face < 2 ? 2 : 1;
            int normalAxis = 3 - uAxis - vAxis;
            double plane = (face & 1) == 0 ? MIN : MAX;
            for (int strip = 0; strip < 4; strip++) {
                double minU = strip == 3 ? MAX - thickness : MIN;
                double maxU = strip == 2 ? MIN + thickness : MAX;
                double minV = strip == 1 ? MAX - thickness : strip >= 2 ? MIN + thickness : MIN;
                double maxV = strip == 0 ? MIN + thickness : strip >= 2 ? MAX - thickness : MAX;
                for (int vertex = 0; vertex < 6; vertex++) {
                    int corner = vertex < 3 ? vertex : vertex == 3 ? 0 : vertex - 2;
                    double u = corner == 1 || corner == 2 ? maxU : minU;
                    double v = corner >= 2 ? maxV : minV;
                    for (int axis = 0; axis < 3; axis++) {
                        vertices[index++] = axis == normalAxis ? plane : axis == uAxis ? u : v;
                    }
                }
            }
        }
        return vertices;
    }
}
