package net.kopeph.ld31.menu.impl;

import java.util.prefs.Preferences;

import net.kopeph.ld31.Audio;
import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.*;
import processing.core.PApplet;

public final class SettingsMenu extends Menu {
	private static final String
		PREFERENCES_NODE = "settings", //$NON-NLS-1$
		KEY_MUSIC_VOLUME = "music", //$NON-NLS-1$
		KEY_TEXTURE_MODE = "texture"; //$NON-NLS-1$
	private static final float DEFAULT_MUSIC_VOLUME = 0.5f;
	private static final int   DEFAULT_TEXTURE_MODE = 2;

	private final Preferences diskStore = Preferences.userNodeForPackage(getClass()).node(PREFERENCES_NODE);

	public SettingsMenu(Audio audio) {
		//pull preferences from disk store
		float musicVolume = Float.parseFloat(diskStore.get(KEY_MUSIC_VOLUME, String.valueOf(DEFAULT_MUSIC_VOLUME)));
		int   textureMode = Integer.parseInt(diskStore.get(KEY_TEXTURE_MODE, String.valueOf(DEFAULT_TEXTURE_MODE)));

		add(new TextBox(context.renderer.font, "Settings", 0, -175));

		add(new TextBox(context.renderer.font, "Music Volume", -180, -125));
		add(new Slider(context.renderer.font, 60, -125, 340, 10, musicVolume, (value) -> {
			audio.setVolume(Audio.VOL_MUSIC, PApplet.pow(value, 0.25f));
			diskStore.put(KEY_MUSIC_VOLUME, String.valueOf(value));
		}));

		add(new Spinner(context.renderer.font, new String[] { "Raw Colors         [ fastest ]",
		                                                      "Color Corrected    [ faster  ]",
		                                                      "Unmoving Textures  [ slower  ]",
		                                                      "Moving Textures    [ slowest ]" }, textureMode, 0, -60, 400, 50, (value) -> {
			context.renderer.textureOption = value;
			diskStore.put(KEY_TEXTURE_MODE, String.valueOf(value));
		}));

		add(new Button(context.renderer.font, "Key Bindings", 0,  20, 400, 50, (down) -> { context.setGameState(LD31.ST_KEYBIND); }));
		add(new Button(context.renderer.font, "Back"        , 0, 120, 400, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
	}
}
