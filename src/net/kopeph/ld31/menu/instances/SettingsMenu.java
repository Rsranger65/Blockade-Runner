package net.kopeph.ld31.menu.instances;

import net.kopeph.ld31.Audio;
import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.Slider;
import net.kopeph.ld31.menu.TextBox;
import processing.core.PApplet;

public final class SettingsMenu extends Menu {
	public SettingsMenu(Audio audio) {
		add(new TextBox(context.renderer.font, "Settings", 0, -175));

		add(new TextBox(context.renderer.font, "Music Volume", -180, -125));
		add(new Slider(context.renderer.font, 60, -125, 340, 10, 0.5f, (value) -> { audio.setVolume(Audio.VOL_MUSIC, PApplet.pow(value, 0.25f)); }));

		add(new MenuButton(context.renderer.font, "Key Bindings", 0,  20, 400, 50, (down) -> { context.setGameState(LD31.ST_KEYBIND); }));
		add(new MenuButton(context.renderer.font, "Back"        , 0, 120, 400, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
	}
}
