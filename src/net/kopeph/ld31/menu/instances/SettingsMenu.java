package net.kopeph.ld31.menu.instances;

import net.kopeph.ld31.Audio;
import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.*;
import processing.core.PApplet;

public final class SettingsMenu extends Menu {
	public SettingsMenu(Audio audio) {
		add(new TextBox(context.renderer.font, "Settings", 0, -175));

		add(new TextBox(context.renderer.font, "Music Volume", -180, -125));
		add(new Slider(context.renderer.font, 60, -125, 340, 10, 0.5f, (value) -> { audio.setVolume(Audio.VOL_MUSIC, PApplet.pow(value, 0.25f)); }));

		add(new Spinner(context.renderer.font, new String[] { "Raw Colors         [ fastest ]",
		                                                      "Color Corrected    [ faster  ]",
		                                                      "Unmoving Textures  [ slower  ]",
		                                                      "Moving Textures    [ slowest ]" }, 2, 0, -60, 400, 50, (value) -> {
			context.renderer.textureOption = value;
		}));

		add(new Button(context.renderer.font, "Key Bindings", 0,  20, 400, 50, (down) -> { context.setGameState(LD31.ST_KEYBIND); }));
		add(new Button(context.renderer.font, "Back"        , 0, 120, 400, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
	}
}
