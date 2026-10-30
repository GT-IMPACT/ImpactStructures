package space.impact.api.kotlinx.structure

import space.impact.api.hint.HintColor
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.StructureUtility

public fun <T> withHint(
    element: IStructureElement<T>,
    color: HintColor,
): IStructureElement<T> {
    return StructureUtility.withHint(
        element,
        color,
    )
}
