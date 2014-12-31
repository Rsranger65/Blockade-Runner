package net.kopeph.ld31.graphics;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author alexg
 */
public class SpriteSheet {
	private final PApplet context = LD31.getContext();
	private List<PImage> splitImages = new ArrayList<>();

	public SpriteSheet(String filename, int cellsX, int cellsY) {
		init(context.loadImage(filename), cellsX, cellsY);
	}

	public SpriteSheet(PImage sheet, int cellsX, int cellsY) {
		init(sheet, cellsX, cellsY);
	}

	private void init(PImage sheet, int cellsX, int cellsY) {
		int width = sheet.width / cellsX;
		int height = sheet.height / cellsY;

		//Splice up the image into a bunch of little ones
		for (int y = 0; y < cellsY; y++) {
			for (int x = 0; x < cellsX; x++) {
				PImage cell = Util.crop(context, sheet, x * width, y * height, width, height);
				splitImages.add(cell);
			}
		}
	}

	/**
	 * @param imageId resource ID from loadImage()
	 * @param cellId numbering left to right, top to bottom, which cell in the sheet to render
	 * @param x render location X
	 * @param y render location Y
	 */
	public void render(int cellId, int x, int y) {
		context.image(splitImages.get(cellId), x, y);
	}
}
