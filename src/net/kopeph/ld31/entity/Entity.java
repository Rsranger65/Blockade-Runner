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


	protected final LD31 context;
	protected final Level level;
	protected double speedMultiplier = 1.0;

	private Vector2 pos = new Vector2();
	public int color;

	public Entity(Level level, int color) {
		this.context = LD31.getContext();
		this.level = level;
		this.color = color;

		//place the entity in a valid spot
		do {
			pos = new Vector2(context.random(SIZE, level.LEVEL_WIDTH - SIZE),
			                  context.random(SIZE, level.LEVEL_HEIGHT - SIZE));
		} while (!validPosition(x(), y()));
	}

	private boolean validPosition(int x, int y) {
		for(int i = 0 - SIZE; i < SIZE + 1; i++)
			for(int j = 0 - SIZE; j < SIZE + 1; j++)
				if (!level.validTile(x + j, y + i))
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
	
	public int screenX() {
		return x() - context.renderer.viewX;
	}
	
	public int screenY() {
		return y() - context.renderer.viewY;
	}

	public Vector2 pos() {
		return pos;
	}

	public Node toNode() {
		return new Node(x(), y());
	}

	public void rayTrace(final int[] array, final int viewDistance, final int color) {
		final int xInitial = screenX(); //pre-calculating these gives us at least a 30% performance improvement
		final int yInitial = screenY(); //holy shit
		final int vdsq = viewDistance*viewDistance; //don't judge, every CPU cycle counts

		PointPredicate op = (x, y) -> {
			//restrict it to a circle
			int dx = x - xInitial, dy = y - yInitial; //squaring manually to avoid int/float conversion with PApplet.sq()
			if (dx*dx + dy*dy >= vdsq) return false; //distance formula
			//restrict to the window (avoid out-of-bounds exceptions)
			int i = y*context.width + x; //we use this value twice now, so it makes sense to calculate and store
			if (!context.contains(x, y)) return true;
			if (array[i] == Level.FLOOR_NONE) return false;
			
			array[i] |= color;
			return true;
		};

		//change the bounds of the for loop to stay within the level
		//this way we don't have to do bounds checking per pixel inside of op.on()
		int minx = PApplet.max(xInitial - viewDistance + 1, 0);
		int miny = PApplet.max(yInitial - viewDistance + 1, 0);
		int maxx = PApplet.min(xInitial + viewDistance - 1, context.width - 1);
		int maxy = PApplet.min(yInitial + viewDistance - 1, context.height - 1);
		
		PApplet.println(minx + ", " + miny + ", " + maxx + ", " + maxy); //debug

		PApplet.println("tracing rays"); //debug
		for (int dx = minx; dx <= maxx; ++dx) {
			Trace.ray(xInitial, yInitial, dx, miny, op);
			Trace.ray(xInitial, yInitial, dx, maxy, op);
			//DEBUG
			//array[miny*context.width + dx] = Entity.COLOR_OBJECTIVE;
			//array[maxy*context.width + dx] = Entity.COLOR_OBJECTIVE;
		}

		for (int dy = miny + 1; dy < maxy; ++dy) {
			Trace.ray(xInitial, yInitial, minx, dy, op);
			Trace.ray(xInitial, yInitial, maxx, dy, op);
			//DEBUG
			//array[dy*context.width + minx] = Entity.COLOR_ENEMY_COM;
			//array[dy*context.width + maxx] = Entity.COLOR_ENEMY_COM;
		}
		PApplet.println("rays traced"); //debug
	}

	@Override
	public void render() {
		Trace.rectangle(screenX() - SIZE, screenY() - SIZE, SIZE*2 + 1, SIZE*2 + 1, (x, y) -> {
			if (!context.contains(x, y)) return false;
			context.pixels[y*context.width + x] = color;
			return true;
		});
	}
}
