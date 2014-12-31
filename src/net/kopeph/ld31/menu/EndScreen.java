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
package net.kopeph.ld31.menu;

import net.kopeph.ld31.LD31;
import net.kopeph.ld31.entity.Renderable;
import net.kopeph.ld31.graphics.Font;
import processing.core.PApplet;

/**
 *
 * @author alexg
 */
public class EndScreen implements Renderable {
	private static final int SWEEP_SPEED = 8;
	private static final int DARKEN_SPEED = 255;

	private final PApplet context = LD31.getContext();
	private final Font font;
	private final String title, footer;
	private final int backColor;
	private int phase;

	public EndScreen(Font font, String title, String footer, int backColor) {
		this.font = font;
		this.title = title;
		this.footer = footer;
		this.backColor = backColor;
	}

	@Override
	public void render() {
		++phase;

		if (phase > 0) {
			context.fill(backColor);
			context.rect(0, context.height/2 - phase*SWEEP_SPEED, context.width, 2*phase*SWEEP_SPEED);
			font.render(title, context.width/2, context.height/2);
			font.render(footer, 8, context.height - 16);

			if (phase * SWEEP_SPEED >= context.height / 2)
				phase = -DARKEN_SPEED;
		}
		else if (phase < 0) {
			context.background(context.red  (backColor)*phase/-DARKEN_SPEED,
							   context.green(backColor)*phase/-DARKEN_SPEED,
					           context.blue (backColor)*phase/-DARKEN_SPEED);
			font.render(title, context.width/2, context.height/2);
			font.render(footer, 8, context.height - 16);
		}
		else {
			context.noLoop();
		}
	}
}
