package net.kopeph.ld31.graphics;

import java.util.Arrays;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.Level;
import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.util.ThreadPool;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PImage;

/** @author stuntddude */
public class Renderer {
	public PImage textureRed    , rawTextureRed;
	public PImage textureGreen  , rawTextureGreen;
	public PImage textureBlue   , rawTextureBlue;
	public PImage textureCyan   , rawTextureCyan;
	public PImage textureMagenta, rawTextureMagenta;
	public PImage textureYellow , rawTextureYellow;
	public PImage textureGrey   , rawTextureGrey;
	public PImage textureWhite  , rawTextureWhite;
	public PImage textureBlack  , rawTextureBlack;
	public Font font;

	public int viewX = 0, viewY = 0;

	private final LD31 context;
	private final ThreadPool renderingPool = new ThreadPool();

	public Renderer() {
		context = LD31.getContext();

		//load raw textures
		rawTextureRed     = context.loadImage("res/textures/red-background.jpg"    ); //$NON-NLS-1$
		rawTextureGreen   = context.loadImage("res/textures/green-background.jpg"  ); //$NON-NLS-1$
		rawTextureBlue    = context.loadImage("res/textures/blue-background.jpg"   ); //$NON-NLS-1$
		rawTextureCyan    = context.loadImage("res/textures/cyan-background.jpg"   ); //$NON-NLS-1$
		rawTextureMagenta = context.loadImage("res/textures/magenta-background.jpg"); //$NON-NLS-1$
		rawTextureYellow  = context.loadImage("res/textures/yellow-background.jpg" ); //$NON-NLS-1$
		rawTextureGrey    = context.loadImage("res/textures/grey-background.jpg"   ); //$NON-NLS-1$
		rawTextureWhite   = context.loadImage("res/textures/white-background.jpg"  ); //$NON-NLS-1$
		rawTextureBlack   = context.loadImage("res/textures/black-background.jpg"  ); //$NON-NLS-1$

		font = new Font("res/font-16-white.png"); //$NON-NLS-1$
	}

	public void cropTextures(int width, int height) {
		textureRed     = Util.crop(rawTextureRed    , width, height);
		textureGreen   = Util.crop(rawTextureGreen  , width, height);
		textureBlue    = Util.crop(rawTextureBlue   , width, height);
		textureCyan    = Util.crop(rawTextureCyan   , width, height);
		textureMagenta = Util.crop(rawTextureMagenta, width, height);
		textureYellow  = Util.crop(rawTextureYellow , width, height);
		textureGrey    = Util.crop(rawTextureGrey   , width, height);
		textureWhite   = Util.crop(rawTextureWhite  , width, height);
		textureBlack   = Util.crop(rawTextureBlack  , width, height);
	}

	public void calculateLighting(int[] lighting, Level level) {
		viewX = level.player.x() - context.lastWidth/2;
		viewY = level.player.y() - context.lastHeight/2;

		Arrays.fill(lighting, Level.FLOOR_NONE);

		//crop the tiles array into the pixels array
		//assumes the bottom left of the screen won't be outside the level unless the level is smaller than the screen
		int sourceX = PApplet.max(viewX, 0);
		int sourceY = PApplet.max(viewY, 0);

		int destinationX = PApplet.max(-viewX, 0);
		int destinationY = PApplet.max(-viewY, 0);

		int cropWidth = PApplet.min(level.LEVEL_WIDTH - sourceX, context.lastWidth - destinationX);
		int cropHeight = PApplet.min(level.LEVEL_HEIGHT - sourceY, context.lastHeight - destinationY);

		for (int y = destinationY; y < destinationY + cropHeight; ++y) {
			System.arraycopy(level.tiles, (y + viewY)*level.LEVEL_WIDTH + sourceX, lighting, y*context.lastWidth + destinationX, cropWidth);
		}

		for (final Enemy e : level.enemies) {
			//only render enemies that have a chance of casting light into the scene
			if (e.screenX() > -e.viewDistance + 1 && e.screenX() < context.lastWidth + e.viewDistance - 2 &&
				e.screenY() > -e.viewDistance + 1 && e.screenY() < context.lastHeight + e.viewDistance - 2) {
				//TODO: distance formula to check for circle intersection with screen corners (minor optimization to ignore certain enemies)

				//create a new thread to run the lighting process of each enemy
				renderingPool.post(() -> { e.rayTrace(lighting, e.viewDistance, e.color); });
			}
		}

		renderingPool.forceSync();
	}

	public void applyTexture(final int[] pixels) {
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
				case Level.FLOOR_NONE:    pixels[i] = textureBlack.pixels[i];   break;
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

	public void applyTextureAlt(final int[] pixels) {
		float taskSize = context.height/renderingPool.poolSize;
		for (int i = 0; i < renderingPool.poolSize; ++i) {
			final int j = i;
			renderingPool.post(() -> { applyTextureAltImpl(pixels, PApplet.round(j*taskSize), PApplet.round((j+1)*taskSize)); });
		}

		renderingPool.forceSync();
	}

	private void applyTextureAltImpl(final int[] pixels, final int yBegin, final int yEnd) {
		final int width = context.lastWidth;
		final int height = context.lastHeight;
		final int originX = viewX;
		final int originY = viewY;

		for (int dy = yBegin; dy < yEnd; ++dy) {
			for (int dx = 0; dx < width; ++dx) {
				final int sx = Math.floorMod(dx + originX, width);
				final int sy = Math.floorMod(dy + originY, height);

				final int di = dy*width + dx;
				final int si = sy*width + sx;

				switch (pixels[di]) {
					case Level.FLOOR_NONE:    pixels[di] = textureBlack.pixels[si];   break;
					case Level.FLOOR_RED:     pixels[di] = textureRed.pixels[si];     break;
					case Level.FLOOR_GREEN:   pixels[di] = textureGreen.pixels[si];   break;
					case Level.FLOOR_BLUE:    pixels[di] = textureBlue.pixels[si];    break;
					case Level.FLOOR_CYAN:    pixels[di] = textureCyan.pixels[si];    break;
					case Level.FLOOR_MAGENTA: pixels[di] = textureMagenta.pixels[si]; break;
					case Level.FLOOR_YELLOW:  pixels[di] = textureYellow.pixels[si];  break;
					case Level.FLOOR_BLACK:   pixels[di] = textureGrey.pixels[si];    break;
					case Level.FLOOR_WHITE:   pixels[di] = textureWhite.pixels[si];   break;
				}
			}
		}
	}

	public void renderEntities(Level level) {
		level.objective.render();
		level.player.render();
		for (Enemy e : level.enemies)
			e.render();
	}
}
