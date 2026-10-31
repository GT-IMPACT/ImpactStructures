package space.impact.structures.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HintFrameGeometryTest {

    @Test
    void solidBordersStayCloseToTheBlockAndLeaveTheFaceCenterOpen() {
        double min = 0.24, max = 0.76, thickness = 0.025;
        for (int face = 0; face < 6; face++) {
            int uAxis = face < 4 ? 0 : 2;
            int vAxis = face < 2 ? 2 : 1;
            int normalAxis = 3 - uAxis - vAxis;
            for (int strip = 0; strip < 4; strip++) {
                for (int triangle = 0; triangle < 2; triangle++) {
                    double centerU = 0, centerV = 0;
                    for (int vertex = triangle * 3; vertex < triangle * 3 + 3; vertex++) {
                        for (int axis = 0; axis < 3; axis++) {
                            double coordinate = HintFrameGeometry.coordinate(face, strip, vertex, axis);
                            assertTrue(coordinate >= min - 1e-9 && coordinate <= max + 1e-9);
                        }
                        assertEquals((face & 1) == 0 ? min : max,
                                HintFrameGeometry.coordinate(face, strip, vertex, normalAxis), 1e-9);
                        centerU += HintFrameGeometry.coordinate(face, strip, vertex, uAxis) / 3;
                        centerV += HintFrameGeometry.coordinate(face, strip, vertex, vAxis) / 3;
                    }
                    assertTrue(centerU <= min + thickness || centerU >= max - thickness
                            || centerV <= min + thickness || centerV >= max - thickness);
                }
            }
        }
    }

    @Test
    void outsideViewsOmitRearFacesAndInsideViewsRemainVisible() {
        for (int face = 0; face < 6; face++) {
            assertEquals(face == 2, HintFrameGeometry.isFaceVisible(face, 0.5, 0.5, -2));
            assertEquals(face == 1 || face == 2 || face == 5, HintFrameGeometry.isFaceVisible(face, 2, 2, -2));
            assertTrue(HintFrameGeometry.isFaceVisible(face, 0.5, 0.5, 0.5));
        }
        assertFalse(HintFrameGeometry.isFaceVisible(3, 0.5, 0.5, -2));
    }
}
