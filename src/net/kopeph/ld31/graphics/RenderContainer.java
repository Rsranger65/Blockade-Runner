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
package net.kopeph.ld31.graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kopeph.ld31.spi.Renderable;

/**
 * @author alexg
 */
public class RenderContainer implements Renderable {
	private List<Renderable> renderObjects = new ArrayList<>();
	private List<Integer> zIndices = new ArrayList<>();

	public void registerObject(Renderable object, int zIndex) {
		//Live binary insertion sort
		int insertIndex = Arrays.binarySearch(zIndices.toArray(), zIndex);
		if (insertIndex < 0)
			insertIndex = -insertIndex - 1;
		renderObjects.add(insertIndex, object);
		zIndices.add(insertIndex, zIndex);
	}

	public boolean unregisterObject(Renderable object) {
		return renderObjects.remove(object);
	}

	@Override
	public void render() {
		for (Renderable r : renderObjects)
			r.render();
	}
}
