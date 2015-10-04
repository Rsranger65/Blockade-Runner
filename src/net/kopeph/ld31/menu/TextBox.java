package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;

/**
 * @author alexg
 */
public class TextBox extends MenuWidget {
	private final Font font;
	public String text;
	public boolean xCenter, yCenter;

	public TextBox(Font font, float x, float y, float w, float h, String text) {
		super(x, y, w, h);
		this.font = font;
		this.text = text;
	}

	//This is sort of a hack for merging purposes
	public TextBox(Font font, String text, float xPos, float yPos) {
		this(font, xPos, yPos, LD31.getContext().width, LD31.getContext().height, text);
		xCenter = true;
		yCenter = true;
		xAnchor = ANCHOR_CENTER;
		yAnchor = ANCHOR_CENTER;
	}

	@Override
	public void render() {
		updateBounds();
		font.render(text, (int)xPos, (int)yPos, (int)width, (int)height, xCenter, yCenter);
	}
}
