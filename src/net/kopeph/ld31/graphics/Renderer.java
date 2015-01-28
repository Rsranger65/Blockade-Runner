package net.kopeph.ld31.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.util.ThreadPool;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PImage;

public class Renderer {
	public PImage textureRed    , rawTextureRed;
	public PImage textureGreen  , rawTextureGreen;
	public PImage textureBlue   , rawTextureBlue;
	public PImage textureCyan   , rawTextureCyan;
	public PImage textureMagenta, rawTextureMagenta;
	public PImage textureYellow , rawTextureYellow;
	public PImage textureGrey   , rawTextureGrey;
	public PImage textureWhite  , rawTextureWhite;
	public Font font;
	
	public int viewX = 0, viewY = 0;
	
	private final LD31 context;
	private final ThreadPool renderingPool = new ThreadPool();
	
	private Map<Integer, List<Renderable>> states;
	
	public Renderer() {
		context = LD31.getContext();
		
		//load raw textures
		rawTextureRed     = context.loadImage("res/red-background.jpg"    ); //$NON-NLS-1$
		rawTextureGreen   = context.loadImage("res/green-background.jpg"  ); //$NON-NLS-1$
		rawTextureBlue    = context.loadImage("res/blue-background.jpg"   ); //$NON-NLS-1$
		rawTextureCyan    = context.loadImage("res/cyan-background.jpg"   ); //$NON-NLS-1$
		rawTextureMagenta = context.loadImage("res/magenta-background.jpg"); //$NON-NLS-1$
		rawTextureYellow  = context.loadImage("res/yellow-background.jpg" ); //$NON-NLS-1$
		rawTextureGrey    = context.loadImage("res/grey-background.jpg"   ); //$NON-NLS-1$
		rawTextureWhite   = context.loadImage("res/white-background.jpg"  ); //$NON-NLS-1$

		font = new Font("res/font-16-white.png"); //$NON-NLS-1$
	}
	
	public void cropTextures(int width, int height) {
		textureRed     = Util.crop(rawTextureRed    , 0, 0, width, height);
		textureGreen   = Util.crop(rawTextureGreen  , 0, 0, width, height);
		textureBlue    = Util.crop(rawTextureBlue   , 0, 0, width, height);
		textureCyan    = Util.crop(rawTextureCyan   , 0, 0, width, height);
		textureMagenta = Util.crop(rawTextureMagenta, 0, 0, width, height);
		textureYellow  = Util.crop(rawTextureYellow , 0, 0, width, height);
		textureGrey    = Util.crop(rawTextureGrey   , 0, 0, width, height);
		textureWhite   = Util.crop(rawTextureWhite  , 0, 0, width, height);
	}
	
	public void calculateLighting(int[] lighting, Level level) {
		viewX = PApplet.max(0, PApplet.min(level.LEVEL_WIDTH - context.lastWidth, level.player.x() - context.lastWidth/2));
		viewY = PApplet.max(0, PApplet.min(level.LEVEL_HEIGHT - context.lastHeight, level.player.y() - context.lastHeight/2));
		
		//crop the tiles array into the pixels array
		//assumes the bottom left of the screen won't be outside the level unless the level is smaller than the screen
		for (int y = 0; y < PApplet.min(context.lastHeight, level.LEVEL_HEIGHT); ++y) {
			System.arraycopy(level.tiles, (y + viewY)*level.LEVEL_WIDTH + viewX, lighting, y*context.lastWidth, PApplet.min(context.lastWidth, level.LEVEL_WIDTH));
		}
		
		
		/*
		//LIGHTING SCHEDULER:
		
		//build list of enemies needing lighting
		List<Enemy> light = new ArrayList<>();
		for (Enemy e : level.enemies)
			if (Util.boxContains(-e.viewDistance + 1,
								 -e.viewDistance + 1,
								 context.lastWidth + e.viewDistance - 2,
								 context.lastHeight + e.viewDistance - 2,
								 e.screenX(), e.screenY()))
				light.add(e);
		
		//if there are no enemies to light, don't bother with any of this
		if (light.size() == 0) {
			System.out.println("NO ENEMIES TO LIGHT"); //debug
			return;
		}
		
		//create stack representing the order of enemies to light
		List<Integer> order = new ArrayList<>(); //really just a stack, to be honest, but lists are nice
		
		//create "current" index to keep track of where we are in the algorithm
		int current = -1;
		
		//while the ordering is incomplete
		while (order.size() < light.size()) {
			//find the next potential
			current = firstOutOfRange(order, light, current);
			//if there is none
			if (current == -1) {
				//pop from the stack (move backwards)
				current = order.remove(order.size() - 1);
			} else {
				//push onto the stack
				order.add(current);
				current = -1;
			}
			
			//if we try all our possibilities and 
			if (order.size() == 0) {
				System.out.println("LIGHTING ORDER FAILED");
				for (int i = 0; i < light.size(); ++i)
					order.add(i);
				break;
			}
		}
		
		
		
		for (Integer i : order) {
			Enemy e = light.get(i);
			renderingPool.post(() -> { e.rayTrace(lighting, e.viewDistance, e.color); });
		}
		*/
		
		for (final Enemy e : level.enemies) {
			//create a new thread to run the lighting process of each enemy
			if (e.screenX() > -e.viewDistance + 1 && e.screenX() < context.lastWidth + e.viewDistance - 2 &&
				e.screenY() > -e.viewDistance + 1 && e.screenY() < context.lastHeight + e.viewDistance - 2) {
				renderingPool.post(() -> { e.rayTrace(lighting, e.viewDistance, e.color); });
			}
		}
		
		renderingPool.forceSync();
	}
	
