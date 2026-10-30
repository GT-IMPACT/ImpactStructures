package space.impact.api.kotlinx.structure

import net.minecraft.item.ItemStack
import net.minecraft.world.World
import space.impact.api.structure.AutoPlaceEnvironment
import space.impact.api.structure.IStructureElement.BlocksToPlace
import space.impact.api.structure.IStructureElement.PlaceResult

public typealias CheckCallback<T> = T.(World, Int, Int, Int) -> Boolean

public typealias HintCallback<T> = T.(World, Int, Int, Int, ItemStack?) -> Boolean

public typealias SurvivalCallback<T> = T.(
    World,
    Int,
    Int,
    Int,
    ItemStack?,
    AutoPlaceEnvironment,
) -> PlaceResult

public typealias BlocksCallback<T> = T.(
    World,
    Int,
    Int,
    Int,
    ItemStack?,
    AutoPlaceEnvironment,
) -> BlocksToPlace?
