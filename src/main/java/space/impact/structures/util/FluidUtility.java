package space.impact.structures.util;

import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;

import space.impact.structures.structure.IItemSource;

public final class FluidUtility {

    private FluidUtility() {
    }

    public static Fluid getFluid(Block block) {
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).getFluid();
        }
        if (block == Blocks.water || block == Blocks.flowing_water) {
            return FluidRegistry.WATER;
        }
        if (block == Blocks.lava || block == Blocks.flowing_lava) {
            return FluidRegistry.LAVA;
        }
        return block.getMaterial().isLiquid() ? FluidRegistry.lookupFluidForBlock(block) : null;
    }

    /** Drain a container through the ordinary item sources, then try bulk fluid storage. */
    public static boolean takeFluid(
            IItemSource source,
            FluidStack fluid,
            boolean simulate,
            Consumer<ItemStack> returnContainer
    ) {
        if (fluid == null || fluid.amount <= 0) {
            throw new IllegalArgumentException("Fluid request must have a positive amount");
        }
        ItemStack container = source.takeOne(stack -> drainContainer(stack, fluid) != null, true);
        if (container == null) {
            return source.takeFluid(fluid, simulate);
        }
        DrainedContainer drained = drainContainer(container, fluid);
        if (drained == null) {
            return false;
        }
        if (simulate) {
            return true;
        }
        if (!source.takeOne(container, false)) {
            return false;
        }
        if (drained.remainder != null && drained.remainder.stackSize > 0) {
            returnContainer.accept(drained.remainder);
        }
        return true;
    }

    private static DrainedContainer drainContainer(ItemStack stack, FluidStack requested) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) {
            return null;
        }
        // Some container APIs normalize NBT even when querying. Never give them the stored stack.
        ItemStack working = stack.copy();
        working.stackSize = 1;
        if (working.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem item = (IFluidContainerItem) working.getItem();
            FluidStack drained = item.drain(working, requested.amount, true);
            if (drained == null || !drained.isFluidEqual(requested) || drained.amount != requested.amount) {
                return null;
            }
            return new DrainedContainer(working);
        }
        FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(working);
        if (contained == null || !contained.isFluidEqual(requested) || contained.amount != requested.amount) {
            return null;
        }
        return new DrainedContainer(FluidContainerRegistry.drainFluidContainer(working));
    }

    private static final class DrainedContainer {

        private final ItemStack remainder;

        private DrainedContainer(ItemStack remainder) {
            this.remainder = remainder;
        }
    }
}
