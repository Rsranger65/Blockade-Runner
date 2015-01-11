package net.kopeph.ld31;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.Interaction;
import net.kopeph.ld31.spi.KeyPredicate;
import processing.core.PConstants;

/**
 * Do NOT send PConstants values to keyId parameters. use Input.K_* values instead.
 * @author alexg
 */
//TODO: make addAction() and handleBind() read and write to a config.
public class Input {
	public static final int
		K_UNBOUND = 1 << 16,
		K_BINDING = 2 << 16,
		K_ESC     = PConstants.ESC,
		K_TAB     = PConstants.TAB,
		K_ENTER   = PConstants.ENTER,
		K_BKSP    = PConstants.BACKSPACE,
		K_SHIFT   = PConstants.SHIFT   << 16,
		K_CTRL    = PConstants.CONTROL << 16,
		K_ALT     = PConstants.ALT     << 16,
		K_UP      = PConstants.UP      << 16,
		K_DOWN    = PConstants.DOWN    << 16,
		K_LEFT    = PConstants.LEFT    << 16,
		K_RIGHT   = PConstants.RIGHT   << 16;

	private Map<Integer, Boolean> keyStates = new HashMap<>();

	private Map<String, KeyAction> actions = new HashMap<>();
	private Map<Integer, KeyAction> keyMap = new HashMap<>();

	private List<KeyPredicate> triggers = new ArrayList<>();

	public void eventKey(char pKey, int pKeyCode, boolean isDown) {
		//Need to blacklist/transform a key from processing? do it here.

		if (pKeyCode > 0xFFFF)
			//this breaks the 1:1 assumption described below, so nix it
			return;

		//ALL CAPS FOR CASE INSENSITIVITY
		pKey = Character.toUpperCase(pKey);

		//This is a way to stuff two values into one because its harder to work
		//with two values. the shift moves past the range of a char, so the
		//resulting value has a 1:1 correspondence, with the assumption that
		//pKeyCode > 0xFFFF
		int keyId = (pKey == PConstants.CODED) ? pKeyCode << 16 : pKey;

		keyStates.put(keyId, isDown);

		if (isDown) {
			//Triggers take precedence
			for (int i = 0; i < triggers.size(); i++) {
				if (triggers.get(i).press(keyId)) {
					triggers.remove(i);
					return;
				}
			}
			if (keyMap.containsKey(keyId))
				keyMap.get(keyId).lambda.interact();
		}
	}

	public void addAction(String id, Interaction lambda, int[] keyIds) {
		KeyAction action = new KeyAction(lambda, keyIds);
		actions.put(id, action);
		for (int keyId : keyIds)
			keyMap.put(keyId, action);
	}

	public Map<Interaction, List<String>> actionKeyMappings() {
		Map<Interaction, List<String>> retMap = new HashMap<>();

		for (KeyAction action : keyMap.values())
			retMap.put(action.lambda, action.keyIdTitles());

		return retMap;
	}

	public static String getKeyTitle(int keyId) {
		if (keyId >= 0x21 && keyId <= 0x7E) //ASCII range minus SPACE
			return String.valueOf((char) keyId);
		switch (keyId) {
		case ' '      : return "SPACE";
		case K_UNBOUND: return "---";
		case K_BINDING: return "???";
		case K_ESC    : return "ESC";
		case K_TAB    : return "TAB";
		case K_ENTER  : return "ENTER";
		case K_SHIFT  : return "SHIFT";
		case K_CTRL   : return "CTRL";
		case K_ALT    : return "ALT";
		case K_UP     : return String.valueOf(Font.ARROW_UP);
		case K_DOWN   : return String.valueOf(Font.ARROW_DOWN);
		case K_LEFT   : return String.valueOf(Font.ARROW_LEFT);
		case K_RIGHT  : return String.valueOf(Font.ARROW_RIGHT);
		}

		//well shit, we got a weird key
		//Should be distinct from others, so we can catch bugs easier
		return "!!!";
	}

	public boolean getKey(int keyId) {
		if (keyStates.containsKey(keyId))
			return keyStates.get(keyId);
		return false;
	}

	public void handleBind(String id, final int index) {
		final KeyAction action = actions.get(id);

		//Remove the binding from keyMap, then set the id in action to ---
		keyMap.remove(action.keyIds.get(index));
		action.keyIds.set(index, K_UNBOUND);

		//Wait until a key is pressed, and lock onto it
		postTrigger((keyId) -> {
			action.keyIds.set(index, keyId);
			return true;
		});
	}

	/**
	 * Predicate should return true to remove from the handler,
	 * or false to keep it in, and allow it to propagate.
	 */
	public void postTrigger(KeyPredicate action) {
		triggers.add(action);
	}

	private static class KeyAction {
		public final Interaction lambda;
		public final List<Integer> keyIds = new ArrayList<>();

		public KeyAction(Interaction lambda, int[] keyIds) {
			this.lambda = lambda;
			for (int keyId : keyIds)
				this.keyIds.add(keyId);
		}

		public List<String> keyIdTitles() {
			List<String> retList = new ArrayList<>();

			for(int keyId : keyIds)
				retList.add(getKeyTitle(keyId));

			return retList;
		}
	}
}
