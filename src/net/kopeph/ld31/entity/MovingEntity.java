package net.kopeph.ld31.entity;

import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.util.Util;
import net.kopeph.ld31.util.Vector2;
import processing.core.PApplet;

/** @author alexg */
public class MovingEntity extends Entity {
	protected double speedMultiplier = 1.0;

	public MovingEntity(Level level, int color) {
		super(level, color);
	}

	public MovingEntity(Level level, int x, int y, int color) {
		super(level, x, y, color);
	}

	public boolean move(double angle) {
		return move(Vector2.polar(speedMultiplier * SP, angle));
	}

	public boolean move(Vector2 offset) {
		//Check direct move
		if (move0(offset)) return true;

		//Check component moves
		if (!Util.epsilonZero(offset.x) && move0(new Vector2(offset.x, 0))) return true;
		if (!Util.epsilonZero(offset.y) && move0(new Vector2(0, offset.y))) return true;

		//Check component moves, with slight bias (using manhattan movement)
		for (int i = 0; i <= SIZE; i++) {
			if (!Util.epsilonZero(offset.x) && move0(new Vector2( 0,  i), new Vector2(offset.x, 0))) return true;
			if (!Util.epsilonZero(offset.y) && move0(new Vector2( i,  0), new Vector2(0, offset.y))) return true;
			if (!Util.epsilonZero(offset.x) && move0(new Vector2( 0, -i), new Vector2(offset.x, 0))) return true;
			if (!Util.epsilonZero(offset.y) && move0(new Vector2(-i,  0), new Vector2(0, offset.y))) return true;
		}

		return false;
	}

	private boolean move0(Vector2...offsets) {
		Vector2 futurePos = new Vector2(pos);

		for (Vector2 offset : offsets)
			if (!checkOffset(futurePos, futurePos = futurePos.add(offset)))
				return false;

		pos = futurePos;
		return true;
	}

	/** @return true only if the whole movement is valid */
	private boolean checkOffset(Vector2 oldPos, Vector2 newPos) {
		int oldXi = (int)Math.round(oldPos.x);
		int oldYi = (int)Math.round(oldPos.y);
		int newXi = (int)Math.round(newPos.x);
		int newYi = (int)Math.round(newPos.y);

		return Trace.line(oldXi, oldYi, newXi, newYi, (x, y) -> { return validPosition(x, y);});
	}
}
