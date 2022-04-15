package net.marsupialgutz.pctl_meta.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.marsupialgutz.pctl_meta.PlayerctlControls.*;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))

    public void onRender(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        if (showStatus && !MinecraftClient.getInstance().options.debugEnabled) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "Song:", 5, 5, 0xffffff);
            if (playing) {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, result.substring(1, result.length() - 1), 5, 15, 0xffffff);
            } else {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, result.substring(1, result.length() - 1) + " (Paused)", 5, 15, 0xffffff);
            }
        }
    }

}