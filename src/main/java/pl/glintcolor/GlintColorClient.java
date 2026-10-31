package pl.glintcolor;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class GlintColorClient implements ClientModInitializer {
	private static final Map<String, Integer> PRESETS = Map.ofEntries(
		Map.entry("red", 0xFF2020), Map.entry("orange", 0xFF8000), Map.entry("yellow", 0xFFFF20),
		Map.entry("green", 0x20FF20), Map.entry("cyan", 0x20FFFF), Map.entry("blue", 0x2060FF),
		Map.entry("purple", 0x9020FF), Map.entry("pink", 0xFF60C0), Map.entry("white", 0xFFFFFF),
		Map.entry("black", 0x202020), Map.entry("gold", 0xFFC800)
	);

	@Override
	public void onInitializeClient() {
		GlintConfig.load();

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() {
					return Identifier.of("glintcolor", "glint_styles");
				}

				@Override
				public void reload(ResourceManager manager) {
					GlintStyles.apply(manager);
				}
			});

		KeyBinding openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.glintcolor.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.glintcolor"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.wasPressed()) {
				if (client.currentScreen == null) client.setScreen(new GlintScreen(null));
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("glint")
				// /glint set <nazwa|#RRGGBB>
				.then(ClientCommandManager.literal("set")
					.then(ClientCommandManager.argument("kolor", StringArgumentType.word())
						.suggests((ctx, b) -> { PRESETS.keySet().forEach(b::suggest); return b.buildFuture(); })
						.executes(ctx -> {
							String s = StringArgumentType.getString(ctx, "kolor").toLowerCase();
							Integer c = PRESETS.get(s);
							if (c == null) {
								try {
									c = Integer.parseInt(s.startsWith("#") ? s.substring(1) : s, 16) & 0xFFFFFF;
								} catch (NumberFormatException e) {
									ctx.getSource().sendError(Text.literal("Zły kolor. Użyj nazwy (red, blue...) albo hex np. ff00aa"));
									return 0;
								}
							}
							GlintConfig.color = c;
							GlintConfig.rainbow = false;
							GlintConfig.save();
							ctx.getSource().sendFeedback(Text.literal(String.format("Kolor glinta: #%06X", c)));
							return 1;
						})))
				// /glint rainbow [szybkość]
				.then(ClientCommandManager.literal("rainbow")
					.executes(ctx -> setRainbow(ctx.getSource(), 1f))
					.then(ClientCommandManager.argument("szybkosc", FloatArgumentType.floatArg(0.1f, 20f))
						.executes(ctx -> setRainbow(ctx.getSource(), FloatArgumentType.getFloat(ctx, "szybkosc")))))
				// /glint reset
				.then(ClientCommandManager.literal("reset").executes(ctx -> {
					GlintConfig.color = 0xFFFFFF;
					GlintConfig.rainbow = false;
					GlintConfig.save();
					ctx.getSource().sendFeedback(Text.literal("Glint zresetowany."));
					return 1;
				}))
			));
	}

	private static int setRainbow(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, float speed) {
		GlintConfig.rainbow = true;
		GlintConfig.rainbowSpeed = speed;
		GlintConfig.save();
		src.sendFeedback(Text.literal("Tęczowy glint włączony (szybkość " + speed + ")."));
		return 1;
	}
}
