package net.kopeph.ld31.util;

import processing.core.PApplet;

/**
 * @author alexg
 */
public class Profiler {
	public static final int
		PLAYER_MOVE  = 0,
		LIGHTING     = 1,
		ENEMY_PATH   = 2,
		TEXTURE      = 3,
		ENTITY_DRAW  = 4,
		PIXEL_UPDATE = 5,
		PROFILE_SIZE = 6;
	private static final String[] description = {
		"PLAYER_MOVE", 	//$NON-NLS-1$
		"LIGHTING", 	//$NON-NLS-1$
		"ENEMY_PATH", 	//$NON-NLS-1$
		"TEXTURE", 		//$NON-NLS-1$
		"ENTITY_DRAW", 	//$NON-NLS-1$
		"PIXEL_UPDATE", //$NON-NLS-1$
		"PROFILE_SIZE", //$NON-NLS-1$
	};

	private final long[] startTimeMS = new long[PROFILE_SIZE];
	private final long[]   endTimeMS = new long[PROFILE_SIZE];

	public void start(int task) {
		startTimeMS[task] = System.currentTimeMillis();
	}

	public void end(int task) {
		endTimeMS[task] = System.currentTimeMillis();
	}

	public void swap(int endTask, int startTask) {
		long time = System.currentTimeMillis();
		startTimeMS[startTask] = time;
		  endTimeMS[  endTask] = time;
	}

	public void report(PApplet context) {
		for (int i = 0; i < PROFILE_SIZE; i++)
			System.out.printf("%s: %.3f\n", description[i], (endTimeMS[i] - startTimeMS[i]) / 1000.0); //$NON-NLS-1$
		System.out.printf("FRAMERATE_CUR: %.2f\n", context.frameRate); //$NON-NLS-1$
		System.out.println();
	}
}
