package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.LD31;
import processing.core.PApplet;

public class Menu extends MenuWidget {
	private final PApplet context = LD31.getContext();

	private List<MenuWidget> widgets = new ArrayList<>();

	public Menu(int width, int height) {
		super(0,0,0,0); //Dummy values
		setBounds(width, height);
	}

	public Menu(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public void setBounds(int width, int height) {
		setBounds((LD31.getContext().getWidth() - width) / 2,
			      (LD31.getContext().getHeight() - height) / 2,
			       width, height);
	}

	public void add(MenuWidget b) {
		widgets.add(b);
	}

	public void render() {
		context.pushStyle();

		context.fill(100, 200);
		context.rect(xPos, yPos, width, height, 10);

		for (MenuWidget w : widgets)
			w.render();

		context.popStyle();
	}
}
