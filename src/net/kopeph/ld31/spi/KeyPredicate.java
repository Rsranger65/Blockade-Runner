package net.kopeph.ld31.spi;

/**
 * Referenced in
 * net.kopeph.ld31.Input
 */
@FunctionalInterface
@Deprecated
public interface KeyPredicate {
	/**
	 * @param keyId ld31.Input based keyCode (distinct from processing based keyCodes)
	 * @param down true iif keyDown even
	 * @return boolean to signal to the function. See function documentation for details
	 */
	public boolean press(int keyId, boolean down);
}
