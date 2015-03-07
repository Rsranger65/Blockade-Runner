package net.kopeph.ld31.graphics;

/** Base interface for all objects that can be painted to the screen */
public interface Renderable {
	/**
	 * Tells the object to paint itself to the processing context found by invoking
	 * {@code net.kopeph.ld31.LD31.getContext()}
	 */
	public void render();
}
