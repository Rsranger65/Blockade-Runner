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

/**
 *
 * @author alexg
 */
public abstract class GraphicsContext extends ContextImage {
	public static final String PROCESSING = "net.kopeph.ld31.graphics.context.PGraphicsContext";
	public final int COLOR_BLACK   = color(  0,   0,   0),
					 COLOR_BLUE    = color(  0,   0, 255),
					 COLOR_GREEN   = color(  0, 255,   0),
					 COLOR_CYAN    = color(  0, 255, 255),
					 COLOR_GREY    = color(127, 127, 127),
					 COLOR_RED     = color(255,   0,   0),
					 COLOR_MAGENTA = color(255,   0, 255),
					 COLOR_ORANGE  = color(255, 127,   0),
					 COLOR_PINK    = color(255, 127, 127),
					 COLOR_YELLOW  = color(255, 255,   0),
					 COLOR_WHITE   = color(255, 255, 255);

	static GraphicsContext context;

	public static void init(String provider, Action setup, Action render) {
		try {
			context = (GraphicsContext) Class.forName(provider).newInstance();
		} catch (ReflectiveOperationException | ClassCastException e) {
			throw new IllegalArgumentException(provider + " must have a visible, blank constructor and be a GraphicsContext.", e);
		}

		context.init(setup, render);
	}

	public static GraphicsContext getInstance() {
		return context;
	}

	protected abstract void init(Action setup, Action render);
	public abstract void setKeyHandlers(KeyHandler keyDown, KeyHandler keyUp);

	public abstract void size(int width, int height);
	public abstract void frameRate(int fps);
	public abstract void setFrameTitle(String title);
	public abstract void allowResize(boolean resize);
	public abstract void loop(boolean loop);
	public abstract void exit();

	public abstract int frameCount();
	public abstract double millis();
	public abstract double frameRate();

	public abstract void fill(int color);
	public abstract void stroke(int color);
	public abstract void noFill();
	public abstract void noStroke();
	public abstract void background(int color);
	public abstract void rect(int x, int y, int width, int height);
	public abstract void rect(int x, int y, int width, int height, int cornerRadius);
	public abstract void image(ContextImage image, int x, int y);

	public abstract int mouseX();
	public abstract int mouseY();
	public abstract boolean mousePressed();
	public abstract boolean mouseLeftButton();
	public abstract boolean mouseRightButton();

	public abstract ContextImage loadImage(String filename);
	public abstract ContextImage createImage(int w, int h, int color);
	public abstract int color(int color, int a);
	public abstract int color(int r, int g, int b);
	public abstract int color(int r, int g, int b, int a);
	public abstract int a(int color);
	public abstract int r(int color);
	public abstract int g(int color);
	public abstract int b(int color);
}
