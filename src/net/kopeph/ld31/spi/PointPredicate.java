package net.kopeph.ld31.spi;

/**
 * Referenced in:
 * <li> net.kopeph.ld31.entity.Enemy
 * <li> net.kopeph.ld31.graphics.Trace
 */
@FunctionalInterface
public interface PointPredicate {
	/** Called for each pixel applicable to the method. */
	public boolean on(int x, int y);
}
