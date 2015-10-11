package net.kopeph.ld31.menu.instances;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.TextBox;

public final class PauseMenu extends Menu {
	private static final int MAX_MENUHEIGHT = 320;
	private static final int PAUSE_MENU_WIDTH = 240;

	public int menuHeight;

	public PauseMenu() {
		add(new TextBox(context.renderer.font, "Game Paused", 0, -200));
		add(new MenuButton(context.renderer.font, "Resume Playing"     , 0,  -120, 200, 50, (down) -> { context.setGameState(LD31.ST_RUNNING); }));
		add(new MenuButton(context.renderer.font, "Reset"              , 0,   -50, 200, 50, (down) -> { context.setGameState(LD31.ST_RESET); }));
		add(new MenuButton(context.renderer.font, "Return to Main Menu", 0,    50, 200, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
		add(new MenuButton(context.renderer.font, "Quit Game"          , 0,   120, 200, 50, (down) -> { context.exit(); }));
	}

	@Override
	public void render() {
		context.updatePixels(); //doing this every frame allows us to draw the pause menu over a still of the game

		menuHeight += 20; //menu expands vertically from height of 0 to MAX_MENUHEIGHT
		if(menuHeight > MAX_MENUHEIGHT) menuHeight = MAX_MENUHEIGHT;

		//since the pause menu is of irregular dimensions, we're taking manual control
		setBounds(PAUSE_MENU_WIDTH, menuHeight); //Manual override! Get to your battle stations!
		if (menuHeight > MAX_MENUHEIGHT - 40)
			super.render(); //only render the menu (with buttons) if the menu height is enough that the buttons won't be off the menu
		else
			renderBack(); //otherwise, draw only the menu's background rectangle
	}
}
