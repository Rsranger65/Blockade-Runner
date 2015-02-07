package net.kopeph.ld31;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.spi.Interaction;
import net.kopeph.ld31.spi.KeyPredicate;
import net.kopeph.ld31.util.OneToManyBiMap;
import net.kopeph.ld31.util.Util;
import processing.core.PConstants;

/**
 * Do NOT send PConstants values to keyId parameters. use Input.K_* values instead.
 * @author alexg
 */
public class Input {
	public static final int
		K_UNBOUND = -1, //These fill both halves of the word, so they can't collide
		K_BINDING = -2,

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

	private Map<String, Interaction> actions = new HashMap<>();
	private OneToManyBiMap<String, Integer> keyMap = new OneToManyBiMap<>(K_UNBOUND);
	private Preferences keyMapStorage = Preferences.userNodeForPackage(getClass());
	private List<KeyPredicate> triggers = new ArrayList<>();


	public void eventKey(char pKey, int pKeyCode, boolean isDown) {
		//Need to blacklist/transform a key from processing? do it here.

		//These are the same thing on the keyboard...
		if (pKey == PConstants.RETURN)
			pKey = PConstants.ENTER;

		if (pKeyCode > 0xFFFF)
			//this breaks the 1:1 assumption described below, so nix it
			return;

		//ALL CAPS FOR CASE INSENSITIVITY
		pKey = Character.toUpperCase(pKey);

		//chars (pKey) are only 16 bits in size, and all pKeyCodes that we care about
		//will also fit into 16 bits, so for simplicity in other parts of the code,
		//we are squashing these together into an int (0xAAAABBBB) A: pKeyCode B: pKey
		//We only care about the A values if B says we should, so really keyId =
		//0x0000BBBB when B != CODED
		//0xAAAA0000 when A == CODED
		int keyId = (pKey == PConstants.CODED) ? pKeyCode << 16 : pKey;

		keyStates.put(keyId, isDown);

		if (isDown) {
			//Triggers take precedence
			for (int i = 0; i < triggers.size(); i++) {
				if (triggers.get(i).press(keyId, true)) {
					triggers.remove(i);
					return;
				}
			}
			if (keyMap.containsValue(keyId))
				actions.get(keyMap.getRev(keyId)).interact(true);
		}
	}

	public void addAction(String id, Interaction lambda, Integer...keyIds) {
		addAction(id, lambda, Arrays.asList(keyIds));
	}

	public void addAction(String id, Interaction lambda, List<Integer> keyIds) {
		actions.put(id, lambda);
		if (!loadKeys(id))
			keyMap.put(id, keyIds);
	}

	public void setKey(String id, int index, int keyId) {
		keyMap.putIndex(id, index, keyId);
		saveKeys(id);
	}


	private boolean loadKeys(String id) {
		String property = keyMapStorage.get(id, null);
		if (property == null)
			return false;

		String[] properties = property.split(","); //TODO: extract to constant
		if (properties[0].isEmpty())
			return true;

		for (int i = 0; i < properties.length; i++)
			keyMap.putIndex(id, i, Integer.parseInt(properties[i]));
		return true;
	}

	private void saveKeys(String id) {
		keyMapStorage.put(id, Util.join(keyMap.get(id), ","));
	}

	public void addMonitor(String id, Integer...keyIds) {
		addMonitor(id, Arrays.asList(keyIds));
	}

	public void addMonitor(String id, List<Integer> keyIds) {
		addAction(id, (down) -> { /* no action */ }, keyIds);
	}

	/**
	 * Returns a mapping between ids and a list of button titles for the settings menu.
	 */
	public Map<String, List<String>> keyMap() {
		Map<String, List<String>> retMap = new HashMap<>();

		//KeyMap output
		for (Entry<String, List<Integer>> entry : keyMap.entrySet())
			retMap.put(entry.getKey(), getKeyTitle(entry.getValue()));

		return retMap;
	}

	public String keyMap(String id, int index) {
		if (keyMap.get(id).size() == 0)
			return getKeyTitle(K_UNBOUND);
		return getKeyTitle(keyMap.get(id).get(index));
	}

	public static String getKeyTitle(int keyId) {
		if (keyId >= 0x21 && keyId <= 0xFFFF) //UTF-16 range minus SPACE and control codes
			return String.valueOf((char) keyId);
		switch (keyId) {
		case ' '      : return "SPACE";
		case K_UNBOUND: return "---";
		case K_BINDING: return "<???>";
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

	public static List<String> getKeyTitle(List<Integer> keyIds) {
		List<String> retList = new ArrayList<>();
		for (Integer keyId : keyIds)
			retList.add(getKeyTitle(keyId));
		return retList;
	}

	public boolean getKey(int keyId) {
		if (keyStates.containsKey(keyId))
			return keyStates.get(keyId);
		return false;
	}

	public boolean getKey(String id) {
		for (Integer keyId : keyMap.get(id)) {
			if (getKey(keyId))
				return true;
		}
		return false;
	}

	public void handleBind(String id, final int index, final String escapeId) {
		//Set key to ???
		keyMap.putIndex(id, index, K_BINDING);

		//Wait until a key is pressed, and lock onto it
		postTrigger((keyId, down) -> {
			if (getKey(escapeId))
				keyId = K_UNBOUND;

			setKey(id, index, keyId);
			//Capture and remove
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
}
