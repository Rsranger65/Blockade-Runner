package net.kopeph.ld31.util;

import net.kopeph.ld31.LD31;
import processing.core.PConstants;
import processing.core.PImage;

/** @author alexg */
public class Util {
	private Util() {
		throw new AssertionError("No Instantiation of: " + getClass().getName()); //$NON-NLS-1$
	}

	/**
	 * Creates a new PImage of a different size.
	 * @param img source image
	 * @param x & y position of top-left corner to be the new origin
	 * @param w new width
	 * @param h new height
	 * @return resultant image, extra space in new image is transparent
	 */
	public static PImage crop(PImage img, int x, int y, int w, int h) {
		PImage cropped = LD31.getContext().createImage(w, h, PConstants.ARGB);
		cropped.copy(img, x, y, w, h, 0, 0, w, h);

		return cropped;
	}

	/**
	 * Creates a new PImage of a different size.
	 * @param img source image
	 * @param w new width
	 * @param h new height
	 * @return resultant image, tiling the result
	 */
	public static PImage crop(PImage img, int w, int h) {
		PImage cropped = LD31.getContext().createImage(w, h, PConstants.ARGB);
		for (int x = 0; x < w; x += img.width)
			for (int y = 0; y < h; y += img.height)
				cropped.copy(img, 0, 0, img.width, img.height, x, y, img.width, img.height);
		return cropped;
	}

	/** @return true if input is ~== 0*/
	public static boolean epsilonZero(double input) {
		return Math.abs(input) < 0.000001;
	}
}
