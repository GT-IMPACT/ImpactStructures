package space.impact.structures.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import space.impact.structures.Main;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.structure.IStructureElement.PlaceResult;
import space.impact.structures.util.FluidUtility;
import space.impact.structures.util.InventoryUtility;
import space.impact.structures.util.InventoryUtility.ItemStackExtractor;
import space.impact.structures.util.InventoryUtility.ItemStackExtractor.APIType;

class FluidPlacementTest {

    private Field loaderField;
    private Object previousLoader;
    private EntityPlayerMP actor;
    private World world;
    private Block previousBlock;

    @BeforeEach
    void prepareFluidRuntime() throws Exception {
        loaderField = Loader.class.getDeclaredField("instance");
        loaderField.setAccessible(true);
        previousLoader = loaderField.get(null);
        Loader loader = mock(Loader.class);
        ModContainer mod = mock(ModContainer.class);
        when(mod.getModId()).thenReturn("impactapi_test");
        when(loader.activeModContainer()).thenReturn(mod);
        when(loader.getIndexedModList()).thenReturn(Collections.emptyMap());
        loaderField.set(null, loader);
        if (Launch.blackboard == null) {
            Launch.blackboard = new HashMap<>();
        }
        Launch.blackboard.put("fml.deobfuscatedEnvironment", false);
        Main.DEBUG_MODE = false;

        // The lightweight test runtime has no vanilla registry bootstrap.
        setIfMissing(Blocks.class, "water", new Block(Material.water) {});
        setIfMissing(Blocks.class, "flowing_water", new BlockLiquid(Material.water) {});
        setIfMissing(Blocks.class, "lava", new Block(Material.lava) {});
        setIfMissing(Blocks.class, "flowing_lava", new BlockLiquid(Material.lava) {});
        for (String name : new String[] { "bucket", "water_bucket", "lava_bucket", "glass_bottle" }) {
            setIfMissing(Items.class, name, new Item());
        }
        setIfMissing(Items.class, "potionitem", new ItemPotion());
        FluidContainerRegistry.getFluidForFilledItem(new ItemStack(Items.water_bucket));

        actor = mock(EntityPlayerMP.class);
        actor.capabilities = new PlayerCapabilities();
        actor.inventory = new InventoryPlayer(actor);
        actor.inventoryContainer = mock(Container.class);
        previousBlock = mock(Block.class);
        world = mock(World.class);
        when(previousBlock.isAir(world, 1, 2, 3)).thenReturn(true);
        when(world.getBlock(1, 2, 3)).thenReturn(previousBlock);
        when(world.getBlockMetadata(1, 2, 3)).thenReturn(4);
        when(world.setBlock(1, 2, 3, Blocks.water, 0, 2)).thenReturn(true);
    }

    @AfterEach
    void restoreLoader() throws Exception {
        loaderField.set(null, previousLoader);
    }

    @Test
    void waterElementConsumesOneBucketAndReturnsEmptyContainer() {
        actor.inventory.setInventorySlotContents(0, new ItemStack(Items.water_bucket, 2));
        IItemSource source = IItemSource.fromPlayer(actor);
        IStructureElement<Object> element = StructureUtility.ofBlock(Blocks.water, 0);
        AutoPlaceEnvironment env = wrappedEnvironment(source);

        assertEquals(PlaceResult.ACCEPT, element.survivalPlaceBlock(null, world, 1, 2, 3, null, env));
        assertEquals(1, actor.inventory.getStackInSlot(0).stackSize);
        assertSame(Items.bucket, actor.inventory.getStackInSlot(1).getItem());
        verify(world).setBlock(1, 2, 3, Blocks.water, 0, 2);
        verify(world).notifyBlockChange(1, 2, 3, Blocks.water);
    }

