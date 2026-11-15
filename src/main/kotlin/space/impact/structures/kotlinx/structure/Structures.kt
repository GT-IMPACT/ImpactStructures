package space.impact.structures.kotlinx.structure

import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.structure.StructureUtility

public fun <T> structure(configure: StructureBuilder<T>.() -> Unit): IStructureDefinition<T> {
    val builder = StructureBuilder<T>()
    configure.invoke(builder)
    return builder.build()
}

/** Explicit B/C transposition with exactly the Java API's shape semantics. */
public fun transpose(layers: Array<Array<String>>): Array<Array<String>> {
    return StructureUtility.transpose(layers)
}

public fun transpose(layers: List<List<String>>): List<List<String>> {
    val array = layers.map { rows ->
        rows.toTypedArray()
    }.toTypedArray()
    return StructureUtility.transpose(array).map { rows ->
        rows.toList()
    }
}
