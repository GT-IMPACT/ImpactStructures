package space.impact.structures.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import space.impact.structures.structure.IChannelDefinition
import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.IStructureElementNoPlacement
import space.impact.structures.structure.StructureDefinition

@StructureDsl
public class StructureBuilder<T> : IElementFactories<T> {

    private val builder: StructureDefinition.Builder<T> = StructureDefinition.builder<T>()

    public fun shape(
        name: String,
        configure: ShapeBuilder.() -> Unit,
    ) {
        val shapeBuilder = ShapeBuilder()
        configure.invoke(shapeBuilder)
        builder.addShape(
            name,
            shapeBuilder.buildLayers(),
        )
    }

    public fun shape(
        name: String,
        layers: List<List<String>>,
    ) {
        builder.addShape(
            name,
            layers.map { rows ->
                rows.toTypedArray()
            }.toTypedArray(),
        )
    }

    public fun element(
        symbol: Char,
        element: IStructureElement<T>,
    ) {
        builder.addElement(
            symbol,
            element,
        )
    }

    public fun channel(definition: IChannelDefinition) {
        builder.addChannel(definition)
    }

    public fun channel(
        name: String,
        values: IntRange = 1..Int.MAX_VALUE,
    ) {
        builder.addChannel(
            name,
            values.first,
            values.last,
        )
    }

    public fun build(): IStructureDefinition<T> {
        return builder.build()
    }

    public inline fun <reified Tile : TileEntity> tileAdderOf(
        hintBlock: Block,
        hintMetadata: Int = 0,
        noinline adder: T.(Tile) -> Boolean,
    ): IStructureElementNoPlacement<T> {
        return specificTileAdder(
            tileClass = Tile::class.java,
            hintBlock = hintBlock,
            hintMetadata = hintMetadata,
            adder = adder,
        )
    }
}