    @Test
    void anyMetadataFluidElementUsesContainersWithoutAnItemBlock() {
        actor.inventory.setInventorySlotContents(0, new ItemStack(Items.water_bucket));
        IStructureElement<Object> element = StructureUtility.ofBlockAnyMeta(Blocks.water, Blocks.water, 0);
        AutoPlaceEnvironment env = wrappedEnvironment(IItemSource.fromPlayer(actor));

        assertEquals(PlaceResult.ACCEPT, element.survivalPlaceBlock(null, world, 1, 2, 3, null, env));
        assertSame(Items.bucket, actor.inventory.getStackInSlot(0).getItem());
        when(world.getBlock(1, 2, 3)).thenReturn(Blocks.water);
        assertEquals(PlaceResult.SKIP, element.survivalPlaceBlock(null, world, 1, 2, 3, null, env));
    }

    @Test
    void reusableContainerIsNotMutatedBySimulationAndKeepsRemainingFluid() {
        ItemFluidContainer tank = new ItemFluidContainer(0, 4000);
        ItemStack filled = new ItemStack(tank);
        tank.fill(filled, new FluidStack(FluidRegistry.WATER, 2500), true);
        InventoryBasic inventory = new InventoryBasic("tank", false, 1);
        inventory.setInventorySlotContents(0, filled);
        IItemSource source = IItemSource.fromInventory(inventory);
        List<ItemStack> returned = new ArrayList<>();
        FluidStack request = new FluidStack(FluidRegistry.WATER, 1000);

        assertTrue(FluidUtility.takeFluid(source, request, true, returned::add));
        assertEquals(2500, tank.getFluid(filled).amount);
        assertTrue(returned.isEmpty());
        assertTrue(FluidUtility.takeFluid(source, request, false, returned::add));
        assertNull(inventory.getStackInSlot(0));
        assertEquals(1500, tank.getFluid(returned.get(0)).amount);
        assertEquals(2500, tank.getFluid(filled).amount);
    }

    @Test
    void wrongFluidAndInsufficientContainerRemainUntouched() {
        ItemFluidContainer tank = new ItemFluidContainer(0, 4000);
        ItemStack filled = new ItemStack(tank);
        tank.fill(filled, new FluidStack(FluidRegistry.WATER, 999), true);
        InventoryBasic inventory = new InventoryBasic("tank", false, 1);
        inventory.setInventorySlotContents(0, filled);
        IItemSource source = IItemSource.fromInventory(inventory);

        assertFalse(FluidUtility.takeFluid(source, new FluidStack(FluidRegistry.WATER, 1000), false, s -> fail()));
        assertFalse(FluidUtility.takeFluid(source, new FluidStack(FluidRegistry.LAVA, 500), false, s -> fail()));
        assertEquals(999, tank.getFluid(filled).amount);
        assertSame(filled, inventory.getStackInSlot(0));
    }

    @Test
    void failedPlacementDoesNotConsumeBucket() {
        actor.inventory.setInventorySlotContents(0, new ItemStack(Items.water_bucket));
        when(world.setBlock(1, 2, 3, Blocks.water, 0, 2)).thenReturn(false);

        assertEquals(PlaceResult.REJECT, placeWater(IItemSource.fromPlayer(actor)));
        assertSame(Items.water_bucket, actor.inventory.getStackInSlot(0).getItem());
        assertNull(actor.inventory.getStackInSlot(1));
    }

    @Test
    void unavailableFluidDoesNotModifyWorldAndExistingFluidIsSkipped() {
        IItemSource source = IItemSource.fromPlayer(actor);
        assertEquals(PlaceResult.REJECT, placeWater(source));
        verify(world, never()).setBlock(1, 2, 3, Blocks.water, 0, 2);
        when(world.getBlock(1, 2, 3)).thenReturn(Blocks.water);
        when(world.getBlockMetadata(1, 2, 3)).thenReturn(0);
        assertEquals(PlaceResult.SKIP, placeWater(source));
    }

