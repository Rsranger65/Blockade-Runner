package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.Interaction;

public class MenuButton extends TextBox {
	private static boolean wasPressed; //Stop presses from propagating while mouse is held down

	private Interaction interaction;

	public MenuButton(Font font, String text, int yPos, int width, int height, Interaction interaction) {
		super(font, (LD31.getContext().width - width) / 2, yPos - height / 2, width, height, text);
		hAlign = true;
		vAlign = true;
		this.interaction = interaction;
	}

	@Override
	public void render() {
		if (!wasPressed && isMouseDownInside()) {
			interaction.interact();
			wasPressed = true;
		}
		if (!isMouseDown())
			wasPressed = false;

		context.fill(isHovered() ? 50 : 150, 200);
		context.rect(xPos, yPos, width, height, 7);

		super.render(); //Button Text
	}
}
