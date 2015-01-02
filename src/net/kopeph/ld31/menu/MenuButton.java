package net.kopeph.ld31.menu;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.context.GraphicsContext;
import net.kopeph.ld31.spi.Interaction;

public class MenuButton extends TextBox {
	private static final int
		PRESSED = 0xEEEEEE,
		HOVERED = 0x333333,
		NORMAL  = 0x666666,
		ALPHA       = 0xCC;

	private Interaction interaction;
	private boolean wasPressed;

	public MenuButton(Font font, String text, int yPos, int width, int height, Interaction interaction) {
		super(font, (GraphicsContext.getInstance().width() - width) / 2, yPos - height / 2, width, height, text);
		hAlign = true;
		vAlign = true;
		this.interaction = interaction;
	}

	@Override
	public void render() {
		if (wasPressed && !isPressed())
			interaction.interact();
		wasPressed = isPressed();

		ctx.fill(ctx.color(isPressed() ? PRESSED : isHovered() ? HOVERED : NORMAL, ALPHA));
		ctx.rect(xPos, yPos, width, height, 7);

		super.render();
	}
}
