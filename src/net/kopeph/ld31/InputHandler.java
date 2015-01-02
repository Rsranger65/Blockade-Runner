package net.kopeph.ld31;


/**
 * @author mILES
 */
public class InputHandler {
	public static final int
		UP      = 0,
		LEFT    = 1,
		DOWN    = 2,
		RIGHT   = 3;
		//RESTART = 4;

	private static String[][] bindings = {
		{"W", "UP"},
		{"A", "LEFT"},
		{"S", "DOWN"},
		{"D", "RIGHT"}
		//{" ", "ENTER"}
	};

	private static boolean[][] pressed = new boolean[bindings.length][2];

	public static void handleInput(String keyId, boolean down) {
		if (keyId.isEmpty()) return;

		for (int i = 0; i < bindings.length; ++i) {
			for (int j = 0; j < bindings[i].length; ++j) {
				if (bindings[i][j].equals(keyId)) {
					setPressed(i, j, down);
				}
			}
		}
	}

	private static void setPressed(int i, int j, boolean down) {
		pressed[i][j] = down;
	}

	public static boolean isPressed(int keyConstant) {
		for (int j = 0; j < pressed[keyConstant].length; j++)
			if (pressed[keyConstant][j]) return true;
		return false;
	}
}
