package net.kopeph.ld31.entity;

import java.util.List;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.util.RouteNode;
import net.kopeph.ld31.util.Vector2;
import processing.core.PApplet;

/** @author stuntddude */
public class Enemy extends MovingEntity {
	public static final int
		ENEMY_COM_COLOR = 0xFFFF7F00;

	public static int getColorByString(String value) {
		switch (value.trim().toLowerCase()) {
			case "red":   return Level.FLOOR_RED;
			case "green": return Level.FLOOR_GREEN;
			case "blue":  return Level.FLOOR_BLUE;
			case "yellow": return Level.FLOOR_YELLOW;
			case "cyan": return Level.FLOOR_CYAN;
			case "magenta": return Level.FLOOR_MAGENTA;
			case "black": return Level.FLOOR_BLACK;
			case "white": return Level.FLOOR_WHITE;
		}
		return Level.FLOOR_NONE;
	}

	private static final float TWO_PI = (float) (Math.PI * 2);

	public final int viewDistance = 120; //distance that enemy light can reach in pixels
	private float direction; //radians
	private boolean pursuing; //used in render() so that we can know

	private List<RouteNode> route;
	private int waitTime;
	private int routeIndex;

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

	public Enemy(Level level, int color, List<RouteNode> route) {
		super(level, color);
		this.route = route;
	}

	public Enemy(Level level, int x, int y, int color, List<RouteNode> route) {
		super(level, x, y, color);
		this.route = route;
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
	}

	public void moveAuto() {
		pursuing = checkPursuing();
		if (pursuing) {
			speedMultiplier = 1.25; //set speed slightly faster than player
			move(level.player.pos().sub(pos()).theta()); //Pursue
		} else {
			speedMultiplier = 0.75; //set speed slightly slower than player
			if (route == null)
				moveIdle(); //Wiggle
			else
				followRoute();
		}
	}

	private void moveIdle() {
		direction += context.random(-1.0f/2, 1.0f/2);
		direction += TWO_PI; //because modulus sucks with negative numbers
		direction %= TWO_PI;
		Vector2 oldPos = pos();
		move(direction);
		if (pos().equals(oldPos)) //If we didn't move, pick a random direction to fake a bounce
			direction = context.random(8);
	}

	private void followRoute() {
		if (waitTime == 0) { //if not waiting
			Vector2 v = route.get(routeIndex).pos;
			//if we've just arrived at our destination
			if (PApplet.dist((float)pos().x, (float)pos().y, (float)v.x, (float)v.y) < SP*speedMultiplier) {
				routeIndex = (routeIndex + 1) % route.size();
				v = route.get(routeIndex).pos;
				waitTime = route.get(routeIndex).waitTime;
			}
			move(v.sub(pos()).theta()); //move toward the next node
		} else {
			//wait for the specified amount of time
			waitTime -= 1;
		}
	}

	@Override
	public void render() {
		//draw route lines, if one exists
		if (route != null) {
			for (int i = 1; i <= route.size(); ++i) {
				Trace.line((int)route.get(i - 1).pos.x,          (int)route.get(i - 1).pos.y,
						   (int)route.get(i%route.size()).pos.x, (int)route.get(i%route.size()).pos.y,
						   (x, y) -> {
					context.set(x, y, color);
					return true;
				});
			}
		}

		//draw enemy rectangle
		super.render();

		//draw line to player, if pursuing
		if (pursuing) {
			Trace.line(screenX(), screenY(), level.player.screenX(), level.player.screenY(), (x, y) -> {
				context.set(x, y, color);
				return true;
			});
		}
	}
}
