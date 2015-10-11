package net.kopeph.ld31.entity;

import java.util.List;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.spi.PointPredicate;
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

	public void rayTrace(final int[] array, final int viewDistance, final int lightColor) {
		final int xi = screenX(); //pre-calculating these gives a huge performance boost
		final int yi = screenY();
		final int vdsq = viewDistance*viewDistance; //don't judge, ever CPU cycle counts!

		final int width = context.lastWidth;
		final int height = context.lastHeight;
		final int size = array.length;

		//determine the most efficient way to do ray casting based on whether the enemy is inside or outside the level
		PointPredicate op;
		if (context.contains(xi, yi)) {
			op = (x, y) -> {
				//restrict light to a circle
				final int dx = x - xi, dy = y - yi;
				if (dx*dx + dy*dy >= vdsq) return false; //distance formula

				final int i = y*width + x; //we use this value twice, so pre-calculate it
				if (array[i] == Level.FLOOR_NONE) return false;

				array[i] |= lightColor;
				return true;
			};
		} else {
			op = (x, y) -> {
				//restrict light to a circle
				final int dx = x - xi, dy = y - yi;
				if (dx*dx + dy*dy >= vdsq) return false; //distance formula

				final int i = y*width + x; //we use this value thrice, so pre-calculate it
				if (i < 0 || i >= size) return true; //avoid OutOfBoundsException
				if (array[i] == Level.FLOOR_NONE) return false;

				array[i] |= lightColor;

				return true;
			};
		}

		//change the bounds of the for loop to optimally stay within the level
		final int minx = PApplet.max(xi - viewDistance + 1, 0);
		final int miny = PApplet.max(yi - viewDistance + 1, 0);
		final int maxx = PApplet.min(xi + viewDistance - 1, width - 1);
		final int maxy = PApplet.min(yi + viewDistance - 1, height - 1);

		//flags to optimize out unnecessary ray casts
		final boolean traceRight = xi < maxx;
		final boolean traceLeft  = xi > minx;
		final boolean traceDown  = yi < maxy;
		final boolean traceUp    = yi > miny;

		//flags to determine whether rays cast toward a particular edge could end up out of bounds
		final boolean cautionRight = maxx < xi + viewDistance - 1;
		final boolean cautionLeft  = minx > xi - viewDistance + 1;
		final boolean cautionDown  = maxy < yi + viewDistance - 1;
		final boolean cautionUp    = miny > yi - viewDistance + 1;

		//pre-calculated constants to help check for each individual ray whether there is a chance of going out of bounds
		final int dy1 = yi - miny, dy1sq = dy1*dy1;
		final int dy2 = yi - maxy, dy2sq = dy2*dy2;

		//trace top and bottom
		for (int x = minx; x <= maxx; ++x) {
			final int dx = xi - x, dxsq = dx*dx;

			if (traceUp) {
				//check if ray has a chance of going out of bounds and adjust accordingly
				if (cautionUp && dy1sq + dxsq < vdsq) //distance formula
					Trace.line(xi, yi, x, miny, op);
				else
					Trace.ray(xi, yi, x, miny, op);
			}

			if (traceDown) {
				//check if ray has a chance of going out of bounds and adjust accordingly
				if (cautionDown && dy2sq + dxsq < vdsq) //distance formula
					Trace.line(xi, yi, x, maxy, op);
				else
					Trace.ray(xi, yi, x, maxy, op);
			}
		}

		//pre-calculated constants to help check for each individual ray whether there is a chance of going out of bounds
		final int dx1 = xi - minx, dx1sq = dx1*dx1;
		final int dx2 = xi - maxx, dx2sq = dx2*dx2;

		//trace left and right (discounting corners because we already traced those in the loop above)
		for (int y = miny + 1; y < maxy; ++y) {
			final int dy = yi - y, dysq = dy*dy;

			if (traceLeft) {
				//check if ray has a chance of going out of bounds and adjust accordingly
				if (cautionLeft && dx1sq + dysq < vdsq)
					Trace.line(xi, yi, minx, y, op);
				else
					Trace.ray(xi, yi, minx, y, op);
			}

			if (traceRight) {
				if (cautionRight && dx2sq + dysq < vdsq)
					Trace.line(xi, yi, maxx, y, op);
				else
					Trace.ray(xi, yi, maxx, y, op);
			}
		}
	}

	@Override
	public void render() {
		//draw route lines, if one exists
		if (route != null) {
			for (int i = 1; i <= route.size(); ++i) {
				Trace.line((int)route.get(i - 1).pos.x          - context.renderer.viewX, (int)route.get(i - 1).pos.y          - context.renderer.viewY,
						   (int)route.get(i%route.size()).pos.x - context.renderer.viewX, (int)route.get(i%route.size()).pos.y - context.renderer.viewY,
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
