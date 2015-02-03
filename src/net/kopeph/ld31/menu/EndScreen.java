package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.Renderable;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * @author alexg
 */
public class EndScreen implements Renderable {
	private static final int PHASE_ONE = 200;
	private static final int PHASE_TWO = 160;

	private final PApplet context = LD31.getContext();
	private final Font font;
	public String title, footer;
	private final int backColor;
	private int phase;
	private int lastFrame;

	public EndScreen(Font font, String title, String footer, int backColor) {
		this.font = font;
		this.title = title;
		this.footer = footer;
		this.backColor = backColor;
	}

	public void render() {
		//keeping track of the phase
		if (lastFrame < context.frameCount - 1)
			phase = PHASE_ONE; //reset
		if (phase > 0)
			--phase;
		lastFrame = context.frameCount;
		
		context.pushStyle();
		
		context.rectMode(PConstants.CENTER);
		context.fill(phase * context.red  (backColor) / PHASE_TWO,
					 phase * context.green(backColor) / PHASE_TWO,
					 phase * context.blue (backColor) / PHASE_TWO);
		context.rect(context.width/2, context.height/2, context.width,
					(PHASE_ONE - phase) * context.height / (PHASE_ONE - PHASE_TWO));
		font.renderCentered(title, context.width/2, context.height/2);
		font.render(footer, 8, context.height - 16);
		
		context.popStyle();
	}
}

