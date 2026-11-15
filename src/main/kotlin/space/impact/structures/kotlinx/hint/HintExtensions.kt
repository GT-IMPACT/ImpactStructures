package space.impact.structures.kotlinx.hint

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.world.World
import space.impact.structures.ImpactStructures
import space.impact.structures.hint.HintColor
import space.impact.structures.hint.HintMarkerKind

public fun World.showHint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    color: HintColor,
) {
    ImpactStructures.hintParticle(
        this,
        worldX,
        worldY,
        worldZ,
        color,
    )
}

/** Custom textures may use an independent item, including its metadata and NBT, for the hover label. */
public fun World.showHint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    icons: Array<out IIcon?>,
    displayStack: ItemStack? = null,
) {
    ImpactStructures.hintParticle(
        this,
        worldX,
        worldY,
        worldZ,
        icons,
        displayStack,
    )
}

public fun World.showMarker(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    kind: HintMarkerKind,
) {
    ImpactStructures.hintMarker(
        this,
        worldX,
        worldY,
        worldZ,
        kind,
    )
}

public fun World.showTintedHint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    icons: Array<out IIcon?>,
    tint: ShortArray,
    displayStack: ItemStack? = null,
) {
    ImpactStructures.hintParticleTinted(
        this,
        worldX,
        worldY,
        worldZ,
        icons,
        tint,
        displayStack,
    )
}

public fun World.showTintedHint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    color: HintColor,
    tint: ShortArray,
) {
    ImpactStructures.hintParticleTinted(
        this,
        worldX,
        worldY,
        worldZ,
        ImpactStructures.getBlockHint(),
        color.metadata,
        tint,
    )
}

public fun World.showTintedHint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    block: Block,
    metadata: Int,
    tint: ShortArray,
) {
    ImpactStructures.hintParticleTinted(
        this,
        worldX,
        worldY,
        worldZ,
        block,
        metadata,
        tint,
    )
}

public fun EntityPlayer.markHintError(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
): Boolean {
    return ImpactStructures.markHintParticleError(
        this,
        worldObj,
        worldX,
        worldY,
        worldZ,
    )
}

public fun EntityPlayer.updateHintTint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    tint: ShortArray,
): Boolean {
    return ImpactStructures.updateHintParticleTint(
        this,
        worldObj,
        worldX,
        worldY,
        worldZ,
        tint,
    )
}
