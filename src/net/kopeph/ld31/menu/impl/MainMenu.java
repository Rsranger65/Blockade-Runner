package net.kopeph.ld31.menu.impl;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.Button;
import net.kopeph.ld31.menu.TextBox;

public final class MainMenu extends Menu {
	public MainMenu() {
		add(new TextBox(context.renderer.font,  "Blockade Runner", 0, -175));
		add(new Button(context.renderer.font, "Free Play"    , 0, -100, 400, 50, (down) -> { context.setGameState(LD31.ST_FREE_PLAY); }));
		add(new Button(context.renderer.font, "Campaign Mode", 0, - 40, 400, 50, (down) -> { context.setGameState(LD31.ST_CAMPAIGN); }));
		add(new Button(context.renderer.font, "Settings"     , 0, + 20, 400, 50, (down) -> { context.setGameState(LD31.ST_SETTINGS); }));
		add(new Button(context.renderer.font, "Exit"         , 0, +120, 400, 50, (down) -> { context.exit(); }));
	}
}
