package net.kopeph.ld31.menu.instances;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.Button;
import net.kopeph.ld31.menu.TextBox;

public final class FreePlayMenu extends Menu {
	public FreePlayMenu() {
		add(new TextBox(context.renderer.font, "Free Play Mode", 0, -175));
		add(new Button(context.renderer.font, "Tiny (480x340)"   , 0, -130, 400, 40, (down) -> { context.setLevelSize(480, 340);   context.setGameState(LD31.ST_RESET_HARD); context.setLevelPath(null); }));
		add(new Button(context.renderer.font, "Small (800x600)"  , 0,  -80, 400, 40, (down) -> { context.setLevelSize(800, 600);   context.setGameState(LD31.ST_RESET_HARD); context.setLevelPath(null); }));
		add(new Button(context.renderer.font, "Medium (1280x720)", 0,  -30, 400, 40, (down) -> { context.setLevelSize(1280, 720);  context.setGameState(LD31.ST_RESET_HARD); context.setLevelPath(null); }));
		add(new Button(context.renderer.font, "Large (1920x1080)", 0,   20, 400, 40, (down) -> { context.setLevelSize(1920, 1080); context.setGameState(LD31.ST_RESET_HARD); context.setLevelPath(null); }));
		add(new Button(context.renderer.font, "Huge (2560x1440)" , 0,   70, 400, 40, (down) -> { context.setLevelSize(2560, 1440); context.setGameState(LD31.ST_RESET_HARD); context.setLevelPath(null); }));
		add(new Button(context.renderer.font, "Back"             , 0,  150, 400, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
	}
}
