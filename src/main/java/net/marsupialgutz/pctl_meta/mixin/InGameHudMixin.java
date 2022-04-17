package net.marsupialgutz.pctl_meta.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
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

    public void onRender(final MatrixStack matrices, final float tickDelta, final CallbackInfo info) {
        final var mc = MinecraftClient.getInstance();
        if (showStatus && !MinecraftClient.getInstance().options.debugEnabled) {
            var id = mc.getTextureManager().registerDynamicTexture("img", nativeImageBackedTexture);
            if (!(result == null)) {
                if (!(result.equals("No players found"))) {
                    RenderSystem.setShaderTexture(0, id);
                    DrawableHelper.drawTexture(matrices, 5, 5, 0, 0, 25, 25, 25, 25);
                    mc.textRenderer.drawWithShadow(matrices, "Song:", 35, 8, 0xffffff);
                    if (playing) {
                        mc.textRenderer.drawWithShadow(matrices, result, 35, 18, 0xffffff);
                    } else {
                        mc.textRenderer.drawWithShadow(matrices, result + " (Paused)", 35, 18, 0xffffff);
                    }
                }
            }
        }
    }

}
