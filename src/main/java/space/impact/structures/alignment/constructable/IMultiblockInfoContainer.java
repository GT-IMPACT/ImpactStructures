package space.impact.structures.alignment.constructable;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.structure.AutoPlaceEnvironment;
import space.impact.structures.structure.IItemSource;
import space.impact.structures.structure.ISurvivalBuildEnvironment;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * To implement IConstructable on not own TileEntities
 */
public interface IMultiblockInfoContainer<T> {

    HashMap<String, IMultiblockInfoContainer<?>> MULTIBLOCK_MAP = new HashMap<>();

    /**
     * There is no specific loading phase restriction, but you should generally not call it before the tile entity is
     * properly registered.
     */
    static <T> void registerTileClass(Class<T> clazz, IMultiblockInfoContainer<? super T> info) {
        if (clazz == null || info == null) throw new NullPointerException();
        MULTIBLOCK_MAP.put(clazz.getName(), info);
    }

    @SuppressWarnings("unchecked")
    static <T> IMultiblockInfoContainer<T> get(Class<?> tClass) {
        return (IMultiblockInfoContainer<T>) MULTIBLOCK_MAP.get(tClass.getName());
    }

    static boolean contains(Class<?> tClass) {
        return MULTIBLOCK_MAP.containsKey(tClass.getName());
    }

    void construct(ItemStack stackSize, boolean hintsOnly, T tileEntity, ExtendedFacing aSide);

    /**
     * Construct the structure using
     * {@link space.impact.structures.structure.IStructureElement#survivalPlaceBlock(Object, World, int, int, int, ItemStack, AutoPlaceEnvironment)}
     *
     * @return -1 if done, a helping pointer
     */
    int survivalConstruct(ItemStack stackSize, int elementBudge, ISurvivalBuildEnvironment env, T tileEntity,
            ExtendedFacing aSide);

    @SideOnly(Side.CLIENT)
    String[] getDescription(ItemStack stackSize);

    default ISurvivalConstructable toConstructable(T tileEntity, ExtendedFacing aSide) {
        return new ISurvivalConstructable() {

            @Override
            public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
                return IMultiblockInfoContainer.this
                        .survivalConstruct(stackSize, elementBudget, env, tileEntity, aSide);
            }

            @Override
            public int survivalConstruct(ItemStack stackSize, int elementBudget, IItemSource source,
                    EntityPlayerMP actor) {
                return IMultiblockInfoContainer.this.survivalConstruct(
                        stackSize,
                        elementBudget,
                        ISurvivalBuildEnvironment.create(source, actor),
                        tileEntity,
                        aSide);
            }

            @Override
            public void construct(ItemStack stackSize, boolean hintsOnly) {
                IMultiblockInfoContainer.this.construct(stackSize, hintsOnly, tileEntity, aSide);
            }

            @Override
            public String[] getStructureDescription(ItemStack stackSize) {
                return IMultiblockInfoContainer.this.getDescription(stackSize);
            }
        };
    }
}
