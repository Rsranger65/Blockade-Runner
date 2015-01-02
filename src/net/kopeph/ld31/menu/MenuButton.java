package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.Interaction;

/**
 * @author stuntddude
 */
public class MenuButton extends TextBox {
	private static boolean wasPressed;

	Interaction interaction;

	/**
	 * @param text        the text to be displayed centered inside the button
	 * @param xPos        see TextBox constructor
	 * @param yPos        see TextBox constructor
	 * @param interaction method to be called when the button is clicked
	 */
	public MenuButton(Font font, String text, int xPos, int yPos, int width, int height, Interaction interaction) {
		super(font, text, xPos, yPos);

		this.xPos += (LD31.getContext().width - width) / 2;
		this.yPos += (LD31.getContext().height - height) / 2;
		this.width = width;
		this.height = height;
		this.xAnchor = ANCHOR_FILL;
		this.interaction = interaction;
	}

	@Override
	public void render() {
		updateBounds();

		if (!wasPressed && isMouseDownInside()) {
			interaction.interact();
			wasPressed = true;
		}
		if (!isMouseDown())
			wasPressed = false;

		context.fill(isHovered() ? 50 : 150, 200);
		context.rect((int)xPos, (int)yPos, (int)width, (int)height, 7);

		super.render(); //Button Text
	}
}
