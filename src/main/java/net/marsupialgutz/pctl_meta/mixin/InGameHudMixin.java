package net.marsupialgutz.pctl_meta.mixin;

import static net.marsupialgutz.pctl_meta.PlayerctlControls.playing;
import static net.marsupialgutz.pctl_meta.PlayerctlControls.result;
import static net.marsupialgutz.pctl_meta.PlayerctlControls.showStatus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))

    public void onRender(final MatrixStack matrices, final float tickDelta, final CallbackInfo info) {
        final var mc = MinecraftClient.getInstance();
        if (showStatus && !MinecraftClient.getInstance().options.debugEnabled) {
            mc.textRenderer.drawWithShadow(matrices, "Song:", 5, 5, 0xffffff);
            if (playing) {
                mc.textRenderer.drawWithShadow(matrices,
                        result.substring(1, result.length() - 1), 5, 15, 0xffffff);
            } else {
                mc.textRenderer.drawWithShadow(matrices,
                        result.substring(1, result.length() - 1) + " (Paused)", 5, 15, 0xffffff);
            }
        }
        mc.close();
    }

}
