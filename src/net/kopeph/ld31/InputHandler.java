package net.kopeph.ld31;

import processing.core.PConstants;

/**
 * @author mILES
 */
public class InputHandler {
	public static final int
		UP      = 0,
		DOWN    = 1,
		LEFT    = 2,
		RIGHT   = 3,
		RESTART = 4,
		PAUSE   = 5;
	
	private static int[][] bindings = {
		{ UP     , 'w', 'W', PConstants.UP   , '8' },
		{ DOWN   , 's', 'W', PConstants.DOWN , '2' },
		{ LEFT   , 'a', 'A', PConstants.LEFT , '4' },
		{ RIGHT  , 'd', 'D', PConstants.RIGHT, '6' },
		{ RESTART, ' ',  0 , PConstants.TAB  ,  0  },
		{ PAUSE  , 'p', 'P', PConstants.ENTER,  0  }
	};
	
	private static boolean[] pressed = new boolean[bindings.length];
	
	public static void handleInput(int keyCode, boolean down) {
		if (keyCode == 0) return;
		
		for (int i = 0; i < bindings.length; ++i) {
			for (int j = 1; j < bindings[i].length; ++j) {
				if (bindings[i][j] == keyCode) {
					setPressed(i, j, down);
				}
			}
		}
	}
	
	private static void setPressed(int i, int j, boolean down) {
		pressed[i] = down;
	}
	
	public static boolean isPressed(int keyConstant) {
		return pressed[keyConstant];
	}
}
