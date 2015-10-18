package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;

/** @author alexg */
public class TextBox extends Widget {
	private final Font font;
	public String text;

	public TextBox(Font font, float x, float y, float w, float h, String text) {
		super(x, y, w, h);
		this.font = font;
		this.text = text;
	}

	//This is sort of a hack for merging purposes
	public TextBox(Font font, String text, float xPos, float yPos) {
		this(font, xPos, yPos, LD31.getContext().width, LD31.getContext().height, text);
	}

	@Override
	public void render() {
		updateBounds();
		font.renderCentered(text, (int)(xPos + width/2), (int)(yPos + height/2));
	}
}
