package space.impact.structures.kotlinx.structure

import space.impact.structures.hint.HintColor
import space.impact.structures.structure.IStructureElement
import space.impact.structures.structure.StructureUtility

public fun <T> withHint(
    element: IStructureElement<T>,
    color: HintColor,
): IStructureElement<T> {
    return StructureUtility.withHint(
        element,
        color,
    )
}
