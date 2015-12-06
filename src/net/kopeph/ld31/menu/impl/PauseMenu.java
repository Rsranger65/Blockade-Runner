package net.kopeph.ld31.menu.impl;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Button;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.TextBox;
import processing.core.PApplet;

public final class PauseMenu extends Menu {
	private static final int MAX_MENUHEIGHT = 320;
	private static final int PAUSE_MENU_WIDTH = 240;

	private int menuHeight;
	private int lastFrame;

	public PauseMenu() {
		add(new TextBox(context.renderer.font, "Game Paused", 0, -200));
		add(new Button(context.renderer.font, "Resume Playing"     , 0,  -120, 200, 50, (down) -> { context.setGameState(LD31.ST_RUNNING); }));
		add(new Button(context.renderer.font, "Reset"              , 0,   -60, 200, 50, (down) -> { context.setGameState(LD31.ST_RESET); }));
		add(new Button(context.renderer.font, "Settings Menu"      , 0,     0, 200, 50, (down) -> { context.setGameState(LD31.ST_SET_INGAME); }));
		add(new Button(context.renderer.font, "Return to Main Menu", 0,    60, 200, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
		add(new Button(context.renderer.font, "Quit Game"          , 0,   120, 200, 50, (down) -> { context.exit(); }));
	}

	@Override
	public void render() {
		if (lastFrame < context.frameCount - 1)
			menuHeight = 10; //reset only if we weren't rendered last frame
		lastFrame = context.frameCount;

		context.updatePixels(); //doing this every frame allows us to draw the pause menu over a still of the game
		menuHeight = PApplet.min(MAX_MENUHEIGHT, menuHeight + 20); //menu expands vertically from height of 0 to MAX_MENUHEIGHT

		//since the pause menu is of irregular dimensions, we're taking manual control
		setBounds(PAUSE_MENU_WIDTH, menuHeight); //Manual override! Get to your battle stations!
		if (menuHeight > MAX_MENUHEIGHT - 40)
			super.render(); //only render the title and buttons if the menu height is enough that the buttons won't be off the menu
		else
			renderBack(); //otherwise, draw only the menu's background rectangle
	}
}
