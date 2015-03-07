package net.kopeph.ld31.menu;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.Interaction;

/** @author stuntddude */
public class MenuButton extends TextBox {
	private static boolean wasPressed;

	Interaction interaction;

	/**
	 * @param text        the text to be displayed centered inside the button
	 * @param xPos        see TextBox constructor
	 * @param yPos        see TextBox constructor
	 * @param interaction method to be called when the button is clicked
	 */
	public MenuButton(Font font, String text, float xPos, float yPos, float width, float height, Interaction interaction) {
		super(font, text, xPos, yPos);

		this.xPos += (context.width - width) / 2;
		this.yPos += (context.height - height) / 2;
		this.width = width;
		this.height = height;
		this.interaction = interaction;
	}
	
	/** Replaces the previous functionality with the supplied functionality */
	public void replaceInteraction(Interaction replacement) {
		interaction = replacement;
	}

	@Override
	public void render() {
		updateBounds();

		if (!wasPressed && isHovered() && isMouseDown()) {
			interaction.interact(true);
			wasPressed = true;
		}
		if (!isMouseDown())
			wasPressed = false;

		context.fill(isHovered() ? 50 : 150, 200);
		context.rect((int)xPos, (int)yPos, (int)width, (int)height, 7);

		super.render(); //Button Text
	}
}
