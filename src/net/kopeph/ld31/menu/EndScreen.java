package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.HUD;
import net.kopeph.ld31.graphics.Renderable;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * @author alexg
 * @author stuntddude
 */
public class EndScreen implements Renderable {
	private static final int PHASE_ONE = 200;
	private static final int PHASE_TWO = 160;

	private final PApplet context = LD31.getContext();
	private final Font font;
	public String title;
	private final int backColor;
	private int phase;
	private int lastFrame;

	public EndScreen(Font font, String title, int backColor) {
		this.font = font;
		this.title = title;
		this.backColor = backColor;
	}

	@Override
	public void render() {
		//keeping track of the phase
		if (lastFrame < context.frameCount - 1)
			phase = PHASE_ONE; //reset only if we weren't rendered last frame
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

		context.popStyle();
		
		HUD.render();
	}
}

