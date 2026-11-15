package space.impact.structures.example;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import space.impact.structures.render.BlockPosition;

public final class DysonController extends TileEntity {

    private boolean running;
    private long startTick;

    public BlockPosition getStructureOrigin() {
        return new BlockPosition(xCoord, yCoord, zCoord);
    }

    public boolean isRunning() {
        return running;
    }

    public long getStartTick() {
        return startTick;
    }

    public boolean toggle() {
        if (!running) {
            if (!DysonBlueprint.check(this)) {
                return false;
            }
            startTick = worldObj.getTotalWorldTime();
        }
        running = !running;
        sync();
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            AnimationExample.proxy.update(this);
        } else if (running && worldObj.getTotalWorldTime() % 20 == 0
                && DysonBlueprint.isLoaded(worldObj, getStructureOrigin()) && !DysonBlueprint.check(this)) {
            running = false;
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
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        running = tag.getBoolean("Running");
        startTick = tag.getLong("StartTick");
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
