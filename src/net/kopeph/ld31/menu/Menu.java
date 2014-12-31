package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.Renderable;
import processing.core.PApplet;
import processing.core.PConstants;

public class Menu implements Renderable {
	private final PApplet context = LD31.getContext();
	private final Font font;
	private final String title;

	private List<MenuWidget> widgets = new ArrayList<>();

	public Menu(Font font, String title) {
		this.font = font;
		this.title = title;
	}

	public void add(MenuWidget b) {
		widgets.add(b);
	}

	@Override
	public void render() {
		context.fill(100, 200);
		context.pushStyle();
		context.rectMode(PConstants.CENTER);
		context.rect(context.width/2, context.height/2, context.width - 200, context.height - 200, 10);
		context.popStyle();

		font.renderCentered(title, context.width / 2, 125);

		for (MenuWidget w : widgets)
			w.render();
	}
}
