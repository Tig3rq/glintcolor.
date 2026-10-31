package pl.glintcolor.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.glintcolor.GlintConfig;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin extends RenderPhase {
	protected RenderLayerMixin(String name, Runnable beginAction, Runnable endAction) {
		super(name, beginAction, endAction);
	}

	@Unique
	private boolean glintcolor$isGlint() {
		return this.name.contains("glint");
	}

	@Inject(method = "startDrawing", at = @At("TAIL"))
	private void glintcolor$begin(CallbackInfo ci) {
		if (glintcolor$isGlint()) {
			float[] c = GlintConfig.current();
			RenderSystem.setShaderColor(c[0], c[1], c[2], 1.0f);
		}
	}

	@Inject(method = "endDrawing", at = @At("HEAD"))
	private void glintcolor$end(CallbackInfo ci) {
		if (glintcolor$isGlint()) {
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
}
