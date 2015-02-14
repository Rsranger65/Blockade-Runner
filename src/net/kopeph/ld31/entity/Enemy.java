package net.kopeph.ld31.entity;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.spi.PointPredicate;
import net.kopeph.ld31.util.Vector2;

public class Enemy extends Entity {
	private static final float TWO_PI = (float) (Math.PI * 2);

	public final int viewDistance = 120; //distance that enemy light can reach in pixels
	public final int comDistance = 100; //distance that enemy coms can reach in pixels (doesn't need line of sight)
	private float direction; //radians

	//for communication
	boolean pursuing; //null if not pursuing, this if has line of sight, otherwise the referring Enemy

	public Enemy(Level level) {
		super(level, randomColor());
	}
	
	public Enemy(Level level, int x, int y) {
		super(level, x, y, randomColor());
	}
	
	//helper function for constructor
	private static int randomColor() {
		int[] possibleColors = { Level.FLOOR_RED, Level.FLOOR_GREEN, Level.FLOOR_BLUE };
		return possibleColors[(int)(LD31.getContext().random(possibleColors.length))];
	}

	/** Checks if the enemy should pursue the player by line of sight */
	public void checkPursuing() {
		pursuing = true; //guilty until proven innocent
		Trace.line(x(), y(), level.player.x(), level.player.y(), (x, y) -> {
			if (level.tiles[y*level.LEVEL_WIDTH + x] != Level.FLOOR_NONE)
				return true;
			pursuing = false;
			return false;
		});
		//XXX: consider making enemy communication omnidistant (would remove this logic entirely)
	}

	public void moveAuto() {
		if (pursuing) {
			speedMultiplier = 1.25; //set speed slightly faster than player
			move(new Vector2(level.player.x() - x(), level.player.y() - y()).theta());
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

		if (pursuing) {
			Trace.line(screenX(), screenY(), level.player.screenX(), level.player.screenY(), op);
		}
	}
}
