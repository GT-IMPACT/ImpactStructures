package space.impact.structures.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.junit.jupiter.api.Test;
import space.impact.structures.animation.RenderTransform;
import space.impact.structures.animation.Vector3;
import space.impact.structures.render.BlockPosition;

class DysonGeometryTest {

    @Test
    void threeCompleteFramesHaveRequestedDiametersAndNoOverlaps() {
        Set<BlockPosition> occupied = new HashSet<>();
        for (int layer = 0; layer < 3; layer++) {
            List<BlockPosition> frame = DysonGeometry.frame(layer);
            int radius = 48 - layer * 12;
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (BlockPosition position : frame) {
                assertTrue(occupied.add(position), "Layers and individual ribs must not duplicate blocks");
                minX = Math.min(minX, position.x);
                minY = Math.min(minY, position.y);
                minZ = Math.min(minZ, position.z);
                maxX = Math.max(maxX, position.x);
                maxY = Math.max(maxY, position.y);
                maxZ = Math.max(maxZ, position.z);
                double dx = position.x + 0.5;
                double dy = position.y + 0.5 - 49;
                double dz = position.z + 0.5 - 50;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                assertTrue(distance <= radius && distance > radius - 1, "Blocks must stay on the spherical surface");
            }
            assertEquals(-radius, minX);
            assertEquals(radius - 1, maxX);
            assertEquals(49 - radius, minY);
            assertEquals(49 + radius - 1, maxY);
            assertEquals(50 - radius, minZ);
            assertEquals(50 + radius - 1, maxZ);
            assertEquals(radius * 2, maxX - minX + 1);
            assertEquals(radius * 2, maxY - minY + 1);
            assertEquals(radius * 2, maxZ - minZ + 1);
        }
    }

    @Test
    void framesHaveOpenPanelsAndJoinedRibsAroundBothHemispheres() {
        for (int layer = 0; layer < 3; layer++) {
            Set<BlockPosition> remaining = new HashSet<>(DysonGeometry.frame(layer));
            int radius = DysonGeometry.radius(layer);
            // Even the smallest cage leaves more than half the surface open.
            assertTrue(remaining.size() < 2 * Math.PI * radius * radius);
            assertFalse(remaining.contains(new BlockPosition(0, 49, 50)));
            Queue<BlockPosition> pending = new ArrayDeque<>();
            BlockPosition first = remaining.iterator().next();
            pending.add(first);
            remaining.remove(first);
            while (!pending.isEmpty()) {
                BlockPosition position = pending.remove();
                // Voxel curves can join at edges or corners as well as faces.
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPosition neighbor = new BlockPosition(position.x + dx, position.y + dy, position.z + dz);
                            if (remaining.remove(neighbor)) {
                                pending.add(neighbor);
                            }
                        }
                    }
                }
            }
            assertTrue(remaining.isEmpty(), "Each layer must be one connected cage");
        }
    }

    @Test
    void outerAndInnerTurnClockwiseAndMiddleCounterclockwiseFromAbove() {
        double[] quarterTurns = {30, 18, 90.0 / 7};
        double[] expectedZ = {51, 49, 51};
        for (int layer = 0; layer < 3; layer++) {
            RenderTransform transform = new RenderTransform();
            DysonGeometry.animation(layer).apply(quarterTurns[layer], transform);
            Vector3 point = transform.transform(new Vector3(1, 49, 50));
            assertEquals(0, point.x, 1e-9);
            assertEquals(49, point.y, 1e-9);
            assertEquals(expectedZ[layer], point.z, 1e-9);
            Vector3 pivot = transform.transform(DysonGeometry.CENTER);
            assertEquals(0, pivot.x, 1e-9);
            assertEquals(49, pivot.y, 1e-9);
            assertEquals(50, pivot.z, 1e-9);
        }
    }

    @Test
    void rotationUsesFractionalTimeWithoutBlockSteps() {
        RenderTransform transform = new RenderTransform();
        DysonGeometry.animation(0).apply(0.025, transform);
        Vector3 point = transform.transform(new Vector3(1, 49, 50));
        assertTrue(point.x < 1 && point.x > 0.999);
        assertTrue(point.z > 50 && point.z < 50.01);
    }
}
