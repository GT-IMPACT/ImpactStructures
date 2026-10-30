package space.impact.api.alignment.constructable;

import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import space.impact.api.ConfigurationHandler;
import space.impact.api.Main;
import space.impact.api.ImpactAPI;
import space.impact.api.alignment.IAlignment;
import space.impact.api.alignment.IAlignmentProvider;
import space.impact.api.alignment.enumerable.ExtendedFacing;
import space.impact.api.item.ItemDebugConstructableTrigger;
import space.impact.api.structure.IItemSource;
import space.impact.api.structure.ISurvivalBuildEnvironment;

public class ConstructableUtility {

    private static final WeakHashMap<EntityPlayerMP, Long> lastUse = new WeakHashMap<>();
    private static long clientSideLastUse = 0;

    private ConstructableUtility() {}

    public static boolean handle(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ,
            int aSide) {
        ImpactAPI.startHinting(aWorld);
        try {
            return handle0(aStack, aPlayer, aWorld, aX, aY, aZ, aSide);
        } finally {
            ImpactAPI.endHinting(aWorld);
        }
    }

    private static boolean handle0(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ,
            int aSide) {
        TileEntity tTileEntity = aWorld.getTileEntity(aX, aY, aZ);
        if (tTileEntity == null || aPlayer instanceof FakePlayer) {
            return aPlayer instanceof EntityPlayerMP;
        }
        if (aPlayer instanceof EntityPlayerMP) {
            // not sneaking - client side will generate hologram. nothing to do on server side
            if (!aPlayer.isSneaking()) return true;

            long timePast = System.currentTimeMillis() - getLastUseMilis(aPlayer);
            if (timePast < ConfigurationHandler.INSTANCE.getAutoPlaceInterval()) {
                aPlayer.addChatComponentMessage(
                        new ChatComponentTranslation(
                                "item.impactapi.constructableTrigger.too_fast",
                                ConfigurationHandler.INSTANCE.getAutoPlaceInterval() - timePast));
                return true;
            }
        } else if (!Main.isCurrentPlayer(aPlayer)) {
            return false;
        }

        IConstructable constructable = null;
        if (tTileEntity instanceof IConstructableProvider)
            constructable = ((IConstructableProvider) tTileEntity).getConstructable();
        if (constructable == null && tTileEntity instanceof IConstructable) {
            constructable = (IConstructable) tTileEntity;
        }
        if (constructable == null && IMultiblockInfoContainer.contains(tTileEntity.getClass())) {
            IAlignment alignment = tTileEntity instanceof IAlignment ? (IAlignment) tTileEntity : null;
            if (alignment == null && tTileEntity instanceof IAlignmentProvider)
                alignment = ((IAlignmentProvider) tTileEntity).getAlignment();
            ExtendedFacing facing = alignment != null ? alignment.getExtendedFacing()
                    : ExtendedFacing.of(ForgeDirection.getOrientation(aSide));
            constructable = IMultiblockInfoContainer.<TileEntity>get(tTileEntity.getClass())
                    .toConstructable(tTileEntity, facing);
        }

        if (constructable == null) return false;

        if (aPlayer instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) aPlayer;
            // server side and sneaking (already checked above)
            // do construct
            if (aPlayer.capabilities.isCreativeMode || aStack.getItem() instanceof ItemDebugConstructableTrigger) {
                constructable.construct(aStack, false);
            } else if (constructable instanceof ISurvivalConstructable) {
                int built = ((ISurvivalConstructable) constructable).survivalConstruct(
                        aStack,
                        ConfigurationHandler.INSTANCE.getAutoPlaceBudget(),
                        ISurvivalBuildEnvironment.create(IItemSource.fromPlayer(playerMP), playerMP));
                if (built > 0) {
                    playerMP.addChatMessage(new ChatComponentTranslation("impactapi.autoplace.built_stat", built));
                } else if (built == -1) {
                    playerMP.addChatMessage(new ChatComponentTranslation("impactapi.autoplace.complete"));
                }
                setLastUseMilis(aPlayer);
            } else {
                playerMP.addChatMessage(new ChatComponentTranslation("impactapi.autoplace.error.not_enabled"));
            }
            return true;
        }
        // client side
        // particles and text
        constructable.construct(aStack, true);
        if (System.currentTimeMillis() - getLastUseMilis(aPlayer) >= 300)
            Main.addClientSideChatMessages(constructable.getStructureDescription(aStack));
        setLastUseMilis(aPlayer);
        return false;
    }

    private static void setLastUseMilis(EntityPlayer aPlayer) {
        if (!(aPlayer instanceof EntityPlayerMP))
            // assume client side
            clientSideLastUse = System.currentTimeMillis();
        else lastUse.put((EntityPlayerMP) aPlayer, System.currentTimeMillis());
    }

    private static long getLastUseMilis(EntityPlayer aPlayer) {
        if (!(aPlayer instanceof EntityPlayerMP))
            // assume client side
            return clientSideLastUse;
        return lastUse.getOrDefault(aPlayer, 0L);
    }
}
