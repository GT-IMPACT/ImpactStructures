package space.impact.api.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import space.impact.api.alignment.constructable.ChannelDataAccessor;
import space.impact.api.item.ItemConstructableTrigger;

public class CommandConfigureChannels extends CommandBase {
    @Override
    public String getCommandName() { return "istruct_channel"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "impactapi.command.channel.usage"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1
                ? getListOfStringsMatchingLastWord(args, "get", "set", "unset", "wipe", "getall") : null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ChannelCommandArguments request = ChannelCommandArguments.parse(args);
        if (!(sender instanceof EntityPlayerMP)) throw new WrongUsageException("impactapi.command.player_only");
        EntityPlayerMP player = (EntityPlayerMP) sender;
        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemConstructableTrigger)) {
            throw new WrongUsageException("impactapi.command.hold_projector");
        }
        switch (request.action) {
            case "getall":
                if (!ChannelDataAccessor.hasSubChannel(heldItem)) {
                    sender.addChatMessage(new ChatComponentTranslation("impactapi.command.no_channels"));
                } else {
                    sender.addChatMessage(new ChatComponentTranslation(
                            "item.impactapi.constructableTrigger.desc.lshift.0",
                            ChannelDataAccessor.countChannelData(heldItem)));
                    ChannelDataAccessor.iterateChannelData(heldItem)
                            .sorted(java.util.Map.Entry.comparingByKey())
                            .map(entry -> new ChatComponentText(entry.getKey() + ": " + entry.getValue()))
                            .forEach(player::addChatMessage);
                }
                return;
            case "get":
                sender.addChatMessage(new ChatComponentTranslation(
                        ChannelDataAccessor.hasSubChannel(heldItem, request.channel)
                                ? "impactapi.command.channel_value" : "impactapi.command.channel_unset",
                        request.channel, ChannelDataAccessor.getChannelData(heldItem, request.channel)));
                return;
            case "wipe":
                ChannelDataAccessor.wipeChannelData(heldItem);
                sender.addChatMessage(new ChatComponentTranslation("impactapi.pickchannel.cleared"));
                break;
            case "unset":
                ChannelDataAccessor.unsetChannelData(heldItem, request.channel);
                sender.addChatMessage(new ChatComponentTranslation("impactapi.command.channel_unset", request.channel));
                break;
            case "set":
                ChannelDataAccessor.setChannelData(heldItem, request.channel, request.value);
                sender.addChatMessage(new ChatComponentTranslation(
                        "impactapi.command.channel_value", request.channel, request.value));
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
        player.inventoryContainer.detectAndSendChanges();
    }
}
