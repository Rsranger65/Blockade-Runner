package net.kopeph.ld31.menu;

import net.kopeph.ld31.entity.Renderable;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PConstants;

public abstract class MenuWidget implements Renderable {
	protected final PApplet context;
	protected int xPos, yPos, width, height;

	public MenuWidget(PApplet context, int xPos, int yPos, int width, int height) {
		this.context = context;
		setBounds(xPos, yPos, width, height);
	}

	public void setBounds(int xPos, int yPos, int width, int height)  {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
	}


	public boolean isHovered() {
		return Util.boxContains(xPos, yPos, width, height, context.mouseX, context.mouseY);
	}

	public boolean isPressed() {
		return isHovered() && context.mousePressed && (context.mouseButton == PConstants.LEFT);
	}
}
