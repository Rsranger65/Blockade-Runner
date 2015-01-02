package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.context.GraphicsContext;
import net.kopeph.ld31.spi.Renderable;

public class Menu implements Renderable {
	private final GraphicsContext ctx = GraphicsContext.getInstance();
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
		ctx.fill(ctx.color(100, 100, 100, 200));
		ctx.rect(100, 100, ctx.width() - 200, ctx.height() - 200, 10);
		font.renderCentered(title, ctx.width() / 2, 125);

		for (MenuWidget w : widgets)
			w.render();
	}
}
