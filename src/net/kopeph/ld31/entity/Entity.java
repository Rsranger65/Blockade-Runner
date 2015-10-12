package net.kopeph.ld31.entity;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.util.Vector2;

/** @author alexg */
public abstract class Entity {
	public static final int SIZE = 2; //radius-.5
	protected static final double SP = 1.0; //horizontal/vertical (cardinal) direction movement speed

	protected final LD31 context;
	protected final Level level;

	protected Vector2 pos = new Vector2();
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

	public Entity(Level level, int x, int y, int color) {
		this.context = LD31.getContext();
		this.level = level;
		this.color = color;

		pos = new Vector2(x, y);
	}

	protected boolean validPosition(int x, int y) {
		for(int i = 0 - SIZE; i < SIZE + 1; i++)
			for(int j = 0 - SIZE; j < SIZE + 1; j++)
				if (!level.validTile(x + j, y + i))
					return false;
		return true;
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

	public void render() {
		context.fill(color);
		context.rect(screenX() - SIZE, screenY() - SIZE, SIZE*2 + 1, SIZE*2 + 1);
	}
}
