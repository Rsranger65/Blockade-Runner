/*
 * Copyright 2014 Alex Gittemeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kopeph.ld31.graphics.context;

import net.kopeph.ld31.spi.Action;
import net.kopeph.ld31.spi.KeyHandler;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 *
 * @author alexg
 */
class PGraphicsContext extends GraphicsContext {
	Action setup, render;
	KeyHandler keyDown, keyUp;
	PApplet pCtx;

	static PGraphicsContext getInstance0() { return (PGraphicsContext) GraphicsContext.getInstance(); }
	static void setPContext0(PApplet pCtx) { getInstance0().pCtx = pCtx; }
	@Override protected void init(Action setup, Action render) {
		this.setup = setup;
		this.render = render;
		PApplet.main(Hook.class.getName());
	}

	@Override public void setKeyHandlers(KeyHandler keyDown, KeyHandler keyUp) { this.keyDown = keyDown; this.keyUp = keyUp; }
	@Override public void size(int width, int height) { pCtx.size(width, height); }
	@Override public void frameRate(int fps) { pCtx.frameRate(fps); }
	@Override public void setFrameTitle(String title) { pCtx.frame.setTitle(title); }
	@Override public void allowResize(boolean resizable) { pCtx.frame.setResizable(resizable); }
	@Override public void loop(boolean loop) {
		if (loop)
			pCtx.loop();
		else
			pCtx.noLoop();
	}
	@Override public void exit() { pCtx.exit(); }

	@Override public int width() { return pCtx.width; }
	@Override public int height() { return pCtx.height; }
	@Override public int frameCount() { return pCtx.frameCount; }
	@Override public double millis() { return pCtx.millis(); }
	@Override public double frameRate() { return pCtx.frameRate; }
	@Override public void fill(int color) { pCtx.fill(color, a(color)); }
	@Override public void stroke(int color) { pCtx.stroke(color, a(color)); }
	@Override public void noFill() { pCtx.noFill(); }
	@Override public void noStroke() { pCtx.noStroke(); }
	@Override public void background(int color) { pCtx.background(color, a(color)); }
	@Override public void rect(int x, int y, int width, int height) { pCtx.rect(x, y, width, height); }
	@Override public void rect(int x, int y, int width, int height, int cornerRadius) { pCtx.rect(x, y, width, height, cornerRadius); }
	@Override public void image(ContextImage image, int x, int y) {
		if (image instanceof PGraphicsContext)
			return; //Painting this onto itself is a no-op
		pCtx.image(((PContextImage)image).pImage, x, y);
	}

	@Override public void loadPixels() { pCtx.loadPixels(); }
	@Override public int[] pixels() { return pCtx.pixels; }
	@Override public void updatePixels() { pCtx.updatePixels(); }
	@Override public ContextImage crop(int x, int y, int w, int h) {
		PContextImage image = createImage0(pCtx.width, pCtx.height, 0x00000000);
		image.pImage.loadPixels();
		for (int i = 0; i < image.pImage.pixels.length; i++)
			image.pImage.pixels[i] = pCtx.pixels[i];
		image.pImage.updatePixels();

		return image.crop(x, y, w, h);
	}

	@Override public int mouseX() { return pCtx.mouseX; }
	@Override public int mouseY() { return pCtx.mouseY; }
	@Override public boolean mousePressed() { return pCtx.mousePressed;}
	@Override public boolean mouseLeftButton() { return (pCtx.mouseButton & PConstants.LEFT) != 0; }
	@Override public boolean mouseRightButton() { return (pCtx.mouseButton & PConstants.RIGHT) != 0; }

	@Override public ContextImage loadImage(String filename) { return loadImage0(filename); }
	PContextImage loadImage0(String filename) { return new PContextImage(pCtx.loadImage(filename)); }
	@Override public ContextImage createImage(int w, int h, int color) { return createImage0(w, h, color); }
	PContextImage createImage0(int w, int h, int color) {
		PContextImage image = new PContextImage(pCtx.createImage(w, h, PConstants.ARGB));
		image.pImage.loadPixels();
		for (int i = 0; i < image.pImage.pixels.length; i++)
			image.pImage.pixels[i] = color;
		image.pImage.updatePixels();

		return image;
	}

	@Override public int color(int color,           int a) { return color & 0xFFFFFF | ((a & 0xFF) << 24); }
	@Override public int color(int r, int g, int b)        { return color(r, g, b, 0xFF); }
	@Override public int color(int r, int g, int b, int a) {
		return ((a & 0xFF) << 24) |
			   ((r & 0xFF) << 16) |
			   ((g & 0xFF) << 8 ) |
			   ((b & 0xFF) << 0 );
	}

	@Override public int a(int color) { return (color >>> 24) & 0xFF; }
	@Override public int r(int color) { return (color >>> 16) & 0xFF; }
	@Override public int g(int color) { return (color >>>  8) & 0xFF; }
	@Override public int b(int color) { return (color >>>  0) & 0xFF; }

	public static class Hook extends PApplet {
		private static final long serialVersionUID = 1L;
		private final PGraphicsContext ctx = PGraphicsContext.getInstance0();

		@Override public void setup() { ctx.pCtx = this; ctx.setup.act(); }
		@Override public void draw() {                   ctx.render.act(); }

		@Override public void keyPressed() {
			ctx.keyDown.onKey(keyCodeName());
			//capture ESC key so it doesn't quit the program
			key = 0;
		}
		@Override public void keyReleased() { ctx.keyUp.onKey(keyCodeName()); }
		private String keyCodeName() {
			if (key >= ' ' && key <= '~') //All ASCII printables
				return String.valueOf(key);
			if (key == CODED) {
				switch (keyCode) {
				case UP       : return "UP"; //$NON-NLS-1$
				case DOWN     : return "DOWN"; //$NON-NLS-1$
				case LEFT     : return "LEFT"; //$NON-NLS-1$
				case RIGHT    : return "RIGHT"; //$NON-NLS-1$
				case ALT      : return "ALT"; //$NON-NLS-1$
				case CONTROL  : return "CTRL"; //$NON-NLS-1$
				case SHIFT    : return "SHIFT"; //$NON-NLS-1$
				}
				System.err.println("Unknown keypress: for keyCode=" + keyCode);
			}
			else {
			switch (key) {
				case BACKSPACE: return "BACKSPACE"; //$NON-NLS-1$
				case TAB      : return "TAB"; //$NON-NLS-1$
				case ENTER    :
				case RETURN   : return "ENTER"; //$NON-NLS-1$
				case ESC      : return "ESC"; //$NON-NLS-1$
				case DELETE   : return "DELETE"; //$NON-NLS-1$
				}
				System.err.println("Unknown keypress: for key=" + key);
			}
			return(""); //$NON-NLS-1$
		}
	}
}
