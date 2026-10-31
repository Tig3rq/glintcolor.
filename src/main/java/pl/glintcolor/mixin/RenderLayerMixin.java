package pl.glintcolor.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.glintcolor.GlintConfig;

@Mixin(RenderPhase.class)
public abstract class RenderLayerMixin {
	@Shadow
	@Final
	protected String name;

	@Inject(method = "startDrawing", at = @At("TAIL"), require = 0)
	private void glintcolor$begin(CallbackInfo ci) {
		if (this.name != null && this.name.contains("glint")) {
			float[] c = GlintConfig.current();
			RenderSystem.setShaderColor(c[0], c[1], c[2], 1.0f);
		}
	}

	@Inject(method = "endDrawing", at = @At("HEAD"), require = 0)
	private void glintcolor$end(CallbackInfo ci) {
		if (this.name != null && this.name.contains("glint")) {
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
}
