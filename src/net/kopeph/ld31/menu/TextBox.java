package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;

/**
 * @author alexg
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

	public TextBox(Font font, String text, int yPos) {
		this(font, 0, yPos, LD31.getContext().width, LD31.getContext().height, text);
		hAlign = true;
	}

	public void render() {
		font.render(text, xPos, yPos, width, height, hAlign, vAlign);
	}
}
