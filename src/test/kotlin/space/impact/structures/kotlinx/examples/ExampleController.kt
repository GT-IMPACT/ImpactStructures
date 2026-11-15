package space.impact.structures.kotlinx.examples

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import space.impact.structures.alignment.constructable.IConstructable
import space.impact.structures.alignment.constructable.IConstructableProvider
import space.impact.structures.alignment.constructable.ISurvivalConstructable
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.hint.HintColor
import space.impact.structures.kotlinx.channels.setChannelValue
import space.impact.structures.kotlinx.execution.buildCreative
import space.impact.structures.kotlinx.execution.buildSurvival
import space.impact.structures.kotlinx.execution.checkStructure
import space.impact.structures.kotlinx.execution.showHints
import space.impact.structures.kotlinx.execution.withInstrumentation
import space.impact.structures.kotlinx.hint.showHint
import space.impact.structures.kotlinx.hint.withHinting
import space.impact.structures.kotlinx.structure.StructureBuilder
import space.impact.structures.kotlinx.structure.structure
import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.ISurvivalBuildEnvironment

/** Compile-checked integration example; block/tile registration belongs to the consuming mod. */
public class ExampleController : TileEntity(), IConstructableProvider, ISurvivalConstructable {

    public var casingTier: Int? = null
        private set

    public var acceptedCasings: Int = 0
        private set

    public var facing: ExtendedFacing = ExtendedFacing.DEFAULT

    override fun getConstructable(): IConstructable {
        return this
    }

    override fun getStructureDefinition(): IStructureDefinition<ExampleController> {
        return STRUCTURE
    }

    public fun checkMachine(): Boolean {
        val world: World = worldObj ?: return false
        casingTier = null
        acceptedCasings = 0

        return withInstrumentation(identifier = this) {
            STRUCTURE.checkStructure(
                context = this,
                world = world,
                worldX = xCoord,
                worldY = yCoord,
                worldZ = zCoord,
                facing = facing,
                offsetA = OFFSET_A,
                offsetB = OFFSET_B,
            )
        }
    }

    override fun construct(
        trigger: ItemStack,
        hintsOnly: Boolean,
    ) {
        constructAtFacing(
            trigger = trigger,
            hintsOnly = hintsOnly,
            facing = facing,
        )
    }

    public fun constructAtFacing(
        trigger: ItemStack,
        hintsOnly: Boolean,
        facing: ExtendedFacing,
    ) {
        val world: World = worldObj ?: return
        if (hintsOnly) {
            if (!world.isRemote) {
                return
            }
            withHinting(world = world) {
                STRUCTURE.showHints(
                    context = this,
                    world = world,
                    worldX = xCoord,
                    worldY = yCoord,
                    worldZ = zCoord,
                    trigger = trigger,
                    facing = facing,
                    offsetA = OFFSET_A,
                    offsetB = OFFSET_B,
                )
            }
        } else {
            if (world.isRemote) {
                return
            }
            STRUCTURE.buildCreative(
                context = this,
                world = world,
                worldX = xCoord,
                worldY = yCoord,
                worldZ = zCoord,
                trigger = trigger,
                facing = facing,
                offsetA = OFFSET_A,
                offsetB = OFFSET_B,
            )
        }
    }

    override fun survivalConstruct(
        trigger: ItemStack,
        elementBudget: Int,
        environment: ISurvivalBuildEnvironment,
    ): Int {
        return survivalAtFacing(
            trigger = trigger,
            budget = elementBudget,
            environment = environment,
            facing = facing,
        )
    }

    public fun survivalAtFacing(
        trigger: ItemStack,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
        facing: ExtendedFacing,
    ): Int {
        val world: World = worldObj ?: return -2
        return STRUCTURE.buildSurvival(
            context = this,
            world = world,
            worldX = xCoord,
            worldY = yCoord,
            worldZ = zCoord,
            trigger = trigger,
            budget = budget,
            environment = environment,
            facing = facing,
            offsetA = OFFSET_A,
            offsetB = OFFSET_B,
        )
    }

    override fun getStructureDescription(trigger: ItemStack): Array<String> {
        return arrayOf(
            "Stone casing, one glass window, an empty inner cell and uniform iron/gold tier blocks.",
            "The casing channel selects the tier used for building.",
        )
    }

    public fun configureProjector(
        trigger: ItemStack,
        tier: Int,
    ) {
        require(tier in 1..2) {
            "Casing tier must be 1 or 2"
        }
        trigger.setChannelValue(
            name = "casing",
            value = tier,
        )
    }

    private companion object {

        private const val OFFSET_A: Int = 1
        private const val OFFSET_B: Int = 1

        private val STRUCTURE: IStructureDefinition<ExampleController> by lazy {
            structure<ExampleController> {
                shape(name = "main") {
                    layer(
                        "CCC",
                        "C~W",
                        "CTC",
                    )
                    layer(
                        "CCC",
                        "CAC",
                        "CTC",
                    )
                }
                channel(
                    name = "casing",
                    values = 1..2,
                )
                element(
                    symbol = 'C',
                    element = onPass(element = block(block = Blocks.stone)) {
                        acceptedCasings++
                    },
                )
                element(
                    symbol = 'A',
                    element = air(),
                )
                element(
                    symbol = 'T',
                    element = channeled(
                        element = tiered<Int?>(
                            extractor = { candidate: Block, _ ->
                                when (candidate) {
                                    Blocks.iron_block -> 1
                                    Blocks.gold_block -> 2
                                    else -> null
                                }
                            },
                            notSet = null,
                            property = ExampleController::casingTier,
                            knownTiers = listOf(
                                Pair(
                                    first = Blocks.iron_block,
                                    second = 0,
                                ),
                                Pair(
                                    first = Blocks.gold_block,
                                    second = 0,
                                ),
                            ),
                        ),
                        channel = "casing",
                    ),
                )
                element(
                    symbol = 'W',
                    element = windowElement(),
                )
            }
        }

        private fun StructureBuilder<ExampleController>.windowElement(): IStructureElement<ExampleController> {
            val glass: IStructureElement<ExampleController> = block(block = Blocks.glass)
            return customElement(
                check = { world, x, y, z ->
                    glass.check(
                        this,
                        world,
                        x,
                        y,
                        z,
                    )
                },
                couldBeValid = { world, x, y, z, _ ->
                    glass.check(
                        this,
                        world,
                        x,
                        y,
                        z,
                    )
                },
                hint = { world, x, y, z, _ ->
                    world.showHint(
                        worldX = x,
                        worldY = y,
                        worldZ = z,
                        color = HintColor.LIGHT_BLUE,
                    )
                    true
                },
                place = { world, x, y, z, trigger ->
                    glass.placeBlock(
                        this,
                        world,
                        x,
                        y,
                        z,
                        trigger,
                    )
                },
                survival = { world, x, y, z, trigger, environment ->
                    glass.survivalPlaceBlock(
                        this,
                        world,
                        x,
                        y,
                        z,
                        trigger,
                        environment,
                    )
                },
                blocksToPlace = { world, x, y, z, trigger, environment ->
                    glass.getBlocksToPlace(
                        this,
                        world,
                        x,
                        y,
                        z,
                        trigger,
                        environment,
                    )
                },
            )
        }
    }
}
