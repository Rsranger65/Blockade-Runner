package net.kopeph.ld31.entity;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Node;
import net.kopeph.ld31.graphics.PointPredicate;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.util.Pointer;
import net.kopeph.ld31.util.Vector2;
import processing.core.PApplet;

public class Entity implements Renderable {
	public static final int SIZE = 2; //radius-.5
	//if you modify a constant at runtime again I'll fucking kill you
	private static final double SP = 1.0; //horizontal/vertical (cardinal) direction movement speed

	public static final int
		COLOR_OBJECTIVE = 0xFFFF7F7F,
		COLOR_PLAYER    = 0xFFFFFFFF,
		COLOR_ENEMY_COM = 0xFFFF7F00;

	private static final double
		NONE =  -1         ,
		 E   = 	0*Math.PI/4,
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


	protected final PApplet context = LD31.getContext();
	protected final Level level;
	protected double speedMultiplier = 1.0;
	public final int color;

	private Vector2 pos = new Vector2();

	public Entity(Level level, int color) {
		this.level = level;
		this.color = color;

		//place the player in a valid spot
		do {
			pos = new Vector2(context.random(SIZE, level.LEVEL_WIDTH - SIZE),
					          context.random(SIZE, level.LEVEL_HEIGHT - SIZE));
		} while (!validPosition(x(), y()));
	}

	private boolean validPosition(int x, int y) {
		for(int i = 0 - SIZE; i < SIZE + 1; i++)
			for(int j = 0 - SIZE; j < SIZE + 1; j++)
				if(level.tiles[(y + i)*level.LEVEL_WIDTH + x + j] == Level.FLOOR_NONE)
					return false;
		return true;
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

	public boolean move(double angle) {
		return move0(Vector2.polar(speedMultiplier * SP, angle), true);
	}

	/** This only checks and executes the specified move operation or one of its components */
	private boolean move0(Vector2 offset, boolean tryComponents) {
		if (checkOffset(offset)) {
			pos = pos.add(offset); //not having operator overloads is such a drag.
			return true;
		}

		if (offset.x != 0 && offset.y != 0) {
			if (tryComponents) {
				if (move0(new Vector2(0, offset.y), false)) return true;
				if (move0(new Vector2(offset.x, 0), false)) return true;
			}
		}
		//SORRY DON'T WANT TO WORK ON THIS RN
//		//if this is in a cardinal direction, try "bopping" around the corner
//		else if (offset.x == 0) {
//			for (int i = 1; i < SIZE * 2; i++) {
//				if (move0(new Vector2( i, offset.y), false)) return true;
//				if (move0(new Vector2(-i, offset.y), false)) return true;
//			}
//		}
//		else { //offset.y == 0
//			for (int i = 1; i < SIZE * 2; i++) {
//				if (move0(new Vector2(offset.x,  i), false)) return true;
//				if (move0(new Vector2(offset.x, -i), false)) return true;
//			}
//		}
		return false;
	}

	/** This checks the whole move operation */
	private boolean checkOffset(Vector2 offset) {
		Vector2 newPos = pos.add(offset);
		int newXi = (int)Math.round(newPos.x);
		int newYi = (int)Math.round(newPos.y);
		final Pointer<Boolean> passable = new Pointer<>(true);

		Trace.line(x(), y(), newXi, newYi, (x, y) -> { return passable.value = validPosition(x, y);});

		return passable.value;
	}

	public void setPos(Vector2 pos) {
		this.pos = pos;
	}

	public int x() {
		return (int)Math.round(pos.x);
	}

	public int y() {
		return (int)Math.round(pos.y);
	}

	public Vector2 pos() {
		return pos;
	}

	public Node toNode() {
		return new Node(x(), y());
	}

	public void rayTrace(final int[] array, final int viewDistance, final int color) {
		int x = x(); //pre-calculating these gives us at least a 30% performance improvement
		int y = y(); //holy shit
		int vdsq = viewDistance*viewDistance; //don't judge, every CPU cycle counts

		PointPredicate op = (lx, ly) -> {
			int i = ly*level.LEVEL_WIDTH + lx; //we use this value twice now, so it makes sense to calculate and store
			if (array[i] == Level.FLOOR_NONE) return false;
			//restrict it to a circle
			int dx = lx - x, dy = ly - y; //squaring manually to avoid float/int conversion with PApplet.sq()
			if (dx*dx + dy*dy > vdsq) return false; //distance formula
			array[i] |= color;
			return true;
		};

		//change the bounds of the for loop to stay within the level
		//this way we don't have to do bounds checking per pixel inside of op.on()
		int minx = PApplet.max(x - viewDistance + 1, level.minx);
		int miny = PApplet.max(y - viewDistance + 1, level.miny);
		int maxx = PApplet.min(x + viewDistance - 1, level.maxx);
		int maxy = PApplet.min(y + viewDistance - 1, level.maxy);

		for (int dx = minx; dx <= maxx; ++dx) {
			Trace.line(x, y, dx, miny, op);
			Trace.line(x, y, dx, maxy, op);
		}

		for (int dy = miny + 1; dy < maxy; ++dy) {
			Trace.line(x, y, minx, dy, op);
			Trace.line(x, y, maxx, dy, op);
		}
	}

	@Override
	public void render() {
		for (int dy = -SIZE; dy <= SIZE; ++dy) {
			for (int dx = -SIZE; dx <= SIZE; ++dx) {
				int loc = (y() + dy)*context.width + x() + dx;
				if (loc >= 0 && loc < context.pixels.length)
					context.pixels[loc] = color;
			}
		}
	}
}
