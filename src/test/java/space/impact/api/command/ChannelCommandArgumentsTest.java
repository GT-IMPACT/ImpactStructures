package space.impact.api.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.command.WrongUsageException;
import org.junit.jupiter.api.Test;

class ChannelCommandArgumentsTest {
    @Test
    void preservesSpacesAndNormalizesCaseLikeTheGui() {
        ChannelCommandArguments request = ChannelCommandArguments.parse(new String[] { "set", "Coil", "Tier", "7" });
        assertEquals("coil tier", request.channel);
        assertEquals("set", request.action);
        assertEquals(7, request.value);
    }

    @Test
    void handlesExplicitUnsetAndNonPositiveSetValues() {
        assertEquals("unset", ChannelCommandArguments.parse(new String[] { "unset", "tier" }).action);
        assertEquals("unset", ChannelCommandArguments.parse(new String[] { "set", "tier", "0" }).action);
        assertEquals("unset", ChannelCommandArguments.parse(new String[] { "set", "tier", "-3" }).action);
    }

    @Test
    void rejectsMissingValuesMalformedNumbersAndTrailingWipeArguments() {
        assertThrows(WrongUsageException.class, () -> ChannelCommandArguments.parse(new String[] { "set", "tier" }));
        assertThrows(WrongUsageException.class, () -> ChannelCommandArguments.parse(new String[] { "set", "tier", "2147483648" }));
        assertThrows(WrongUsageException.class, () -> ChannelCommandArguments.parse(new String[] { "set", "tier", "x" }));
        assertThrows(WrongUsageException.class, () -> ChannelCommandArguments.parse(new String[] { "wipe", "tier" }));
        assertThrows(WrongUsageException.class, () -> ChannelCommandArguments.parse(new String[] { "unknown" }));
    }
}
