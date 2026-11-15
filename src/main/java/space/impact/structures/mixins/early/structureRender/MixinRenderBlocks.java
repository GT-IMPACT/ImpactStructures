package space.impact.structures.mixins.early.structureRender;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import space.impact.structures.render.client.HiddenBlocks;

@Mixin(RenderBlocks.class)
public class MixinRenderBlocks {

    @Shadow
    public IBlockAccess blockAccess;

    @Inject(method = "renderBlockByRenderType", at = @At("HEAD"), cancellable = true)
    private void hideAnimatedSource(
            Block block,
            int x,
            int y,
            int z,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (HiddenBlocks.contains(blockAccess, x, y, z)) {
            callback.setReturnValue(false);
        }
    }
}
