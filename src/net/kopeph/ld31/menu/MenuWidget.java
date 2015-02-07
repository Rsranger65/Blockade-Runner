package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Renderable;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PConstants;

/** implemented by all visual elements */
public abstract class MenuWidget implements Renderable {
	public static final int
		ANCHOR_CENTER = 0,                    //Move origin by delta/2
		ANCHOR_LEFT   = 1, ANCHOR_TOP    = 1, //Move nothing
		ANCHOR_RIGHT  = 2, ANCHOR_BOTTOM = 2, //Move origin by delta
		ANCHOR_FILL   = 3;                    //Move size by delta

	protected final PApplet context = LD31.getContext();

	public float xPos, yPos, width, height;
	public int xAnchor = ANCHOR_LEFT, yAnchor = ANCHOR_TOP;
	public String tag; //General purpose

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

	public boolean isHovered() {
		return Util.boxContains(xPos, yPos, width, height, context.mouseX, context.mouseY);
	}

	public boolean isMouseDown() {
		return context.mousePressed && (context.mouseButton == PConstants.LEFT);
	}

	public boolean isMouseDownInside() {
		return isMouseDown() && isHovered();
	}

	protected void updateBounds() {
		int dCtxWidth = context.width - curCtxWidth;
		int dCtxHeight = context.height - curCtxHeight;
		curCtxWidth = context.width;
		curCtxHeight = context.height;

		switch(xAnchor) {
		case ANCHOR_CENTER: xPos += dCtxWidth/2.0; break;
		case ANCHOR_LEFT: 						   break;
		case ANCHOR_RIGHT:  xPos += dCtxWidth; 	   break;
		case ANCHOR_FILL:  width += dCtxWidth; 	   break;
		}

		switch(yAnchor) {
		case ANCHOR_CENTER: yPos += dCtxHeight/2.0; break;
		case ANCHOR_TOP: 						    break;
		case ANCHOR_BOTTOM: yPos += dCtxHeight;     break;
		case ANCHOR_FILL: height += dCtxHeight;     break;
		}
	}

	@Override
	public abstract void render();
}
