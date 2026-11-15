package space.impact.structures.kotlinx.structure

/** Layers follow the Java API's C slices, B rows and A characters without padding or axis changes. */
@StructureDsl
public class ShapeBuilder {

    private val layers: MutableList<Array<String>> = mutableListOf()

    public fun layer(vararg rows: String) {
        layers.add(rows.toList().toTypedArray())
    }

    internal fun buildLayers(): Array<Array<String>> {
        return layers.toTypedArray()
    }
}
