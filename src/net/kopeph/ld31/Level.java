package net.kopeph.ld31;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.spi.PointPredicate;
import net.kopeph.ld31.util.ThreadPool;
import processing.core.PApplet;

/**
 * @author stuntddude
 */
public class Level implements AutoCloseable {
	//C-style enumeration of color values
	public static final int
		FLOOR_NONE    = 0x00000000,
		FLOOR_WHITE   = 0xFFFFFFFF,
		FLOOR_BLACK   = 0xFF000000,
		FLOOR_RED     = 0xFFFF0000,
		FLOOR_GREEN   = 0xFF00FF00,
		FLOOR_BLUE    = 0xFF0000FF,
		FLOOR_CYAN    = 0xFF00FFFF,
		FLOOR_YELLOW  = 0xFFFFFF00,
		FLOOR_MAGENTA = 0xFFFF00FF;

	//constants (these may not be constant later)
	private final int ROOM_COUNT, //25
	                  MIN_ROOM_WIDTH = 50,
	                  MIN_ROOM_HEIGHT = 50,
	                  MAX_ROOM_WIDTH = 150,
	                  MAX_ROOM_HEIGHT = 150,

	                  HALLWAY_COUNT, //60
	                  MIN_HALLWAY_LENGTH, //50
	                  MAX_HALLWAY_LENGTH, //300
	                  HALLWAY_SIZE = 5, //number of pixels to either side of the center of a hallway

	                  VORONOI_POINTS = 100;

	public final int LEVEL_WIDTH,
	                 LEVEL_HEIGHT,
	                 ENEMY_COUNT; //20

	//enemies and player
	public Enemy[] enemies;
	public Entity  player;
	public Entity  objective;

	private final ThreadPool lightingThreadPool = new ThreadPool();
	public final int[] tiles;

	//used to constrain Entity.rayTrace() for performance purposes
	public int minx, miny, maxx, maxy;

