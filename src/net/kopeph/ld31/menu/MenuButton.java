package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import processing.core.PConstants;

/**
 * @author stuntddude
 */
public class MenuButton extends TextBox {
	int width, height;
	Interaction interaction;
	
	/**
	 * @param text        the text to be displayed centered inside the button
	 * @param xPos        see TextBox constructor
	 * @param yPos        see TextBox constructor
	 * @param interaction method to be called when the button is clicked
	 */
	public MenuButton(Font font, String text, int xPos, int yPos, int width, int height, Interaction interaction) {
		super(font, text, xPos, yPos);
		
		this.width = width;
		this.height = height;
		this.interaction = interaction;
	}
	
	public void render() {
		int x = context.width/2 + xPos;
		int y = context.height/2 + yPos;
		
		boolean isActive = (context.mouseX > x - width  / 2 &&
							context.mouseX < x + width  / 2 &&
							context.mouseY > y - height / 2 &&
							context.mouseY < y + height / 2);
		
		if (isActive && ((LD31)context).interacting) //this is probably really bad practice, will fix later
			interaction.interact();
		
		context.fill((isActive? 50 : 150), 200);
		context.rectMode(PConstants.CENTER);
		context.rect(context.width/2, context.height/2 + yPos, width, height, 7);
		super.render(); //display the text inside the button
	}
}