    @Test
    void bulkFluidRequestsSurviveEnvironmentWrapperAndRollBackFailedExtraction() {
        IItemSource source = mock(IItemSource.class);
        when(source.takeFluid(any(), eq(true)))
                .thenReturn(true);
        when(source.takeFluid(any(), eq(false)))
                .thenReturn(false);

        assertEquals(PlaceResult.REJECT, placeWater(source));
        verify(source).takeFluid(any(), eq(false));
        verify(world).setBlock(1, 2, 3, previousBlock, 4, 2);
        verify(world, never()).notifyBlockChange(1, 2, 3, Blocks.water);
    }

    @Test
    void playerSourceFindsBulkFluidAdapterAndPlacesWaterWithoutABucket() {
        Item terminal = new Item();
        ItemStack held = new ItemStack(terminal);
        actor.inventory.setInventorySlotContents(0, held);
        ItemStackExtractor extractor = mock(ItemStackExtractor.class);
        when(extractor.isAPIImplemented(any())).thenAnswer(call -> call.getArgument(0) == APIType.MAIN);
        when(extractor.isValidSource(any(), any())).thenAnswer(call -> {
            ItemStack stack = call.getArgument(0);
            return stack.getItem() == terminal;
        });
        when(extractor.takeFluid(any(), any(), eq(true), any())).thenReturn(true);
        when(extractor.takeFluid(any(), any(), eq(false), any())).thenReturn(true);
        InventoryUtility.registerStackExtractor("test-fluid-terminal", extractor);

        assertEquals(PlaceResult.ACCEPT, placeWater(IItemSource.fromPlayer(actor)));
        verify(extractor).takeFluid(eq(held), argThat(fluid -> fluid.amount == 1000
                && fluid.getFluid() == FluidRegistry.WATER), eq(false), eq(actor));
        assertSame(held, actor.inventory.getStackInSlot(0));
        assertNull(actor.inventory.getStackInSlot(1));
    }

    @Test
    void fullInventoryDropsEmptyBucketWithoutLosingIt() {
        Item filler = new Item();
        for (int slot = 0; slot < actor.inventory.mainInventory.length; slot++) {
            actor.inventory.setInventorySlotContents(slot, new ItemStack(filler, 64));
        }
        actor.inventory.setInventorySlotContents(0, new ItemStack(Items.water_bucket, 2));
        assertEquals(PlaceResult.ACCEPT, placeWater(IItemSource.fromPlayer(actor)));
        verify(actor).dropPlayerItemWithRandomChoice(
                argThat(stack -> stack.getItem() == Items.bucket),
                eq(false)
        );
    }

    @Test
    void lavaAndModdedFluidBlocksUseTheirRegisteredFluid() {
        assertSame(FluidRegistry.WATER, FluidUtility.getFluid(Blocks.flowing_water));
        assertSame(FluidRegistry.LAVA, FluidUtility.getFluid(Blocks.flowing_lava));
        Fluid fluid = new Fluid("impactapi_test_build_fluid");
        FluidRegistry.registerFluid(fluid);
        Block block = new BlockFluidClassic(fluid, Material.water);
        assertSame(fluid, FluidUtility.getFluid(block));
        assertNull(FluidUtility.getFluid(new Block(Material.rock) {}));
    }

    private PlaceResult placeWater(IItemSource source) {
        return StructureUtility.ofBlock(Blocks.water, 0)
                .survivalPlaceBlock(null, world, 1, 2, 3, null, wrappedEnvironment(source));
    }

    private AutoPlaceEnvironment wrappedEnvironment(IItemSource source) {
        return new AutoPlaceEnvironment(
                source, actor, null, mock(IStructureDefinition.class), "main", ExtendedFacing.DEFAULT,
                new int[3], new int[3]);
    }

    private static void setIfMissing(Class<?> owner, String name, Object value) throws Exception {
        Field field = owner.getField(name);
        if (field.get(null) == null) {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.set(null, value);
        }
    }
}
