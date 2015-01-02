package net.kopeph.ld31.util;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author alexg
 */
public class Util {
	private Util() {
		throw new AssertionError("No Instantiation of: " + getClass().getName()); //$NON-NLS-1$
	}

	/**
	 * Closes the supplied resource, catching all Exceptions.
	 * @param res
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
	 * Closes the supplied resource, catching all IOExceptions.
	 * @param res
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

	/**
	 * @param d
	 * @return 0 if number is zero, otherwise equivalent to abs(d)/d
	 */
	public static int sign(double d) {
		if (d > 0) return  1;
		if (d < 0) return -1;
				   return  0;
	}

	public static boolean boxContains(int x, int y, int width, int height, int xTest, int yTest) {
		return xTest > x         && yTest > y &&
			   xTest < x + width && yTest < y + height;
	}

	public static double dist(int x1, int y1, int x2, int y2) {
		return new Vector2(x2 - x1, y2 - y1).mag();
	}

	public static int randomInt(int upper) {
		return (int)random(0, upper);
	}

	public static int randomInt(int lower, int upper) {
		return (int)random(lower, upper);
	}

	public static double random(double upper) {
		return random(0, upper);
	}

	public static double random(double lower, double upper) {
		return Math.random() * (upper - lower) + lower;
	}

	public static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static double clamp(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}
}
