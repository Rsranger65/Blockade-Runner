package net.kopeph.ld31.entity;

import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.util.Vector2;

/** @author alexg */
public final class Player extends MovingEntity {
	public static final int COLOR = 0xFFFFFFFF;

	public Player(Level level) {
		super(level, COLOR);
	}

	public Player(Level level, int x, int y) {
		super(level, x, y, COLOR);
	}

	public boolean move(boolean w, boolean s, boolean a, boolean d) {
		Vector2 offset = new Vector2(0, 0);
		if (w) offset = offset.add(new Vector2( 0, -1));
		if (s) offset = offset.add(new Vector2( 0,  1));
		if (a) offset = offset.add(new Vector2(-1,  0));
		if (d) offset = offset.add(new Vector2( 1,  0));
		return move(offset);
	}

	public void renderAlternate(int radius) {
		//equivalent to the functionality of Entity.render(), but after a call to PApplet.updatePixels()
		Trace.rectangle(level.player.screenX() - Entity.SIZE, level.player.screenY() - Entity.SIZE, Entity.SIZE*2 + 1, Entity.SIZE*2 + 1, (x, y) -> {
			context.set(x, y, Player.COLOR);
			return true;
		});
		//draw a circle closing in on the player
		Trace.circle(level.player.screenX(), level.player.screenY(), radius, (x, y) -> {
			if (level.inBounds(x, y))
				context.set(x, y, Player.COLOR);
			return true;
		});
	}
}
