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

import processing.core.PImage;

/**
 *
 * @author alexg
 */
class PContextImage extends ContextImage {
	final PImage pImage;
	PContextImage(PImage img) {
		this.pImage = img;
	}

	@Override
	public ContextImage crop(int x, int y, int w, int h) {
		PContextImage cropped = PGraphicsContext.getInstance0().createImage0(w, h, 0x00000000);
		cropped.pImage.copy(pImage, x, y, w, h, 0, 0, w, h);

		return cropped;
	}

	@Override
	public void loadPixels() {
		pImage.loadPixels();
	}

	@Override
	public int[] pixels() {
		return pImage.pixels;
	}

	@Override
	public void updatePixels() {
		pImage.updatePixels();
	}

	@Override
	public int width() {
		return pImage.width;
	}

	@Override
	public int height() {
		return pImage.height;
	}
}
