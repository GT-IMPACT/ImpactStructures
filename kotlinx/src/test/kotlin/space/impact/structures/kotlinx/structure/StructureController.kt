package space.impact.structures.kotlinx.structure

internal class StructureController {

    var tier: Int? = null
    var selected: Int = 0
    var callbacks: Int = 0

    fun readTier(): Int? {
        return tier
    }

    fun writeTier(value: Int?) {
        tier = value
    }
}
