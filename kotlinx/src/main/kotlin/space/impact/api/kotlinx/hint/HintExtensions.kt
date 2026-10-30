package space.impact.api.kotlinx.hint

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import space.impact.api.ImpactAPI
import space.impact.api.hint.HintColor
import space.impact.api.hint.HintMarkerKind

public fun World.showHint(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    color: HintColor,
) {
    ImpactAPI.hintParticle(
        this,
        worldX,
        worldY,
        worldZ,
        color,
    )
}

public fun World.showMarker(
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    kind: HintMarkerKind,
) {
    ImpactAPI.hintMarker(
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
    color: HintColor,
    tint: ShortArray,
) {
    ImpactAPI.hintParticleTinted(
        this,
        worldX,
        worldY,
        worldZ,
        ImpactAPI.getBlockHint(),
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
    ImpactAPI.hintParticleTinted(
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
    return ImpactAPI.markHintParticleError(
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
    return ImpactAPI.updateHintParticleTint(
        this,
        worldObj,
        worldX,
        worldY,
        worldZ,
        tint,
    )
}
