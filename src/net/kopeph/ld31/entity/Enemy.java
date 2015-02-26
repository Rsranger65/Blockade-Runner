package net.kopeph.ld31.entity;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.util.Vector2;

public class Enemy extends Entity {
	private static final float TWO_PI = (float) (Math.PI * 2);

	public final int viewDistance = 120; //distance that enemy light can reach in pixels
	private float direction; //radians
	private boolean pursuing; //used in render() so that we can know

	public Enemy(Level level) {
		super(level, randomColor());
	}
	
	public Enemy(Level level, int color) {
		super(level, color);
	}
	
	public Enemy(Level level, int x, int y) {
		super(level, x, y, randomColor());
	}
	
	public Enemy(Level level, int x, int y, int color) {
		super(level, x, y, color);
	}
	
	//helper function for constructor
	public static int randomColor() {
		int[] possibleColors = { Level.FLOOR_RED, Level.FLOOR_GREEN, Level.FLOOR_BLUE };
		return possibleColors[(int)(LD31.getContext().random(possibleColors.length))];
	}

	/** Checks if the enemy should pursue the player by line of sight */
	private boolean checkPursuing() {
		//should only pursue if the player is in white light
		if (context.pixels[level.player.screenY()*context.lastWidth + level.player.screenX()] != Level.FLOOR_WHITE) return false;
		//should only pursue if the player is in line of sight
		return Trace.line(x(), y(), level.player.x(), level.player.y(), (x, y) -> {
			if (level.tiles[y*level.LEVEL_WIDTH + x] != Level.FLOOR_NONE)
				return true;
			return false;
		});
		//XXX: consider making enemy communication omnidistant (would remove this logic entirely)
	}

	public void moveAuto() {
		pursuing = checkPursuing();
		if (pursuing) {
			speedMultiplier = 1.25; //set speed slightly faster than player
			move(level.player.pos().sub(pos()).theta());
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

		if (pursuing) {
			Trace.line(screenX(), screenY(), level.player.screenX(), level.player.screenY(), (x, y) -> {
				if (level.inBounds(x, y))
					context.pixels[y*context.lastWidth + x] = Entity.COLOR_ENEMY_COM;
				return true;
			});
		}
	}
}
