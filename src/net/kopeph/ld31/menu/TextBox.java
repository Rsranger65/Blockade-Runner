package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;

public class TextBox implements MenuWidget {
	private String text;
	private Font font;
	private int yPos;

	public TextBox(Font font, String text, int yPos) {
		this.font = font;
		this.text = text;
		this.yPos = yPos;
	}

	@Override
	public void render() {
		font.renderCentered(text, LD31.getContext().width/2, yPos);
	}
}
