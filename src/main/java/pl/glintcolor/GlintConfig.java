package pl.glintcolor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlintConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("glintcolor.json");

	// 0xRRGGBB
	public static int color = 0xFFFFFF;
	public static boolean rainbow = false;
	public static float rainbowSpeed = 1.0f;
	public static int style = 0;

	private static class Data {
		int color = 0xFFFFFF;
		boolean rainbow = false;
		float rainbowSpeed = 1.0f;
		int style = 0;
	}

	/** Zwraca aktualny kolor jako float[3] (r,g,b 0..1). */
	public static float[] current() {
		int c = color;
		if (rainbow) {
			float hue = ((System.currentTimeMillis() % 10000L) / 10000f) * rainbowSpeed;
			c = java.awt.Color.HSBtoRGB(hue - (float) Math.floor(hue), 1f, 1f);
		}
		return new float[]{((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f};
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				Data d = GSON.fromJson(Files.readString(FILE), Data.class);
				if (d != null) { color = d.color; rainbow = d.rainbow; rainbowSpeed = d.rainbowSpeed; style = d.style; }
			}
		} catch (Exception e) {
			System.err.println("[glintcolor] nie udało się wczytać configu: " + e);
		}
	}

	public static void save() {
		try {
			Data d = new Data();
			d.color = color; d.rainbow = rainbow; d.rainbowSpeed = rainbowSpeed; d.style = style;
			Files.writeString(FILE, GSON.toJson(d));
		} catch (IOException e) {
			System.err.println("[glintcolor] nie udało się zapisać configu: " + e);
		}
	}
}
