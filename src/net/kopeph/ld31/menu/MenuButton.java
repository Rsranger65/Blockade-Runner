package net.kopeph.ld31.menu;

import net.kopeph.ld31.graphics.Font;
import processing.core.PApplet;

public class MenuButton extends TextBox {
	private static final int
		BRT_PRESSED = 250,
		BRT_HOVERED =  50,
		BRT_NORMAL  = 150,
		ALPHA       = 200;

	Interaction interaction;
	boolean wasPressed;

	public MenuButton(PApplet context, Font font, String text, int yPos, int width, int height, Interaction interaction) {
		super(context, font, (context.width - width) / 2, yPos - height / 2, width, height, text);
		hAlign = true;
		vAlign = true;
		this.interaction = interaction;
	}

	@Override
	public void render() {
		if (wasPressed && !isPressed())
			interaction.interact();
		wasPressed = isPressed();

		context.fill(isPressed() ? BRT_PRESSED :
			         isHovered() ? BRT_HOVERED :
			        	           BRT_NORMAL, ALPHA);
		context.rect(xPos, yPos, width, height, 7);

		super.render();
	}
}
