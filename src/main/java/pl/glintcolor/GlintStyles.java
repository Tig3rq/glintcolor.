package pl.glintcolor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;

public class GlintStyles {
	public static final String[] NAMES = {"Klasyczny", "Paski", "Dym", "Szkło", "Kropki"};
	public static final String[] FILES = {"classic", "stripes", "smoke", "glass", "dots"};

	private static final Identifier ITEM = Identifier.ofVanilla("textures/misc/enchanted_glint_item.png");
	private static final Identifier ENTITY = Identifier.ofVanilla("textures/misc/enchanted_glint_entity.png");

	public static void apply() {
		apply(MinecraftClient.getInstance().getResourceManager());
	}

	/** Podmienia vanillową teksturę glinta na wybrany styl. */
	public static void apply(ResourceManager rm) {
		int i = Math.floorMod(GlintConfig.style, FILES.length);
		Identifier src = Identifier.of("glintcolor", "textures/glint/" + FILES[i] + ".png");
		TextureManager tm = MinecraftClient.getInstance().getTextureManager();
		try {
			tm.registerTexture(ITEM, new NativeImageBackedTexture(load(rm, src)));
			tm.registerTexture(ENTITY, new NativeImageBackedTexture(load(rm, src)));
		} catch (Exception e) {
			System.err.println("[glintcolor] nie udało się załadować stylu glinta: " + e);
		}
	}

	private static NativeImage load(ResourceManager rm, Identifier id) throws IOException {
		try (InputStream in = rm.getResource(id).orElseThrow().getInputStream()) {
			return NativeImage.read(in);
		}
	}
}
