package net.kopeph.ld31.menu.instances;

import java.util.List;

import net.kopeph.ld31.InputHandler;
import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.TextBox;

public final class KeyBindingsMenu extends Menu {
	private static final int BINDINGS_YPOS = -140;

	public KeyBindingsMenu(InputHandler input) {
		add(new TextBox(context.renderer.font, "Key Bindings", 0, -175));

		//setup key binding buttons and labels
		final int MENU_COLS = 4; //1 label + MENU_COLS buttons per row
		for (int row = 0; row < InputHandler.CTL_ESCAPE; ++row) {
			//setup left label column
			add(new TextBox(context.renderer.font, InputHandler.getControlString(row), -40*MENU_COLS, BINDINGS_YPOS + 32*row));

			//setup each row of buttons
			final int r = row;
			List<Integer> bindings = input.getBoundKeyIdsFor(row);
			for (int col = 1; col < MENU_COLS; ++col) {
				final int keyId = (col <= bindings.size()? bindings.get(col - 1) : InputHandler.K_UNBOUND);
				final MenuButton b = new MenuButton(context.renderer.font, InputHandler.getKeyIdString(keyId),
				                                    -40*MENU_COLS + 80*col, BINDINGS_YPOS + 32*row, 70, 24, (down) -> { /* dummy argument (gets replaced immediately) */ });
				b.replaceInteraction((down) -> { input.handleBind(b, r); }); //working around the strict lambda capture requirements
				b.tag = keyId;
				add(b);
			}
		}

		add(new MenuButton(context.renderer.font, "Revert to Defaults", 0, 70, 400, 50, (down) -> { input.resetKeyIdBindings(); context.setupKeyBindingsMenu(); }));
		add(new MenuButton(context.renderer.font, "Back", 0, 150, 400, 50, (down) -> { context.setGameState(LD31.ST_SETTINGS); }));
	}
}
