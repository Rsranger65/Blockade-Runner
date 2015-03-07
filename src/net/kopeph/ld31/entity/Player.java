package net.kopeph.ld31.entity;

import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;

public final class Player extends Creature {
	public static final int COLOR = 0xFFFFFFFF;

	private static final double
		NONE = -1          ,
		 E   =  0*Math.PI/4,
		SE   =  1*Math.PI/4,
		S    =  2*Math.PI/4,
		SW   =  3*Math.PI/4,
		 W   =  4*Math.PI/4,
		NW   =  5*Math.PI/4,
		N    =  6*Math.PI/4,
		NE   =  7*Math.PI/4;

	private static final double[] DIRECTION = {
		NONE,  E,  W, NONE,
		S   , SE, SW, S   ,
		N   , NE, NW, N   ,
		NONE,  E,  W, NONE
	};

	public Player(Level level) {
		super(level, COLOR);
	}

	public Player(Level level, int x, int y) {
		super(level, x, y, COLOR);
	}

	public boolean move(boolean w, boolean s, boolean a, boolean d) {
		int buttonFlags = (w ? 8 : 0);
		buttonFlags    |= (s ? 4 : 0);
		buttonFlags    |= (a ? 2 : 0);
		buttonFlags    |= (d ? 1 : 0);

		if (DIRECTION[buttonFlags] == NONE)
			return true;

		return move(DIRECTION[buttonFlags]);
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
