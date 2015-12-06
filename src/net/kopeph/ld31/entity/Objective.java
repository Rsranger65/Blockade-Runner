package net.kopeph.ld31.entity;

import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import processing.core.PApplet;

/** @author stuntddude */
public class Objective extends Entity {
	public static final int COLOR = 0xFFFF7F7F;

	public Objective(Level level) {
		super(level, COLOR);
	}

	public Objective(Level level, int x, int y) {
		super(level, x, y, COLOR);
	}

	@Override
	public void render() {
		super.render();

		//draw expanding and contracting circle around objective (uses integer triangle wave algorithm as distance)
		Trace.circle(screenX(), screenY(), PApplet.abs(context.frameCount % 50 - 25) + 50, (x, y) -> {
			context.set(x, y, COLOR);
			return true;
		});
	}
}