	public Level(int width, int height) {
		PApplet context = LD31.getContext();

		LEVEL_WIDTH = width;
		LEVEL_HEIGHT = height;

		//a few adjustments to make the level properties scale somewhat with the game size
		//these are more or less just arbitrary magic numbers that are "close enough" to the desired result
		ROOM_COUNT = LEVEL_WIDTH*LEVEL_HEIGHT / 36000;
		HALLWAY_COUNT = LEVEL_WIDTH*LEVEL_HEIGHT / 18000 + 10;
		MIN_HALLWAY_LENGTH = LEVEL_WIDTH*LEVEL_HEIGHT / 18000;
		MAX_HALLWAY_LENGTH = LEVEL_WIDTH*LEVEL_HEIGHT / 9000 + 200;

		ENEMY_COUNT = LEVEL_WIDTH*LEVEL_HEIGHT / 44100;

		tiles = new int[LEVEL_WIDTH * LEVEL_HEIGHT];

		do {
			//these will be overwritten later in clearRect()
			minx = LEVEL_WIDTH;
			miny = LEVEL_HEIGHT;
			maxx = 0;
			maxy = 0;

			Arrays.fill(tiles, FLOOR_NONE);

			//clear out the rooms
			for (int r = 0; r < ROOM_COUNT; ++r) {
				int rw = (int)context.random(MIN_ROOM_WIDTH, MAX_ROOM_WIDTH);
				int rh = (int)context.random(MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT);
				int rx = (int)context.random(LEVEL_WIDTH - rw - 1);
				int ry = (int)context.random(LEVEL_HEIGHT - rh - 1);

				clearRect(rx, ry, rw, rh, FLOOR_BLACK);
			}

			//clear out some hallways
			for (int i = 0; i < HALLWAY_COUNT; ++i) {
				int rx1, ry1, rx2, ry2;
				//find valid start and end points
				do {
					rx1 = (int)context.random(HALLWAY_SIZE, LEVEL_WIDTH - HALLWAY_SIZE);
					ry1 = (int)context.random(HALLWAY_SIZE, LEVEL_HEIGHT - HALLWAY_SIZE);
					rx2 = (int)context.random(HALLWAY_SIZE, LEVEL_WIDTH - HALLWAY_SIZE);
					ry2 = (int)context.random(HALLWAY_SIZE, LEVEL_HEIGHT - HALLWAY_SIZE);
				} while (Math.abs(rx2 - rx1) + Math.abs(ry2 - ry1) < MIN_HALLWAY_LENGTH ||
						 Math.abs(rx2 - rx1) + Math.abs(ry2 - ry1) > MAX_HALLWAY_LENGTH ||
						 !validTile(rx1, ry1) || !validTile(rx2, ry2));

				//clear out the tiles
				for (int x = rx1; x != rx2; x += (rx1 < rx2? 1 : -1))
					clearRect(x - HALLWAY_SIZE, ry1 - HALLWAY_SIZE, HALLWAY_SIZE*2 + 1, HALLWAY_SIZE*2 + 1, FLOOR_BLACK);
				for (int y = ry1; y != ry2; y += (ry1 < ry2? 1 : -1))
					clearRect(rx2 - HALLWAY_SIZE, y - HALLWAY_SIZE, HALLWAY_SIZE*2 + 1, HALLWAY_SIZE*2 + 1, FLOOR_BLACK);
				clearRect(rx2 - HALLWAY_SIZE, ry2 - HALLWAY_SIZE, HALLWAY_SIZE*2 + 1, HALLWAY_SIZE*2 + 1, FLOOR_BLACK);
			}
		} while (!validateLevel()); //keep generating new layouts until we get one that's continuous

		//create points for a voronoi diagram which will determine level coloring
		int[] posx = new int[VORONOI_POINTS];
		int[] posy = new int[VORONOI_POINTS];
		int[] colors = new int[VORONOI_POINTS];
		for (int i = 0; i < VORONOI_POINTS; ++i) {
			//assign a random position
			posx[i] = (int)context.random(LEVEL_WIDTH);
			posy[i] = (int)context.random(LEVEL_HEIGHT);

			//assign a random color
			int[] possibleColors = { FLOOR_BLACK, FLOOR_RED, FLOOR_GREEN, FLOOR_BLUE };
			colors[i] = possibleColors[(int)context.random(possibleColors.length)];
		}

		//for each pixel of floor
		for (int i = tiles.length - 1; i-- != 0;) {
			if (tiles[i] != FLOOR_NONE) {
				//assign the color of the closest voronoi point (by manhattan distance)
				int minDistance = 1000000; //arbitrarily large number
				int color = FLOOR_BLACK;
				int x = i%LEVEL_WIDTH;
				int y = i/LEVEL_WIDTH;

				for (int v = VORONOI_POINTS - 1; v-- != 0;) {
					int distance = Math.abs(posx[v] - x) + Math.abs(posy[v] - y);
					if (distance < minDistance) {
						minDistance = distance;
						color = colors[v];
					}
				}

				tiles[i] = color;
			}
		}

		//clean up 1-wide walls (this may not even be worth the extra work)
		for (int y = 1; y < LEVEL_HEIGHT - 1; ++y) {
			for (int x = 1; x < LEVEL_WIDTH - 1; ++x) {
				if (tiles[y*LEVEL_WIDTH + x] == FLOOR_NONE) {
					if (tiles[y*LEVEL_WIDTH + x - 1] != FLOOR_NONE &&
						tiles[y*LEVEL_WIDTH + x + 1] != FLOOR_NONE) {
						tiles[y*LEVEL_WIDTH + x] = tiles[y*LEVEL_WIDTH + x - 1];
					} else if (tiles[(y - 1)*LEVEL_WIDTH + x] != FLOOR_NONE &&
					           tiles[(y + 1)*LEVEL_WIDTH + x] != FLOOR_NONE) {
						tiles[y*LEVEL_WIDTH + x] = tiles[(y + 1)*LEVEL_WIDTH + x];
					}
				}
			}
		}

		//add enemies
		enemies = new Enemy[ENEMY_COUNT];
		for (int i = 0; i < ENEMY_COUNT; ++i)
			enemies[i] = new Enemy(this);

		//allow the player + objective placement to give up after so many attempts
		//this is so we don't lock up on edge cases where one of the placements can't possibly succeed
		int placementFailCount = 0;

		//add player
		do {
			player = new Entity(this);
			++placementFailCount;
		} while (!goodPlayerPlacement() && placementFailCount < 100);

		//add objective
		placementFailCount = 0;
		do {
			objective = new Entity(this);
			++placementFailCount;
		} while (!goodObjectivePlacement() && placementFailCount < 100);
	}

	//Effectively a destructor, since ThreadPool explicitly needs one
	@Override
	public void close() throws Exception {
		lightingThreadPool.close();
	}

