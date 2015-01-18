package net.kopeph.ld31.entity;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.graphics.Node;
import net.kopeph.ld31.graphics.Renderable;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.spi.PointPredicate;
import net.kopeph.ld31.util.Pointer;
import net.kopeph.ld31.util.Vector2;
import processing.core.PApplet;

public class Entity implements Renderable {
	public static final int SIZE = 2; //radius-.5
	private static final double SP = 1.0; //horizontal/vertical (cardinal) direction movement speed

	public static final int
		COLOR_OBJECTIVE = 0xFFFF7F7F,
		COLOR_PLAYER    = 0xFFFFFFFF,
		COLOR_ENEMY_COM = 0xFFFF7F00;

	private static final double
		NONE =  -1         ,
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


	protected final PApplet context;
	protected final Level level;
	protected double speedMultiplier = 1.0;

	private Vector2 pos = new Vector2();
	private int color = COLOR_PLAYER;

	public Entity(Level level) {
		this.context = LD31.getContext();
		this.level = level;

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
	private boolean move0(Vector2 offset, boolean tryBop) {
		if (checkOffset(offset)) {
			pos = pos.add(offset); //not having operator overloads is such a drag.
			return true;
		}

		//TODO:extract to constant
		if (tryBop) {
			if (Math.abs(offset.x) > 0.0001 && Math.abs(offset.y) > 0.0001) {
				if (move0(new Vector2(0, offset.y), true)) return true;
				if (move0(new Vector2(offset.x, 0), true)) return true;
			}
			else {
				//if this is in a cardinal direction, try "bopping" around the corner
				if (Math.abs(offset.x) < 0.0001) { // ~== 0
					for (int i = 1; i < SIZE * 4; i++) {
						if (move0(new Vector2( i, offset.y), false)) return true;
						if (move0(new Vector2(-i, offset.y), false)) return true;
					}
				}
				else { //offset.y ~== 0
					for (int i = 1; i < SIZE * 4; i++) {
						if (move0(new Vector2(offset.x,  i), false)) return true;
						if (move0(new Vector2(offset.x, -i), false)) return true;
					}
				}
			}
		}
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
		final int xInitial = x(); //pre-calculating these gives us at least a 30% performance improvement
		final int yInitial = y(); //holy shit
		final int vdsq = viewDistance*viewDistance; //don't judge, every CPU cycle counts

		PointPredicate op = (x, y) -> {
			int i = y*level.LEVEL_WIDTH + x; //we use this value twice now, so it makes sense to calculate and store
			if (array[i] == Level.FLOOR_NONE) return false;
			//restrict it to a circle
			int dx = x - xInitial, dy = y - yInitial; //squaring manually to avoid float/int conversion with PApplet.sq()
			if (dx*dx + dy*dy > vdsq) return false; //distance formula
			array[i] |= color;
			return true;
		};

		//change the bounds of the for loop to stay within the level
		//this way we don't have to do bounds checking per pixel inside of op.on()
		int minx = PApplet.max(xInitial - viewDistance + 1, level.minx);
		int miny = PApplet.max(yInitial - viewDistance + 1, level.miny);
		int maxx = PApplet.min(xInitial + viewDistance - 1, level.maxx);
		int maxy = PApplet.min(yInitial + viewDistance - 1, level.maxy);

		for (int dx = minx; dx <= maxx; ++dx) {
			Trace.ray(xInitial, yInitial, dx, miny, op);
			Trace.ray(xInitial, yInitial, dx, maxy, op);
			//DEBUG
			//array[miny*level.LEVEL_WIDTH + dx] = Entity.COLOR_OBJECTIVE;
			//array[maxy*level.LEVEL_WIDTH + dx] = Entity.COLOR_OBJECTIVE;
		}

		for (int dy = miny + 1; dy < maxy; ++dy) {
			Trace.ray(xInitial, yInitial, minx, dy, op);
			Trace.ray(xInitial, yInitial, maxx, dy, op);
			//DEBUG
			//array[dy*level.LEVEL_WIDTH + minx] = Entity.COLOR_ENEMY_COM;
			//array[dy*level.LEVEL_WIDTH + maxx] = Entity.COLOR_ENEMY_COM;
		}
	}

	public void draw(int color) {
		Trace.rectangle(x() - SIZE, y() - SIZE, SIZE*2 + 1, SIZE*2 + 1, (x, y) -> {
			if (!level.inBounds(x, y)) return false;
			context.pixels[y*level.LEVEL_WIDTH + x] = color;
			return true;
		});
	}
	
	@Override
	public void render() {
		draw(this.color); //XXX: placeholder
	}
}
