package net.kopeph.ld31.graphics;

import java.util.ArrayList;
import java.util.List;

import net.kopeph.ld31.graphics.context.ContextImage;
import net.kopeph.ld31.graphics.context.GraphicsContext;

/**
 *
 * @author alexg
 */
public class SpriteSheet {
	private final GraphicsContext ctx = GraphicsContext.getInstance();
	private List<ContextImage> splitImages = new ArrayList<>();

	public SpriteSheet(String filename, int cellsX, int cellsY) {
		init(ctx.loadImage(filename), cellsX, cellsY);
	}

	public SpriteSheet(ContextImage sheet, int cellsX, int cellsY) {
		init(sheet, cellsX, cellsY);
	}

	private void init(ContextImage sheet, int cellsX, int cellsY) {
		int width = sheet.width() / cellsX;
		int height = sheet.height() / cellsY;

		//Splice up the image into a bunch of little ones
		for (int y = 0; y < cellsY; y++) {
			for (int x = 0; x < cellsX; x++) {
				ContextImage cell = sheet.crop(x * width, y * height, width, height);
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
		ctx.image(splitImages.get(cellId), x, y);
	}
}
