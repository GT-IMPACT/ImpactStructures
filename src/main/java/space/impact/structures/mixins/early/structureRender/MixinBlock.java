package space.impact.structures.mixins.early.structureRender;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import space.impact.structures.render.client.HiddenBlocks;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    private void exposeFacesNextToAnimatedSource(
            IBlockAccess access,
            int x,
            int y,
            int z,
            int side,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (HiddenBlocks.contains(access, x, y, z)) {
            callback.setReturnValue(true);
        }
    }
}
