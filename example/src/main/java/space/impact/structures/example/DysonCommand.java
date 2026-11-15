package space.impact.structures.example;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import space.impact.structures.render.BlockPosition;

public final class DysonCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "istruct_dyson";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/istruct_dyson [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length != 0 && arguments.length != 3) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ) + 3;
        if (arguments.length == 3) {
            x = parseInt(sender, arguments[0]);
            y = parseInt(sender, arguments[1]);
            z = parseInt(sender, arguments[2]);
        }
        World world = player.worldObj;
        if (y < 1 || y + 96 >= world.getHeight()
                || Math.abs((long) x) > 29999800 || Math.abs((long) z) > 29999800) {
            throw new CommandException("Dyson sphere does not fit inside world bounds.");
        }
        BlockPosition origin = new BlockPosition(x, y, z);
        if (!DysonBlueprint.isLoaded(world, origin)) {
            throw new CommandException("Load the full area first (at least 8 chunks render distance recommended).");
        }
        // Reserve the entire swept volume, including block corners between integral rotation angles.
        for (int dx = -49; dx <= 48; dx++) {
            for (int dz = 0; dz <= 98; dz++) {
                for (int dy = 0; dy <= 96; dy++) {
                    if (!world.isAirBlock(x + dx, y + dy, z + dz)) {
                        throw new CommandException("Need an empty 98 x 97 x 99 area above ground, extending along +Z.");
                    }
                }
            }
        }
        DysonBlueprint.place(world, origin);
        sender.addChatMessage(new ChatComponentText("Dyson sphere built: 96 / 72 / 48 blocks, clay frames only. "
                + "Right-click the black clay controller at " + x + " " + y + " " + z + ". "
                + "Outer and inner rotate clockwise from above; middle counterclockwise. Movement is visual."));
    }
}
