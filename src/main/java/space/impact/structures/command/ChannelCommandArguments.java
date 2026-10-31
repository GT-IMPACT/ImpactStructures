package space.impact.structures.command;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import net.minecraft.command.WrongUsageException;

/** Shared parsing keeps CLI channel names consistent with the channel editor. */
final class ChannelCommandArguments {
    final String action;
    final String channel;
    final int value;

    private ChannelCommandArguments(String action, String channel, int value) {
        this.action = action;
        this.channel = channel;
        this.value = value;
    }

    static ChannelCommandArguments parse(String[] args) {
        if (args.length == 0) throw usage();
        String action = args[0].toLowerCase(Locale.ROOT);
        if ("wipe".equals(action) || "getall".equals(action)) {
            if (args.length != 1) throw usage();
            return new ChannelCommandArguments(action, null, 0);
        }
        boolean setting = "set".equals(action);
        if (!setting && !"get".equals(action) && !"unset".equals(action)) throw usage();
        if (args.length < (setting ? 3 : 2)) throw usage();
        int end = args.length - (setting ? 1 : 0);
        String channel = Arrays.stream(args, 1, end).collect(Collectors.joining(" "))
                .trim().toLowerCase(Locale.ROOT);
        if (channel.isEmpty()) throw usage();
        int value = 0;
        if (setting) {
            try {
                value = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException e) {
                throw new WrongUsageException("impactapi.command.invalid_value", args[args.length - 1]);
            }
            if (value <= 0) action = "unset";
        }
        return new ChannelCommandArguments(action, channel, value);
    }

    private static WrongUsageException usage() {
        return new WrongUsageException("impactapi.command.channel.usage");
    }
}
