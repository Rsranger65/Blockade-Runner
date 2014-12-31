/*
 * Copyright 2014 Alex Gittemeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kopeph.ld31.graphics;

import net.kopeph.ld31.LD31;
import processing.core.PImage;

/**
 *
 * @author alexg
 */
public class Font {
	private static final int
		G_DESCENDER = 0x10,
		P_DESCENDER = 0x11,
		Q_DESCENDER = 0x12,
		J_DESCENDER = 0x13;
	public static final int
		X_SIZE = 8, Y_SIZE = 8;
	private static final float LINE_SPACE = 1.5F;

	private final SpriteSheet sheet;

	public Font(String filename) {
		this(LD31.getContext().loadImage(filename));
	}

	public Font(PImage img) {
		sheet = new SpriteSheet(img, 16, 8);
	}

	/**
	 * Renders text with (x, y) describing the top-left corner of the text field.
	 *
	 * @param str
	 * @param x render location X
	 * @param y render location Y
	 */
	public void render(String str, int x, int y) {
		String[] lines = str.split("\r|\n"); //$NON-NLS-1$
		for (String line : lines) {
			renderLine(line, x, y);
		}
	}

	/**
	 * Renders text with (x,y) describing the center point of the text field.
	 *
	 * @param str
	 * @param x render location X
	 * @param y render location Y
	 */
	public void renderCentered(String str, int x, int y) {
		String[] lines = str.split("\r|\n"); //$NON-NLS-1$
		int xInitial = x;

		y -= lines.length * Y_SIZE / 2;
		for (String line : lines) {
			x = xInitial - line.length() * X_SIZE / 2;
			renderLine(line, x, y);
		}
	}

	/**
	 * Renders text with line-wrapping within the rectangle.
	 *
	 * @param str
	 * @param x render location X
	 * @param y render location Y
	 */
	public void render(String str, int x, int y, int width, int height, boolean hCenter, boolean vCenter) {
		String[] lines = str.split("\r|\n"); //$NON-NLS-1$
		int xMax = x + width, xInitial = x;
		int yMax = y + height;


		if (vCenter) {
			y -= lines.length * Y_SIZE / 2;
			y += height / 2;
		}

		for (String line : lines) {
			while (!line.isEmpty()) {
		        if (y > yMax)
		        	break;

				x = xInitial;
				if (hCenter) {
					x -= line.length() * X_SIZE / 2;
					x += width / 2;
				}

				line = renderLine(line, x, y, xMax);
				y += Y_SIZE * LINE_SPACE;
			}
		}
	}

	private void renderLine(String str, int x, int y) {
		renderLine(str, x, y, Integer.MAX_VALUE);
	}

	private String renderLine(String str, int x, int y, int xMax) {
		for (int i = 0; i < str.length(); i++) {
            if (x > xMax)
            	return str.substring(i);
            switch (str.charAt(i)) {
            case 'g':
            case 'y':
                sheet.render(G_DESCENDER, x, y + Y_SIZE);
                break;
            case 'j':
            	sheet.render(J_DESCENDER, x, y + Y_SIZE);
                break;
            case 'p':
            	sheet.render(P_DESCENDER, x, y + Y_SIZE);
                break;
            case 'q':
            	sheet.render(Q_DESCENDER, x, y + Y_SIZE);
                break;
            }
            sheet.render(str.charAt(i), x, y);
            x += X_SIZE;
		}

		return ""; //$NON-NLS-1$
	}
}
