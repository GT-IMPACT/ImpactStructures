package space.impact.api.kotlinx.constructable

import net.minecraft.item.ItemStack
import space.impact.api.alignment.constructable.IConstructable
import space.impact.api.structure.IStructureDefinition

internal open class Constructable<T>(
    protected val context: T,
    private val description: T.(ItemStack) -> List<String>,
    private val definition: IStructureDefinition<*>?,
    private val constructAction: T.(
        trigger: ItemStack,
        hintsOnly: Boolean,
    ) -> Unit,
) : IConstructable {

    public override fun construct(
        trigger: ItemStack,
        hintsOnly: Boolean,
    ) {
        constructAction(
            context,
            trigger,
            hintsOnly,
        )
    }

    public override fun getStructureDescription(trigger: ItemStack): Array<String> {
        return description(
            context,
            trigger,
        ).toTypedArray()
    }

    public override fun getStructureDefinition(): IStructureDefinition<*>? {
        return definition
    }
}
