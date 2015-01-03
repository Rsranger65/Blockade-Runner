package net.kopeph.ld31;

import java.util.HashMap;
import java.util.Map;

import net.kopeph.ld31.spi.Interaction;
import processing.core.PConstants;

/**
 * @author stuntddude
 */
public class InputHandler {
	public static final int
		UP      = 0,
		DOWN    = 1,
		LEFT    = 2,
		RIGHT   = 3,
		RESTART = 4,
		PAUSE   = 5,
		ESCAPE  = 6;
	
	private static int[][] bindings = {
		{ UP     , 'w', 'W', '8', PConstants.UP    },
		{ DOWN   , 's', 'S', '2', PConstants.DOWN  },
		{ LEFT   , 'a', 'A', '4', PConstants.LEFT  },
		{ RIGHT  , 'd', 'D', '6', PConstants.RIGHT },
		{ RESTART, 'r', 'R', ' ', PConstants.ENTER },
		{ PAUSE  , 'p', 'P',  0 , PConstants.TAB   },
		{ ESCAPE ,  0 ,  0 ,  0 , PConstants.ESC   }
	};
	
	private static boolean[] pressed = new boolean[bindings.length];
	
	private static Map<Integer, Interaction> behaviors = new HashMap<>();
	
	public static void handleInput(int keyCode, boolean down) {
		if (keyCode == 0) return; //just in case, like, you never know, y'know?
		
		for (int i = 0; i < bindings.length; ++i) {
			for (int j = 1; j < bindings[i].length; ++j) {
				if (bindings[i][j] == keyCode) {
					setPressed(i, j, down);
					//currently only acts on keyDown events
					if (behaviors.containsKey(bindings[i][0]) && down)
						behaviors.get(bindings[i][0]).interact();
					return;
				}
			}
		}
	}
	
	private static void setPressed(int i, int j, boolean down) {
		pressed[i] = down; //this is going to be more complicated later
	}
	
	public static boolean isPressed(int keyConstant) {
		return pressed[keyConstant];
	}
	
	public static void addBehavior(int code, Interaction behavior) {
		//TODO: find a way to differentiate between key-down and key-up events
		behaviors.put(code, behavior);
	}
}
