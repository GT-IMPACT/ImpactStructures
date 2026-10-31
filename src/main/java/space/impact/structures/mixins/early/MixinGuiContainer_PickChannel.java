package space.impact.structures.mixins.early;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import space.impact.structures.ChannelPickHandler;
import space.impact.structures.ChannelWipeConfirmation;
import space.impact.structures.Main;
import space.impact.structures.item.ItemConstructableTrigger;

import space.impact.structures.compat.nei.NeiChannelPicker;
import cpw.mods.fml.common.Loader;

@Mixin(value = GuiContainer.class, priority = 100)
public abstract class MixinGuiContainer_PickChannel {

    @Inject(method = "mouseClicked(III)V", at = @At("HEAD"), cancellable = true)
    private void istruct$onMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton != 2) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;

        ItemStack cursorStack = player.inventory.getItemStack();
        if (cursorStack == null || !(cursorStack.getItem() instanceof ItemConstructableTrigger)) return;

        // Sink middle click as soon as projector is picked up
        ci.cancel();

        ItemStack hoveredStack = null;
        try {
            Slot slot = istruct$invokeGetSlotAtPosition(mouseX, mouseY);
            if (slot != null) {
                hoveredStack = slot.getStack();
            }

            // try NEI
            if (hoveredStack == null && Loader.isModLoaded("NotEnoughItems")) {
                hoveredStack = NeiChannelPicker.getStackMouseOver((GuiContainer) (Object) this);
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error while resolving hovered slot for channel pick", e);
        }

        boolean isSneaking = GuiScreen.isShiftKeyDown();
        if (isSneaking && !ChannelWipeConfirmation.confirm(player)) return;

        ChannelPickHandler.handleMiddleClick(player, cursorStack, hoveredStack, isSneaking);
    }

    @Invoker("getSlotAtPosition")
    protected abstract Slot istruct$invokeGetSlotAtPosition(int x, int y);
}
