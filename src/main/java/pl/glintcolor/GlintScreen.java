package pl.glintcolor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class GlintScreen extends Screen {
	private final Screen parent;
	private ButtonWidget rainbowButton;
	private ButtonWidget styleButton;

	private static final String[] NAMES = {"Czerwony", "Pomarańczowy", "Żółty", "Zielony", "Błękitny", "Niebieski", "Fioletowy", "Różowy", "Biały"};
	private static final int[] COLORS = {0xFF2020, 0xFF8000, 0xFFFF20, 0x20FF20, 0x20FFFF, 0x2060FF, 0x9020FF, 0xFF60C0, 0xFFFFFF};

	public GlintScreen(Screen parent) {
		super(Text.literal("Kolor glinta"));
		this.parent = parent;
	}

	private class ChannelSlider extends SliderWidget {
		private final String label;
		private final int shift;

		ChannelSlider(int x, int y, String label, int shift) {
			super(x, y, 200, 20, Text.empty(), ((GlintConfig.color >> shift) & 0xFF) / 255.0);
			this.label = label;
			this.shift = shift;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Text.literal(label + ": " + (int) Math.round(this.value * 255)));
		}

		@Override
		protected void applyValue() {
			int v = (int) Math.round(this.value * 255);
			GlintConfig.color = (GlintConfig.color & ~(0xFF << shift)) | (v << shift);
			GlintConfig.rainbow = false;
			if (rainbowButton != null) rainbowButton.setMessage(rainbowText());
		}
	}

	private Text styleText() {
		return Text.literal("Styl glinta: " + GlintStyles.NAMES[Math.floorMod(GlintConfig.style, GlintStyles.NAMES.length)]);
	}

	private Text rainbowText() {
		return Text.literal("Tęcza: " + (GlintConfig.rainbow ? "WŁ." : "WYŁ."));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y = this.height / 2 - 105;

		addDrawableChild(new ChannelSlider(cx - 100, y, "R", 16));
		addDrawableChild(new ChannelSlider(cx - 100, y + 24, "G", 8));
		addDrawableChild(new ChannelSlider(cx - 100, y + 48, "B", 0));

		// gotowe kolory: 3 rzędy po 3
		for (int i = 0; i < COLORS.length; i++) {
			final int idx = i;
			int bx = cx - 100 + (i % 3) * 68;
			int by = y + 80 + (i / 3) * 22;
			addDrawableChild(ButtonWidget.builder(Text.literal(NAMES[i]), b -> {
				GlintConfig.color = COLORS[idx];
				GlintConfig.rainbow = false;
				clearAndInit();
			}).dimensions(bx, by, 64, 20).build());
		}

		styleButton = addDrawableChild(ButtonWidget.builder(styleText(), b -> {
			GlintConfig.style = (GlintConfig.style + 1) % GlintStyles.FILES.length;
			GlintStyles.apply();
			b.setMessage(styleText());
		}).dimensions(cx - 100, y + 148, 200, 20).build());

		rainbowButton = addDrawableChild(ButtonWidget.builder(rainbowText(), b -> {
			GlintConfig.rainbow = !GlintConfig.rainbow;
			b.setMessage(rainbowText());
		}).dimensions(cx - 100, y + 172, 98, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
			GlintConfig.color = 0xFFFFFF;
			GlintConfig.rainbow = false;
			clearAndInit();
		}).dimensions(cx + 2, y + 172, 98, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> close())
			.dimensions(cx - 100, y + 196, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int cx = this.width / 2;
		int y = this.height / 2 - 105;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, y - 20, 0xFFFFFF);

		// podgląd koloru
		float[] c = GlintConfig.current();
		int rgb = ((int) (c[0] * 255) << 16) | ((int) (c[1] * 255) << 8) | (int) (c[2] * 255);
		context.fill(cx + 108, y, cx + 148, y + 68, 0xFF000000 | rgb);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Podgląd"), cx + 128, y + 72, 0xAAAAAA);
	}

	@Override
	public void removed() {
		GlintConfig.save();
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
