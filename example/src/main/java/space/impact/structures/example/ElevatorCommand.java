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

public final class ElevatorCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "istruct_example";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/istruct_example [x y z]";
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
        int z = MathHelper.floor_double(player.posZ) + 14;
        if (arguments.length == 3) {
            x = parseInt(sender, arguments[0]);
            y = parseInt(sender, arguments[1]);
            z = parseInt(sender, arguments[2]);
        }
        World world = player.worldObj;
        if (y < 1 || y + ElevatorBlueprint.HEIGHT >= world.getHeight()
                || Math.abs((long) x) > 29999900 || Math.abs((long) z) > 29999900) {
            throw new CommandException("Elevator does not fit inside world bounds.");
        }
        // Check the complete moving volume before the first write; never overwrite an existing build.
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                for (int dy = 0; dy < ElevatorBlueprint.HEIGHT; dy++) {
                    if (!world.blockExists(x + dx, y + dy, z + dz)
                            || !world.isAirBlock(x + dx, y + dy, z + dz)) {
                        throw new CommandException("Need a loaded, empty 19 x 65 x 19 area above the ground.");
                    }
                }
            }
        }
        ElevatorBlueprint.place(world, new BlockPosition(x, y, z));
        BlockPosition controller = ElevatorBlueprint.CONTROLLER_POSITION;
        sender.addChatMessage(new ChatComponentText("Elevator built. Right-click controller at "
                + (x + controller.x) + " " + (y + controller.y) + " " + (z + controller.z)
                + " to start/stop. Demo movement is visual; collisions stay at the dock."));
    }
}
