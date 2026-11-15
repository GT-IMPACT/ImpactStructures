package space.impact.structures.mixins.early.structureRender;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.impact.structures.render.client.HiddenBlocks;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDF)V",
            at = @At("HEAD"), cancellable = true)
    private void hideReplacedTileEntity(
            TileEntity tile,
            double x,
            double y,
            double z,
            float partialTicks,
            CallbackInfo callback
    ) {
        if (HiddenBlocks.contains(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord)) {
            callback.cancel();
        }
    }
}
