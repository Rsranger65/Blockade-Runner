package net.kopeph.ld31.spi;

/** Referenced in:
 * <li> net.kopeph.ld31.Input
 * <li> net.kopeph.ld31.InputHandler
 * <li> net.kopeph.ld31.menu.MenuButton
 */
@FunctionalInterface
public interface Interaction {
	/** called when a widget receives interaction, e.g. mouse or keyboard input */
	public void interact(boolean down);
}
