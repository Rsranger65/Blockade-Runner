package net.kopeph.ld31.spi;

/** Used by interactive widgets for genericity */
@FunctionalInterface
public interface Interaction {
	/** called when a widget recieves interaction, e.g. mouse or keyboard input */
	public void interact(boolean down);
}
