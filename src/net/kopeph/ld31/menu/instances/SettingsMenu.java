package net.kopeph.ld31.menu.instances;

import java.util.List;

import net.kopeph.ld31.Audio;
import net.kopeph.ld31.InputHandler;
import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.Slider;
import net.kopeph.ld31.menu.TextBox;
import processing.core.PApplet;

public final class SettingsMenu extends Menu {
	private static final int BINDINGS_YPOS = -150;

	public SettingsMenu(int width, int height, InputHandler input, Audio audio) {
		super(width, height);

		add(new TextBox(context.renderer.font, "Settings Menu", 0, -225));

		//setup volume slider
		add(new TextBox(context.renderer.font, "Volume", -180, -175));
		add(new Slider(context.renderer.font, 40, -175, 360, 10, 0.5f, (value) -> { audio.setVolume(Audio.VOL_MUSIC, PApplet.pow(value, 0.25f)); }));

		//setup key binding buttons and labels
		final int MENU_COLS = 4; //1 label + MENU_COLS buttons per row
		//setup top label row
		for (int col = 1; col < MENU_COLS; ++col) {
			add(new TextBox(context.renderer.font, String.valueOf(col), -30*MENU_COLS + 60*col, BINDINGS_YPOS + 25));
		}
		for (int row = 0; row < InputHandler.CTL_ESCAPE; ++row) {
			//setup left label column
			add(new TextBox(context.renderer.font, InputHandler.getControlString(row), -30*MENU_COLS, BINDINGS_YPOS + 50 + 30*row));

			//setup each row of buttons
			final int r = row;
			List<Integer> bindings = input.getBoundKeyIdsFor(row);
			for (int col = 1; col < MENU_COLS; ++col) {
				final int keyId = (col <= bindings.size()? bindings.get(col - 1) : InputHandler.K_UNBOUND);
				final MenuButton b = new MenuButton(context.renderer.font, InputHandler.getKeyIdString(keyId),
				                                    -30*MENU_COLS + 60*col, BINDINGS_YPOS + 50 + 30*row, 50, 20, (down) -> { /* dummy argument (gets replaced immediately) */ });
				b.replaceInteraction((down) -> { input.handleBind(b, r); }); //working around the strict lambda capture requirements
				b.tag = keyId;
				add(b);
			}
		}

		add(new MenuButton(context.renderer.font, "Revert to Defaults", 0, 100, 400, 50, (down) -> { input.resetKeyIdBindings(); context.setupSettingsMenu(); }));
		add(new MenuButton(context.renderer.font, "Back", 0, 200, 400, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
	}
}
