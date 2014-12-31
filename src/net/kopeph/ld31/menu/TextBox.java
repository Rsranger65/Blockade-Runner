package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import processing.core.PApplet;

/**
 * @author stuntddude
 */
public class TextBox implements MenuWidget {
	protected final PApplet context;
	
	protected final String text;
	protected final Font font;
	protected final int xPos, yPos;

	public TextBox(Font font, String text, int xPos, int yPos) {
		context = LD31.getContext();
		
		this.font = font;
		this.text = text;
		this.xPos = xPos;
		this.yPos = yPos;
	}

	@Override
	public void render() {
		font.renderCentered(text, context.width/2 + xPos, context.height/2 + yPos);
	}
}
