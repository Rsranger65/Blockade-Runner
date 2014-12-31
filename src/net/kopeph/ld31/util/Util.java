package net.kopeph.ld31.util;

import java.io.Closeable;
import java.io.IOException;

import net.kopeph.ld31.LD31;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * @author alexg
 */
public class Util {
	private Util() {
		throw new AssertionError("No Instantiation of: " + getClass().getName()); //$NON-NLS-1$
	}

	/**
	 * Closes the supplied resource, catching all Exceptions.
	 * @throws RuntimeException when the resource's close() throws an exception.
	 */
	public static void forceClose(AutoCloseable res) {
		try {
			if (res != null)
				res.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the supplied resource, catching all Exceptions.
	 * @throws RuntimeException when the resource's close() throws an exception.
	 */
	public static void forceClose(Closeable res) {
		try {
			if (res != null)
				res.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** @return 0 if number is zero, otherwise equivalent to abs(d)/d */
	public static int sign(double d) {
		if (d > 0) return  1;
		if (d < 0) return -1;
				   return  0;
	}
	
	public static PImage crop(PImage img, int x, int y, int w, int h) {
		PImage cropped = LD31.getContext().createImage(w, h, PConstants.ARGB);
		cropped.copy(img, x, y, w, h, 0, 0, w, h);

		return cropped;
	}
}
