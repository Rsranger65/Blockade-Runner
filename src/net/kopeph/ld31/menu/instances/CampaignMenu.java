package net.kopeph.ld31.menu.instances;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.TextBox;

public final class CampaignMenu extends Menu {
	public CampaignMenu() {
		super();
		add(new TextBox(context.renderer.font, "Campaign Mode", 0, -175));
		add(new MenuButton(context.renderer.font, "Play Test Level", 0, -100, 400, 50, (down) -> { context.setGameState(LD31.ST_RESET_HARD); context.setLevelPath(LD31.TEST_LEVEL); }));
		add(new MenuButton(context.renderer.font, "Back"           , 0,  -40, 400, 50, (down) -> { context.setGameState(LD31.ST_MENU); }));
		add(new TextBox(context.renderer.font, "This game mode is still in early development!", 0,  150));
	}
}
