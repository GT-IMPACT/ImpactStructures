package space.impact.structures.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import space.impact.structures.SortedRegistry;

public class CommandRegistryDebug extends CommandBase {

    @Override
    public String getCommandName() {
        return "istruct_registry_debug";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/istruct_registry_debug <registry name>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        if (p_71516_2_.length == 1) return getListOfStringsMatchingLastWord(
                p_71516_2_,
                SortedRegistry.getRegistryNames().toArray(new String[0]));
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) throw new WrongUsageException(getCommandUsage(sender));

        if (!(sender instanceof EntityPlayerMP)) throw new WrongUsageException("impactapi.command.player_only");
        EntityPlayerMP player = (EntityPlayerMP) sender;

        SortedRegistry<?> registry = SortedRegistry.getRegistry(args[0]);
        if (registry == null) throw new WrongUsageException("impactapi.command.registry_not_found", args[0]);

        player.addChatMessage(new ChatComponentTranslation("impactapi.command.registry_order"));
        registry.getPlayerOrderingKeys(player).map(ChatComponentText::new).forEach(player::addChatMessage);
    }
}
