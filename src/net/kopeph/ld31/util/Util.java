package net.kopeph.ld31.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

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

	public static PImage crop(PImage img, int w, int h) {
		PImage cropped = LD31.getContext().createImage(w, h, PConstants.ARGB);
		for (int x = 0; x < w; x += img.width)
			for (int y = 0; y < h; y += img.height)
				cropped.copy(img, 0, 0, img.width, img.height, x, y, img.width, img.height);
		return cropped;
	}

	public static boolean boxContains(double x, double y, double w, double h,
	                                  double xTest, double yTest) {
		return xTest > x     && yTest > y &&
		       xTest < x + w && yTest < y + h;
	}

	/**
	 * Missing counterpart for String.split()
	 *
	 * @param array
	 * @param joiner The delimiter to add between each element
	 * @return The joined string
	 */
	public static <T> String join(Iterable<T> array, String joiner) {
		StringBuilder b = new StringBuilder();
		Iterator<T> i = array.iterator();
		boolean first = true;

		while (i.hasNext()) {
			if (first)
				first = false;
			else
				b.append(joiner);
			b.append(i.next());
		}

		return b.toString();
	}

	/** @return true if input is ~== 0*/
	public static boolean epsilonZero(double input) {
		return Math.abs(input) < 0.000001;
	}
}