	//helper function for lighting scheduler in calculateLighting()
	private int firstOutOfRange(List<Integer> order, List<Enemy> light, int fromIndex) {
		//find the first enemy that is unlit and out of lighting range of all current lighting threads
		for (int i = fromIndex + 1; i < light.size(); ++i)
			if (!order.contains(i) && safeToLight(order, light, i, renderingPool.poolSize - 1))
				return i;
		return -1;
	}
	
	//helper function for firstOutOfRange()
	private boolean safeToLight(List<Integer> order, List<Enemy> light, int index, int poolSize) {
		//if the lighting range overlaps with any currently running lighting threads, cancel
		for (int i = PApplet.max(0, order.size() - poolSize); i < order.size(); ++i)
			if (PApplet.dist(light.get(order.get(i)).screenX(), light.get(order.get(i)).screenY(), light.get(index).screenX(), light.get(index).screenY()) < light.get(index).viewDistance*2)
				return false; //TODO: make this more DRY and readable
		return true;
	}
	
	public void applyTexture(int[] pixels) {
		float taskSize = pixels.length/renderingPool.poolSize;
		for (int i = 0; i < renderingPool.poolSize; ++i) {
			final int j = i;
			renderingPool.post(() -> { applyTextureImpl(pixels, PApplet.round(j*taskSize), PApplet.round((j+1)*taskSize)); });
		}

		renderingPool.forceSync();
	}
	
	private void applyTextureImpl(final int[] pixels, int iBegin, int iEnd) {
		for (int i = iBegin; i < iEnd; ++i) {
			switch (pixels[i]) {
				case Level.FLOOR_NONE: break; //I don't know if this helps speed or not
				case Level.FLOOR_RED:     pixels[i] = textureRed.pixels[i];     break;
				case Level.FLOOR_GREEN:   pixels[i] = textureGreen.pixels[i];   break;
				case Level.FLOOR_BLUE:    pixels[i] = textureBlue.pixels[i];    break;
				case Level.FLOOR_CYAN:    pixels[i] = textureCyan.pixels[i];    break;
				case Level.FLOOR_MAGENTA: pixels[i] = textureMagenta.pixels[i]; break;
				case Level.FLOOR_YELLOW:  pixels[i] = textureYellow.pixels[i];  break;
				case Level.FLOOR_BLACK:   pixels[i] = textureGrey.pixels[i];    break;
				case Level.FLOOR_WHITE:   pixels[i] = textureWhite.pixels[i];   break;
			}
		}
	}
	
	public void addContext(Integer state, List<Renderable> targets) {
		states.put(state, targets);
	}
	
	public void renderContext(Integer state) {
		for (Renderable r : states.get(state))
			r.render();
	}
	
	public void renderEntities(Level level) {
		level.objective.render();
		level.player.render();
		for (Enemy e : level.enemies)
			e.render();
	}
}
