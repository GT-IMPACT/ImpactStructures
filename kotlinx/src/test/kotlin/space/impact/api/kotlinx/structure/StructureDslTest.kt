package space.impact.api.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.api.hint.HintColor
import space.impact.api.structure.IStructureDefinition
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.StructureDefinition
import space.impact.api.structure.StructureUtility

internal class StructureDslTest {

    @Test
    fun coloredFramesSupportNamedFactoriesMemberCallsAndDslInference() {
        val casing = object : Block(Material.rock) {}
        val world = StructureTestWorld.create(casing)
        val context = StructureController()
        val decorated = withHint(
            element = constantElement(true),
            color = HintColor.BLUE,
        )
        assertTrue(decorated.check(
            context,
            world,
            0,
            0,
            0,
        ))
        assertTrue(constantElement(true).withHint(HintColor.RED).check(
            context,
            world,
            0,
            0,
            0,
        ))

        val definition = structure<StructureController> {
            shape(name = "main") {
                layer("A~")
            }
            element(
                symbol = 'A',
                element = withHint(
                    element = block(block = casing),
                    color = HintColor.BLACK,
                ),
            )
        }
        assertTrue(definition.isContainedInStructure(
            "main",
            0,
            0,
            0,
        ))
    }

    @Test
    fun raggedLayersMatchJavaShapeOccupancyAndSpecialCharacters() {
        val accepted = constantElement(true)
        val dsl = structure<StructureController> {
            shape(name = "main") {
                layer(
                    "A ~",
                    " A",
                )
                layer(" A")
            }
            element(
                symbol = 'A',
                element = accepted,
            )
        }
        val java = StructureDefinition.builder<StructureController>()
            .addShape(
                "main",
                arrayOf(
                    arrayOf(
                        "A ~",
                        " A",
                    ),
                    arrayOf(" A"),
                ),
            )
            .addElement(
                'A',
                accepted,
            )
            .build()
        for (a in -1..4) {
            for (b in -1..3) {
                for (c in -1..3) {
                    assertEquals(
                        java.isContainedInStructure(
                            "main",
                            a,
                            b,
                            c,
                        ),
                        dsl.isContainedInStructure(
                            "main",
                            a,
                            b,
                            c,
                        ),
                    )
                }
            }
        }
    }

    @Test
    fun membersInferControllerForFactoriesAndVarargChains() {
        val testedBlock = object : Block(Material.rock) {}
        val definition: IStructureDefinition<StructureController> = structure {
            shape(
                name = "main",
                layers = listOf(listOf("~AB")),
            )
            element(
                symbol = 'A',
                element = chain(
                    block(block = testedBlock),
                    hint(HintColor.BLUE),
                ),
            )
            element(
                symbol = 'B',
                element = customElement(
                    check = { _, _, _, _ ->
                        selected == 1
                    },
                    description = {
                        listOf("custom.block")
                    },
                ),
            )
            channel(
                name = "coil tier",
                values = 1..8,
            )
        }
        assertEquals(
            8,
            definition.getChannelDefinition("coil tier").maximumValue,
        )
        assertEquals(
            listOf("custom.block"),
            definition.getStructureFor("main").last().getDescription(StructureController()),
        )
    }

    @Test
    fun propertyAndMethodReferencesSupportNullableUnsetTier() {
        val testedBlock = object : Block(Material.rock) {}
        val world = StructureTestWorld.create(testedBlock)
        world.testedMetadata = 4
        val propertyElement = tieredCheckOnly(
            extractor = { _, metadata ->
                metadata
            },
            notSet = null,
            property = StructureController::tier,
        )
        val methodElement = tieredCheckOnly(
            extractor = { _, metadata ->
                metadata
            },
            notSet = null,
            getter = StructureController::readTier,
            setter = StructureController::writeTier,
        )
        val propertyController = StructureController()
        val methodController = StructureController()
        assertTrue(propertyElement.check(
            propertyController,
            world,
            0,
            0,
            0,
        ))
        assertTrue(methodElement.check(
            methodController,
            world,
            0,
            0,
            0,
        ))
        assertEquals(
            4,
            propertyController.tier,
        )
        assertEquals(
            propertyController.tier,
            methodController.tier,
        )
        world.testedMetadata = 5
        assertFalse(propertyElement.check(
            propertyController,
            world,
            0,
            0,
            0,
        ))
        val invalid = tieredCheckOnly(
            extractor = { _: Block, _: Int ->
                null
            },
            notSet = null,
            property = StructureController::tier,
        )
        val unsetController = StructureController()
        assertFalse(invalid.check(
            unsetController,
            world,
            0,
            0,
            0,
        ))
        assertEquals(
            null,
            unsetController.tier,
        )
    }

    @Test
    fun customCallbacksHaveSafeOptionalDefaultsAndDescriptionChannelsHooks() {
        val testedBlock = object : Block(Material.rock) {}
        val world = StructureTestWorld.create(testedBlock)
        val controller = StructureController()
        val element = customElement<StructureController>(
            check = { _, x, y, z ->
                callbacks += x + y + z
                true
            },
            channels = listOf("coil tier"),
            description = {
                listOf("custom.${selected}")
            },
        )
        assertTrue(element.check(
            controller,
            world,
            1,
            2,
            3,
        ))
        assertEquals(
            6,
            controller.callbacks,
        )
        assertFalse(element.spawnHint(
            controller,
            world,
            0,
            0,
            0,
            null,
        ))
        assertFalse(element.placeBlock(
            controller,
            world,
            0,
            0,
            0,
            null,
        ))
        assertEquals(
            listOf("coil tier"),
            element.channels.toList(),
        )
    }

    @Test
    fun transposeOccursOnlyWhenExplicitlyRequested() {
        val layers = listOf(
            listOf(
                "AA",
                " A",
            ),
            listOf(
                " A",
                "AA",
            ),
        )
        val expected = StructureUtility.transpose(layers.map { rows ->
            rows.toTypedArray()
        }.toTypedArray()).map { rows ->
            rows.toList()
        }
        assertEquals(
            expected,
            transpose(layers),
        )
    }

    private fun constantElement(accepted: Boolean): IStructureElement<StructureController> {
        return customElement(
            check = { _, _, _, _ ->
                accepted
            },
        )
    }
}
