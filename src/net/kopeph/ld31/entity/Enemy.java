package net.kopeph.ld31.entity;

import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.PointPredicate;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.util.Vector2;
import processing.core.PApplet;

public class Enemy extends Entity {
	private static final float TWO_PI = (float) (Math.PI * 2);

	public final int viewDistance = 120; //distance that enemy light can reach in pixels
	public final int comDistance = 100; //distance that enemy coms can reach in pixels (doesn't need line of sight)
	private float direction; //radians

	//for communication
	Enemy referrer; //null if not pursuing, this if has line of sight, otherwise the referring Enemy

	public Enemy(PApplet context, Level level, int color) {
		super(context, level, color);
	}

	//checks if should be pursuing the player and then notifies any enemies within com distance
	/**
	 * @param ref Which Enemy is notifying this Enemy, or null if this is the first contact.
	 */
	public void checkPursuing(Enemy ref) {
		if (referrer != null) return; //if we've already been notified, no need to check all this again

		//establish whether or not we have line of sight
		referrer = this; //guilty until proven innocent
		Trace.line(x(), y(), level.player.x(), level.player.y(), (x, y) -> {
			if (level.tiles[y*context.width + x] != Level.FLOOR_NONE)
				return true;
			referrer = ref;
			return false;
		}); //for some reason this doesn't fucking work

		//notify other enemies within communication range
		if (referrer != null)
			for (Enemy e : level.enemies)
				if (e != this && PApplet.dist(x(), y(), e.x(), e.y()) < comDistance)
					e.checkPursuing(this);
	}

	public void moveAuto() {
		if (referrer != null) {
			speedMultiplier = 1.25; //set speed slightly faster than player

			move(y() > level.player.y(), //Pursue TODO: make this pursue in line of sight direction
				 y() < level.player.y(),
				 x() > level.player.x(),
				 x() < level.player.x());
		} else {
			speedMultiplier = 0.75; //set speed slightly slower than player

			moveIdle(); //Wiggle
		}
	}

	public void moveIdle() {
		direction += context.random(-1.0f/2, 1.0f/2);
		direction += TWO_PI; //because modulus sucks with negative numbers
		direction %= TWO_PI;
		Vector2 oldPos = pos();
		move(direction);
		if (pos().equals(oldPos)) //If we didn't move, pick a random direction to fake a bounce
			direction = context.random(8);
	}

	@Override
	public void render() {
		super.render();

		PointPredicate op = (x, y) -> {
			if (level.inBounds(x, y))
				context.pixels[y*context.width + x] = Entity.COLOR_ENEMY_COM;
			return true;
		};

		if (referrer == this) {
			Trace.line(x(), y(), level.player.x(), level.player.y(), op);
		} else if (referrer != null) {
			Trace.line(x(), y(), referrer.x(), referrer.y(), op);
			//Util.traceCircle(x(), y(), (int)PApplet.dist(x(), y(), referrer.x(), referrer.y()), op);
		}

		referrer = null; //reset required for next call to checkPursuing() to work
	}
}
