package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import processing.core.PApplet;

/**
 * @author alexg
 */
public class EndScreen {
	private static final int SWEEP_SPEED = 8;
	private static final int DARKEN_SPEED = 255;

	private final PApplet context = LD31.getContext();
	private final Font font;
	private final String title, footer;
	private final int backColor;
	private int phase;

	public EndScreen(Font font, String title, String footer, int backColor) {
		this.font = font;
		this.title = title;
		this.footer = footer;
		this.backColor = backColor;
	}

	public void render() {
		++phase;

		if (phase > 0) {
			context.fill(backColor);
			context.rect(0, context.height/2 - phase*SWEEP_SPEED, context.width, 2*phase*SWEEP_SPEED);
			font.render(title, context.width/2, context.height/2);
			font.render(footer, 8, context.height - 16);

			if (phase * SWEEP_SPEED >= context.height / 2)
				phase = -DARKEN_SPEED;
		}
		else if (phase < 0) {
			context.background(context.red  (backColor)*phase/-DARKEN_SPEED,
							   context.green(backColor)*phase/-DARKEN_SPEED,
					           context.blue (backColor)*phase/-DARKEN_SPEED);
			font.render(title, context.width/2, context.height/2);
			font.render(footer, 8, context.height - 16);
		}
		else {
			context.noLoop();
		}
	}
}

