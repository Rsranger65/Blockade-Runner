package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.graphics.Font;
import processing.core.PConstants;

public class MenuButton implements MenuWidget {
	private final LD31 context;
	
	int yPos, width, height;
	String text;
	Font font;
	
	Interaction interaction;
	
	public MenuButton(Font font, String text, int yPos, int width, int height, Interaction interaction) {
		this.context = LD31.getContext();
		this.font = font;
		
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.text = text;
		
		this.interaction = interaction;
	}
	
	public void render() {
		boolean isActive = (context.mouseX > context.width/2 - width/2 &&
							context.mouseX < context.width/2 + width/2 &&
							context.mouseY > yPos - height/2 &&
							context.mouseY < yPos + height/2);
		
		if (isActive && context.interacting) //this is probably bad practice but I'm leaving it for now
			interaction.interact();
		
		context.fill((isActive? 50 : 150), 200);
		context.rectMode(PConstants.CENTER);
		context.rect(context.width/2, yPos, width, height, 7);
		font.renderCentered(text, context.width/2, yPos);
	}
}
