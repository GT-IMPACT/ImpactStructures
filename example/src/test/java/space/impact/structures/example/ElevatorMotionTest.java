package space.impact.structures.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ElevatorMotionTest {

    @Test
    void carriageWaitsAtBothEndsAndReturnsExactlyToRealBlocks() {
        assertEquals(0, ElevatorMotion.height(0));
        assertEquals(0, ElevatorMotion.height(4));
        assertEquals(44, ElevatorMotion.height(16));
        assertEquals(44, ElevatorMotion.height(20));
        assertEquals(0, ElevatorMotion.height(32));
        assertTrue(ElevatorMotion.height(4.025) > 0);
        assertTrue(ElevatorMotion.height(4.025) < 1);
        for (double boundary : new double[] {4, 16, 20, 32}) {
            assertEquals(ElevatorMotion.height(boundary - 0.0001),
                    ElevatorMotion.height(boundary + 0.0001), 1e-6);
        }
    }

    @Test
    void stoppingWaitsForDockAndEpochSurvivesJoiningMidCycle() {
        assertEquals(1010, ElevatorMotion.dockTick(1000, 1010));
        assertEquals(1640, ElevatorMotion.dockTick(1000, 1200));
        assertEquals(2280, ElevatorMotion.dockTick(1000, 1900));
        assertEquals(ElevatorMotion.height(10), ElevatorMotion.height(42));
    }
}