	//checks to make sure the level is continuous by doing a flood fill and then checking for any pixels not reached
	private boolean validateLevel() {
		PointPredicate op = (x, y) -> {
			if (tiles[y*LEVEL_WIDTH + x] != FLOOR_BLACK)
				return false;
			tiles[y*LEVEL_WIDTH + x] = FLOOR_WHITE;
			return true;
		};

		for (int i = 0; i < tiles.length; ++i) {
			if (tiles[i] == FLOOR_BLACK) {
				//find the first pixel of floor and flood fill from there
				Trace.fill(i%LEVEL_WIDTH, i/LEVEL_WIDTH, (x, y) -> {
					return validTile(x, y, op);
				});
				break;
			}
		}

		//iterate backwards looking for unfilled tiles because it's slightly faster
		for (int i = tiles.length - 1; i-- != 0;)
			if (tiles[i] == FLOOR_BLACK)
				return false;

		return true;
	}

	//helper function for constructor/room + hallway generation
	private void clearRect(int x0, int y0, int w, int h, int color) {
		//update minx, maxx, miny, maxy if they need to be updated
		minx = PApplet.min(minx, x0);
		miny = PApplet.min(miny, y0);
		maxx = PApplet.max(maxx, x0 + w - 1);
		maxy = PApplet.max(maxy, y0 + h - 1);

		//this does the clearing
		Trace.rectangle(x0, y0, w, h, (x, y) -> {
			tiles[y*LEVEL_WIDTH + x] = color;
			return true;
		});
	}

	//helper function for constructor/player placement
	private boolean goodPlayerPlacement() {
		for (Enemy e : enemies)
			if (PApplet.dist(e.x(), e.y(), player.x(), player.y()) < e.viewDistance)
				return false;
		return true;
	}

	//helper function for constructor/objective placement
	private boolean goodObjectivePlacement() {
		return (PApplet.dist(player.x(), player.y(), objective.x(), objective.y()) > 200); //the magic numbers are real
	}

	public void calculateLighting(final int[] lighting) {
		System.arraycopy(tiles, 0, lighting, 0, tiles.length);
		
		/*
		List<Enemy> ordered = new ArrayList<>();
		ordered.add(enemies[0]);
		
		//for as many enemies as we need to order
		while (ordered.size() != enemies.length) {
			//find the farthest enemy from the ones currently running
			float farthestDistance = -1; //arbitrarily small number
			Enemy farthestEnemy = null;
			//check each enemy to find the farthest
			for (Enemy e : enemies) {
				//skip over ones we've already added
				if (!ordered.contains(e)) {
					//find how far away we are from running enemies
					float minDistance = Float.MAX_VALUE; //arbitrarily large number
					for (int i = PApplet.max(0, ordered.size() - lightingThreadPool.poolSize); i < ordered.size(); ++i) {
						//look at each running enemy and find the distance of the closest one
						float currentDistance = PApplet.dist(e.x(), e.y(), ordered.get(i).x(), ordered.get(i).y());
						if (currentDistance < minDistance) {
							minDistance = currentDistance;
						}
					}
					
					//replace farthestEnemy if we've found one that's farther
					if (minDistance > farthestDistance) {
						farthestDistance = minDistance;
						farthestEnemy = e;
					}
				}
			}
			
			//finally, add the farthest enemy now that we've found it
			ordered.add(farthestEnemy);
		}
		*/

		for (final Enemy e : enemies) {
			//create a new thread to run the lighting process of each enemy
			//this is extremely simple because the lighting is an embarrassingly parallel operation
			//Pray to the java gods that this doesn't have actual data races
			//lol it really might tho
			lightingThreadPool.post(() -> { e.rayTrace(lighting, e.viewDistance, e.color); });
		}

		lightingThreadPool.forceSync();
	}

	//returns true if an only if the coordinates are inside the level and not inside a wall
	public boolean validTile(int x, int y) {
		return (inBounds(x, y) && tiles[y*LEVEL_WIDTH + x] != FLOOR_NONE);
	}

	public boolean validTile(int x, int y, PointPredicate op) {
		return (inBounds(x, y) && op.on(x, y));
	}

	public boolean inBounds(int x, int y) {
		return (x > 0 && x < LEVEL_WIDTH && y > 0 && y < LEVEL_HEIGHT);
	}
}
