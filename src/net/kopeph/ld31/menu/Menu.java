package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.LD31;

/**
 * @author alexg
 * @author stuntddude
 */
public class Menu extends Widget {
	public static final int
		DEFAULT_WIDTH  = 600,
		DEFAULT_HEIGHT = 400;

	private List<Widget> widgets = new ArrayList<>();

	public Menu() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public Menu(int width, int height) {
		super(0,0,0,0); //Dummy values
		setBounds(width, height);
	}

	protected void setBounds(int width, int height) {
		setBounds((LD31.getContext().width - width) / 2,
		          (LD31.getContext().height - height) / 2,
		          width, height);
	}

	protected void add(Widget b) {
		widgets.add(b);
	}

	@Override
	public void render() {
		updateBounds();
		renderBack();
		for (Widget w : widgets)
			w.render();
	}

	/** Draws the backing rectangle, should be called before drawing the MenuWidgets */
	protected void renderBack() {
		context.pushStyle();
		context.fill(100, 200);
		context.rect((int)xPos, (int)yPos, (int)width, (int)height, 10);
		context.popStyle();
	}
}
