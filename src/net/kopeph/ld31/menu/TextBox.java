package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;

/**
 * @author alexg, stuntddude
 */
public class TextBox extends MenuWidget {
	private final Font font;
	public String text;
	public boolean hAlign, vAlign;

	public TextBox(Font font, int x, int y, int w, int h) {
		this(font, x, y, w, h, ""); //$NON-NLS-1$
	}

	public TextBox(Font font, int x, int y, int w, int h, String text) {
		super(x, y, w, h);
		this.font = font;
		this.text = text;
	}

	//This is sort of a hack for merging purposes
	public TextBox(Font font, String text, int xPos, int yPos) {
		this(font, xPos, yPos, LD31.getContext().width, LD31.getContext().height, text);
		hAlign = true;
		vAlign = true;
	}

	@Override
	public void render() {
		font.render(text, xPos, yPos, width, height, hAlign, vAlign);
	}
}
