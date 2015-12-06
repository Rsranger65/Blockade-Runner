package net.kopeph.ld31.menu;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.SliderHandler;
import processing.core.PApplet;

/**
 * A mouse-interactive horizontal slider
 * @author stuntddude
 */
public class Slider extends Widget {
	private static final int BUTTON_WIDTH = 25;
	private static final int BUTTON_HEIGHT = 50;

	private final Button button;
	private final SliderHandler op;
	private boolean isHeld;

	/** @param value a number from 0 to 1 determining the initial position of the button */
	public Slider(Font font, int x, int y, int width, int height, float value, SliderHandler functionality) {
		super(x, y, width, height);

		this.xPos += (context.width - width) / 2;
		this.yPos += (context.height - height) / 2;

		button = new Button(font, "", (value - 0.5f)*(width - BUTTON_WIDTH) + x, y, BUTTON_WIDTH, BUTTON_HEIGHT, (down) -> { isHeld = true; });
		op = functionality;
		op.update(value);
	}

	@Override
	public void render() {
		updateBounds();

		context.fill(50, 200);
		context.rect((int)xPos, (int)yPos, (int)width, (int)height, 1);

		//allow the user to click and drag to move the button
		if (isHeld) {
			button.setBounds(
					PApplet.min(xPos + width - BUTTON_WIDTH, PApplet.max(xPos, context.mouseX - BUTTON_WIDTH/2)),
					yPos + height/2 - BUTTON_HEIGHT/2,
					BUTTON_WIDTH, BUTTON_HEIGHT);

			op.update(value());

			if (!context.mousePressed)
				isHeld = false;
		}

		button.render();
	}

	public float value() {
		return (button.xPos - xPos)/(width - BUTTON_WIDTH);
	}
}
