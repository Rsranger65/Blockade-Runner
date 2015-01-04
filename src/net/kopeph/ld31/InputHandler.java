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
	
	public static int[][] bindings = {
		{ UP     , 'w', 'W', '8' , PConstants.UP    << 16 },
		{ DOWN   , 's', 'S', '2' , PConstants.DOWN  << 16 },
		{ LEFT   , 'a', 'A', '4' , PConstants.LEFT  << 16 },
		{ RIGHT  , 'd', 'D', '6' , PConstants.RIGHT << 16 },
		{ RESTART, 'r', 'R', ' ' , PConstants.ENTER << 16 },
		{ PAUSE  , 'p', 'P', '\t', PConstants.TAB   << 16 },
		{ ESCAPE ,  0 ,  0 ,  0  , PConstants.ESC   << 16 }
	};
	
	private static boolean[] pressed = new boolean[bindings.length];
	
	private static Map<Integer, Interaction> behaviors = new HashMap<>();
	
	public static void handleInput(int key, int keyCode, boolean down) {
		if (key == 0 || (key == PConstants.CODED && keyCode == 0)) return; //just to be safe, like, you never know, y'know?
		
		//the canonical values of PApplet.keyCode overlap with the ASCII set, which is annoying and means we have to do a bit shift
		if ((keyCode & 0xFFFF0000) != 0) return; //no truncation
		keyCode = keyCode << 16; //key is originally a char, so this way we clear any possibility of overlap
		
		for (int i = 0; i < bindings.length; ++i) {
			for (int j = 1; j < bindings[i].length; ++j) {
				if (bindings[i][j] == key || bindings[i][j] == keyCode) {
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
		//TODO: consider making this search for the value instead of assuming that it will map correctly to the array
		return pressed[keyConstant];
	}
	
	public static void addBehaviour(char key, Interaction behavior) {
		behaviors.put((int)key, behavior);
	}
	
	public static void addBehavior(int keyCode, Interaction behavior) {
		if ((keyCode & 0xFFFF0000) != 0) return; //no truncation
		//TODO: find a way to differentiate between key-down and key-up events
		behaviors.put(keyCode << 16, behavior);
	}
	
	public static String toKey(int keyCode) {
		String ret = toKeyImpl(keyCode);
		if (ret != "") return ret;
		ret = toKeyImpl(keyCode >> 16);
		if (ret != "") return ret;
		return "---";
	}
	
	private static String toKeyImpl(int keyCode) {
		switch(keyCode) {
		case PConstants.TAB: case PConstants.TAB << 16: return "TAB";
		case PConstants.ESC << 16: return "ESC";
		case PConstants.ENTER << 16: return "ENTER";
		case ' ': return "SPACE";
		//TODO: add cases for arrow keys
		}
		
		if (keyCode > 0 && keyCode < 128)
			return "" + (char)keyCode;
		return ""; //no match
	}
}
