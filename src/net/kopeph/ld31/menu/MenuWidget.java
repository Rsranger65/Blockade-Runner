package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import processing.core.PConstants;

/**
 * implemented by all visual menu elements
 * @author alexg
 */
public abstract class MenuWidget {
	protected final LD31 context = LD31.getContext();

	protected float xPos, yPos, width, height;
	public int tag; //Used for key binding

	private int curCtxWidth, curCtxHeight;

	public MenuWidget(float xPos, float yPos, float width, float height) {
		setBounds(xPos, yPos, width, height);
	}

	public void setBounds(float xPos, float yPos, float width, float height)  {
		updateBounds(); //clear out inconsistencies in curCtx* vars
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
	}

	private boolean contains(int x, int y) {
		return x > xPos && x < xPos + width && y > yPos && y < yPos + height;
	}

	protected boolean isHovered() {
		return contains(context.mouseX, context.mouseY);
	}

	protected boolean isMouseDown() {
		return context.mousePressed && (context.mouseButton == PConstants.LEFT);
	}

	protected void updateBounds() {
		int dCtxWidth = context.width - curCtxWidth;
		int dCtxHeight = context.height - curCtxHeight;
		curCtxWidth = context.width;
		curCtxHeight = context.height;

		xPos += dCtxWidth/2.0;
		yPos += dCtxHeight/2.0;
	}

	public abstract void render();
}
