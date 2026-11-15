package space.impact.structures.animation;

import java.nio.DoubleBuffer;
import java.util.Arrays;

/** Reusable column-major affine matrix. Operations post-multiply, as in OpenGL. No client dependencies. */
public final class RenderTransform {

    private final double[] matrix = new double[16];
    private final double[] operation = new double[16];
    private final double[] product = new double[16];

    public RenderTransform() {
        identity();
    }

    public RenderTransform identity() {
        reset(matrix);
        return this;
    }

    public RenderTransform translate(double x, double y, double z) {
        reset(operation);
        operation[12] = x;
        operation[13] = y;
        operation[14] = z;
        multiply();
        return this;
    }

    public RenderTransform scale(double x, double y, double z) {
        reset(operation);
        operation[0] = x;
        operation[5] = y;
        operation[10] = z;
        multiply();
        return this;
    }

    public RenderTransform rotate(double degrees, Vector3 axis) {
        double length = Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);
        if (!Double.isFinite(degrees) || !Double.isFinite(length) || length == 0) {
            throw new IllegalArgumentException("Rotation requires a finite angle and nonzero axis");
        }
        double x = axis.x / length;
        double y = axis.y / length;
        double z = axis.z / length;
        double c = Math.cos(Math.toRadians(degrees));
        double s = Math.sin(Math.toRadians(degrees));
        double t = 1 - c;
        reset(operation);
        operation[0] = t * x * x + c;
        operation[1] = t * x * y + s * z;
        operation[2] = t * x * z - s * y;
        operation[4] = t * x * y - s * z;
        operation[5] = t * y * y + c;
        operation[6] = t * y * z + s * x;
        operation[8] = t * x * z + s * y;
        operation[9] = t * y * z - s * x;
        operation[10] = t * z * z + c;
        multiply();
        return this;
    }

    public RenderTransform rotateAround(double degrees, Vector3 axis, Vector3 pivot) {
        return translate(pivot.x, pivot.y, pivot.z).rotate(degrees, axis)
                .translate(-pivot.x, -pivot.y, -pivot.z);
    }

    public Vector3 transform(Vector3 point) {
        return new Vector3(
                matrix[0] * point.x + matrix[4] * point.y + matrix[8] * point.z + matrix[12],
                matrix[1] * point.x + matrix[5] * point.y + matrix[9] * point.z + matrix[13],
                matrix[2] * point.x + matrix[6] * point.y + matrix[10] * point.z + matrix[14]);
    }

    public void writeTo(DoubleBuffer buffer) {
        buffer.clear();
        buffer.put(matrix);
        buffer.flip();
    }

    private static void reset(double[] values) {
        Arrays.fill(values, 0);
        values[0] = 1;
        values[5] = 1;
        values[10] = 1;
        values[15] = 1;
    }

    private void multiply() {
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                double value = 0;
                for (int index = 0; index < 4; index++) {
                    value += matrix[index * 4 + row] * operation[column * 4 + index];
                }
                product[column * 4 + row] = value;
            }
        }
        System.arraycopy(product, 0, matrix, 0, 16);
    }
}
