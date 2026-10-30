package space.impact.api.net;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import space.impact.api.Main;
import space.impact.api.alignment.IAlignment;
import space.impact.api.alignment.IAlignmentProvider;
import space.impact.api.alignment.enumerable.ExtendedFacing;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public abstract class AlignmentMessage implements IMessage {

    int mPosX;
    int mPosY;
    int mPosZ;
    int mPosD;
    int mAlign;

    public AlignmentMessage() {}

    @Override
    public void fromBytes(ByteBuf pBuffer) {
        NBTTagCompound tTag = ByteBufUtils.readTag(pBuffer);
        if (tTag == null) throw new IllegalArgumentException("Missing alignment data");
        mPosX = tTag.getInteger("posx");
        mPosY = tTag.getInteger("posy");
        mPosZ = tTag.getInteger("posz");
        mPosD = tTag.getInteger("posd");
        mAlign = tTag.getInteger("rotf");
        if (mPosY < 0 || mPosY > 255 || mAlign < 0 || mAlign >= ExtendedFacing.STATES_COUNT) {
            throw new IllegalArgumentException("Invalid alignment data");
        }
    }

    @Override
    public void toBytes(ByteBuf pBuffer) {
        NBTTagCompound tFXTag = new NBTTagCompound();
        tFXTag.setInteger("posx", mPosX);
        tFXTag.setInteger("posy", mPosY);
        tFXTag.setInteger("posz", mPosZ);
        tFXTag.setInteger("posd", mPosD);
        tFXTag.setInteger("rotf", mAlign);

        ByteBufUtils.writeTag(pBuffer, tFXTag);
    }

    private AlignmentMessage(IAlignmentProvider provider) {
        if (!(provider instanceof TileEntity)) throw new IllegalArgumentException("Provider must be a TileEntity");
        IAlignment alignment = provider.getAlignment();
        if (alignment == null) throw new IllegalArgumentException("Passed in provider does not provide an alignment!");
        TileEntity base = (TileEntity) provider;
        mPosX = base.xCoord;
        mPosY = base.yCoord;
        mPosZ = base.zCoord;
        mPosD = base.getWorldObj().provider.dimensionId;
        mAlign = alignment.getExtendedFacing().getIndex();
    }

    private AlignmentMessage(World world, int x, int y, int z, IAlignment front) {
        mPosX = x;
        mPosY = y;
        mPosZ = z;
        mPosD = world.provider.dimensionId;
        mAlign = front.getExtendedFacing().getIndex();
    }

    public static class AlignmentQuery extends AlignmentMessage {

        public AlignmentQuery() {}

        public AlignmentQuery(IAlignmentProvider provider) {
            super(provider);
        }

        public AlignmentQuery(World world, int x, int y, int z, IAlignment front) {
            super(world, x, y, z, front);
        }
    }

    public static class AlignmentData extends AlignmentMessage {

        public AlignmentData() {}

        private AlignmentData(AlignmentQuery query) {
            mPosX = query.mPosX;
            mPosY = query.mPosY;
            mPosZ = query.mPosZ;
            mPosD = query.mPosD;
            mAlign = query.mAlign;
        }

        public AlignmentData(IAlignmentProvider provider) {
            super(provider);
        }

        public AlignmentData(World world, int x, int y, int z, IAlignment front) {
            super(world, x, y, z, front);
        }
    }

    public static class ClientHandler implements IMessageHandler<AlignmentData, IMessage> {

        @Override
        public IMessage onMessage(AlignmentData pMessage, MessageContext pCtx) {
            Main.instance().proxy().enqueueClientTask(() -> apply(pMessage));
            return null;
        }

        private void apply(AlignmentData pMessage) {
            EntityPlayer player = Main.getCurrentPlayer();
            if (player != null && player.worldObj.provider.dimensionId == pMessage.mPosD) {
                if (!player.worldObj.blockExists(pMessage.mPosX, pMessage.mPosY, pMessage.mPosZ)) return;
                TileEntity te = player.worldObj
                        .getTileEntity(pMessage.mPosX, pMessage.mPosY, pMessage.mPosZ);
                if (te instanceof IAlignmentProvider) {
                    IAlignment alignment = ((IAlignmentProvider) te).getAlignment();
                    if (alignment != null) {
                        alignment.setExtendedFacing(ExtendedFacing.byIndex(pMessage.mAlign));
                    }
                }
            }
        }
    }

    public static class ServerHandler implements IMessageHandler<AlignmentQuery, AlignmentData> {

        @Override
        public AlignmentData onMessage(AlignmentQuery pMessage, MessageContext pCtx) {
            EntityPlayerMP player = pCtx.getServerHandler().playerEntity;
            ServerTasks.enqueue(() -> {
                if (player.dimension != pMessage.mPosD || player.getDistanceSq(
                        pMessage.mPosX + 0.5, pMessage.mPosY + 0.5, pMessage.mPosZ + 0.5) > 128 * 128) return;
                AlignmentData reply = answer(pMessage);
                if (reply != null) Main.sendToPlayer(player, reply);
            });
            return null;
        }

        private AlignmentData answer(AlignmentQuery pMessage) {
            World world = DimensionManager.getWorld(pMessage.mPosD);
            if (world != null && world.blockExists(pMessage.mPosX, pMessage.mPosY, pMessage.mPosZ)) {
                TileEntity te = world.getTileEntity(pMessage.mPosX, pMessage.mPosY, pMessage.mPosZ);
                if (te instanceof IAlignmentProvider) {
                    IAlignment alignment = ((IAlignmentProvider) te).getAlignment();
                    if (alignment == null) return null;
                    pMessage.mAlign = alignment.getExtendedFacing().getIndex();
                    return new AlignmentData(pMessage);
                }
            }
            return null;
        }
    }
}
