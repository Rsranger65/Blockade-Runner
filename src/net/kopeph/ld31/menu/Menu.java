package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.entity.Renderable;
import net.kopeph.ld31.graphics.Font;
import processing.core.PApplet;
import processing.core.PConstants;

public class Menu implements Renderable {
	private final PApplet context;
	private final Font font;
	private final String title;

	List<MenuWidget> widgets = new ArrayList<>();

	public Menu(PApplet context, Font font, String title) {
		this.context = context;
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
