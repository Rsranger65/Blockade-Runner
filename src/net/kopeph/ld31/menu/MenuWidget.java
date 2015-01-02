package net.kopeph.ld31.menu;

import net.kopeph.ld31.graphics.context.GraphicsContext;
import net.kopeph.ld31.spi.Renderable;
import net.kopeph.ld31.util.Util;

public abstract class MenuWidget implements Renderable {
	protected final GraphicsContext ctx = GraphicsContext.getInstance();
	protected int xPos, yPos, width, height;

	public MenuWidget(int xPos, int yPos, int width, int height) {
		setBounds(xPos, yPos, width, height);
	}

	public void setBounds(int xPos, int yPos, int width, int height)  {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
	}


	public boolean isHovered() {
		return Util.boxContains(xPos, yPos, width, height, ctx.mouseX(), ctx.mouseY());
	}

	public boolean isPressed() {
		return isHovered() && ctx.mousePressed() && (ctx.mouseLeftButton());
	}
}
