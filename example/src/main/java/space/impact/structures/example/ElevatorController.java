package space.impact.structures.example;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import space.impact.structures.render.BlockPosition;

public final class ElevatorController extends TileEntity {

    private boolean running;
    private long startTick;
    private long stopTick = -1;

    public BlockPosition getStructureOrigin() {
        BlockPosition position = ElevatorBlueprint.CONTROLLER_POSITION;
        return new BlockPosition(xCoord - position.x, yCoord - position.y, zCoord - position.z);
    }

    public boolean isRunning() {
        return running;
    }

    public long getStartTick() {
        return startTick;
    }

    public boolean toggle() {
        if (running) {
            stopTick = ElevatorMotion.dockTick(startTick, worldObj.getTotalWorldTime());
        } else {
            if (!ElevatorBlueprint.check(this)) {
                return false;
            }
            running = true;
            startTick = worldObj.getTotalWorldTime();
            stopTick = -1;
        }
        sync();
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            AnimationExample.proxy.update(this);
            return;
        }
        long tick = worldObj.getTotalWorldTime();
        if (running && ((stopTick >= 0 && tick >= stopTick) || (tick % 20 == 0 && !ElevatorBlueprint.check(this)))) {
            running = false;
            stopTick = -1;
            sync();
        }
    }

    private void sync() {
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void invalidate() {
        AnimationExample.proxy.remove(this);
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        AnimationExample.proxy.remove(this);
        super.onChunkUnload();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Running", running);
        tag.setLong("StartTick", startTick);
        tag.setLong("StopTick", stopTick);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        running = tag.getBoolean("Running");
        startTick = tag.getLong("StartTick");
        stopTick = tag.hasKey("StopTick") ? tag.getLong("StopTick") : -1;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
