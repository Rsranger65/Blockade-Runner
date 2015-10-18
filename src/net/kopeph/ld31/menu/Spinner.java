package net.kopeph.ld31.menu;

import net.kopeph.ld31.InputHandler;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.SpinnerHandler;

public class Spinner extends TextBox {
	private static final int BUTTON_WIDTH = 40; //width of left and right buttons in pixels

	private final Button left, right;
	private final String[] states;
	private final SpinnerHandler op;
	private int state = 0;

	public Spinner(Font font, String[] states, int state, float xPos, float yPos, float width, float height, SpinnerHandler functionality) {
		super(font, states[state], xPos, yPos);
		this.states = states;
		this.state = state;
		op = functionality;
		op.update(state);

		left  = new Button(font, InputHandler.getKeyIdString(InputHandler.K_LEFT ), xPos - width/2 + BUTTON_WIDTH/2, yPos, BUTTON_WIDTH, height, (down) -> { spin(-1); });
		right = new Button(font, InputHandler.getKeyIdString(InputHandler.K_RIGHT), xPos + width/2 - BUTTON_WIDTH/2, yPos, BUTTON_WIDTH, height, (down) -> { spin( 1); });
	}

	private void spin(int dir) {
		text = states[state = Math.floorMod(state + dir, states.length)];
		op.update(state);
	}

	@Override
	public void render() {
		super.render();
		left.render();
		right.render();
	}

}
