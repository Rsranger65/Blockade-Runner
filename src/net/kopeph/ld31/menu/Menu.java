package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.LD31;

/**
 * @author stuntddude
 */
public class Menu extends MenuWidget {
	public static final int
		DEFAULT_WIDTH  = 600,
		DEFAULT_HEIGHT = 400;

	private List<MenuWidget> widgets = new ArrayList<>();

	public Menu() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

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
		xAnchor = ANCHOR_FILL;
		yAnchor = ANCHOR_FILL;
	}

	public void add(MenuWidget b) {
		widgets.add(b);
	}

	public MenuWidget getChild(int index) { //hide yo kids, hide yo wife
		return widgets.get(index);
	}

	public int getChildCount() {
		return widgets.size();
	}

	@Override
	public void render() {
		updateBounds();
		renderBack();
		for (MenuWidget w : widgets)
			w.render();
	}
	
	/** Draws the backing rectangle, should be called before drawing the MenuWidgets */
	public void renderBack() {
		context.pushStyle();
		context.fill(100, 200);
		context.rect((int)xPos, (int)yPos, (int)width, (int)height, 10);
		context.popStyle();
	}
}
