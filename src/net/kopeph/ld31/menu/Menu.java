package net.kopeph.ld31.menu;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.LD31;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * @author stuntddude
 */
public class Menu {
	public static final int
		DEFAULT_WIDTH  = 600,
		DEFAULT_HEIGHT = 400;
	
	private final PApplet context;
	
	private int width, height;
	
	List<MenuWidget> widgets = new ArrayList<>();
	
	public Menu(int width, int height) {
		context = LD31.getContext();
		setDimensions(width, height);
	}
	
	public void setDimensions(int w, int h) {
		width = w;
		height = h;
	}
	
	public void add(MenuWidget b) {
		widgets.add(b);
	}
	
	public void render() {
		context.pushStyle();
		
		context.fill(100, 200);
		context.rectMode(PConstants.CENTER);
		context.rect(context.width/2, context.height/2, width, height, 10);
		
		for (MenuWidget w : widgets)
			w.render();
		
		context.popStyle();
	}
}
