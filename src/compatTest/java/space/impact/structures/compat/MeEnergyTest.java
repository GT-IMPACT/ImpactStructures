package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;

class MeEnergyTest {
    @Test
    void simulationSharesAnEnergyBudgetAcrossItemTypesWithoutConsumingEnergy() {
        AtomicInteger mutations = new AtomicInteger();
        double previous = PowerMultiplier.CONFIG.multiplier;
        PowerMultiplier.CONFIG.multiplier = 2;
        try {
            MeExtractor.SimulatedEnergy budget = new MeExtractor.SimulatedEnergy((amount, action, multiplier) -> {
                if (action == Actionable.MODULATE) mutations.incrementAndGet();
                return multiplier.divide(Math.min(10, multiplier.multiply(amount)));
            });
            assertEquals(5, budget.extractAEPower(8, Actionable.SIMULATE, PowerMultiplier.CONFIG));
            budget.reserve(3);
            assertEquals(2, budget.extractAEPower(8, Actionable.SIMULATE, PowerMultiplier.CONFIG));
            budget.reserve(2);
            assertEquals(0, budget.extractAEPower(8, Actionable.SIMULATE, PowerMultiplier.CONFIG));
            assertEquals(0, mutations.get());
        } finally {
            PowerMultiplier.CONFIG.multiplier = previous;
        }
    }
}
