package net.kopeph.ld31;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.menu.Button;
import net.kopeph.ld31.spi.Interaction;
import processing.core.PConstants;

/** @author stuntddude */
public final class InputHandler {
	//enumeration of controlCodes
	public static final int
		CTL_UP       = 0,
		CTL_LEFT     = 1,
		CTL_DOWN     = 2,
		CTL_RIGHT    = 3,
		CTL_RESET    = 4,
		CTL_PAUSE    = 5,
		CTL_ESCAPE   = 6,
		CTL_UNCAUGHT = 7;

	public static String getControlString(int controlCode) {
		switch (controlCode) {
			case CTL_UP    : return "    UP";
			case CTL_LEFT  : return "  LEFT";
			case CTL_DOWN  : return "  DOWN";
			case CTL_RIGHT : return " RIGHT";
			case CTL_RESET : return " RESET";
			case CTL_PAUSE : return " PAUSE";
			case CTL_ESCAPE: return "ESCAPE";
		}
		return "UNCAUGHT";
	}

	//enumeration of keyIds (should match results of convertToKeyId())
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

	public static String getKeyIdString(int keyId) {
		if (keyId >= 0x21 && keyId <= 0xFFFF) //UTF-16 range minus SPACE and control codes
			return String.valueOf((char) keyId);
		switch (keyId) {
			case ' '      : return "SPACE";
			case K_UNBOUND: return "---";
			case K_BINDING: return "<???>";
			case K_ESC    : return "ESC";
			case K_TAB    : return "TAB";
			case K_ENTER  : return "ENTER";
			case K_BKSP   : return "BKSP";
			case K_SHIFT  : return "SHIFT";
			case K_CTRL   : return "CTRL";
			case K_ALT    : return "ALT";
			case K_UP     : return String.valueOf(Font.ARROW_UP);
			case K_DOWN   : return String.valueOf(Font.ARROW_DOWN);
			case K_LEFT   : return String.valueOf(Font.ARROW_LEFT);
			case K_RIGHT  : return String.valueOf(Font.ARROW_RIGHT);
		}
		return "!!!"; //well shit, we got a weird key
	}



	/** Converts the key and keyCode values given by Processing into a unique keyId for internal use */
	public static int convertToKeyId(char key, int keyCode) {
		//TODO: add documentation explaining keyId values here if we think we need it
		if (key == PConstants.RETURN) key = PConstants.ENTER; //special case - transform '\r' to '\n'
		return (key == PConstants.CODED? (keyCode << 16) : Character.toUpperCase(key));
	}

	/** Loads saved key bindings from system preferences (must be called first, BAE) */
	private void loadKeyIdBindings() {
		if (!pullFromDisk())
			resetKeyIdBindings();
	}

	public void resetKeyIdBindings() {
		keyIdBindings.clear();
		bindKeyIds(new int[] { 'W', '8', InputHandler.K_UP    }, CTL_UP     );
		bindKeyIds(new int[] { 'A', '4', InputHandler.K_LEFT  }, CTL_LEFT   );
		bindKeyIds(new int[] { 'S', '2', InputHandler.K_DOWN  }, CTL_DOWN   );
		bindKeyIds(new int[] { 'D', '6', InputHandler.K_RIGHT }, CTL_RIGHT  );
		bindKeyIds(new int[] { 'R', ' ', InputHandler.K_ENTER }, CTL_RESET  );
		bindKeyIds(new int[] { 'P',      InputHandler.K_TAB   }, CTL_PAUSE  );
		bindKeyId(K_ESC, CTL_ESCAPE);
	}



	public InputHandler() {
		loadKeyIdBindings();

		final LD31 context = LD31.getContext();

		bindControlCode(InputHandler.CTL_RESET, (down) -> {
			if (context.gameState() == LD31.ST_RUNNING ||
				context.gameState() == LD31.ST_WIN ||
				context.gameState() == LD31.ST_DIE)
				context.setGameState(LD31.ST_RESET);
		});

		bindControlCode(InputHandler.CTL_PAUSE, (down) -> {
			if (context.gameState() == LD31.ST_RUNNING)
				context.setGameState(LD31.ST_PAUSE);
			else if (context.gameState() == LD31.ST_PAUSE)
				context.setGameState(LD31.ST_RUNNING);
		});

		bindControlCode(InputHandler.CTL_ESCAPE, (down) -> {
			if (context.gameState() == LD31.ST_MENU)
				context.exit();
			else if (context.gameState() == LD31.ST_RUNNING)
				context.setGameState(LD31.ST_PAUSE);
			else if (context.gameState() == LD31.ST_PAUSE)
				context.setGameState(LD31.ST_RUNNING);
			else if (context.gameState() == LD31.ST_KEYBIND)
				context.setGameState(LD31.ST_SETTINGS);
			else
				context.setGameState(LD31.ST_MENU);
		});
	}



	private Map<Integer, Integer> keyIdBindings = new HashMap<>();

	/** Binds a given keyId to a given controlCode, overwriting any existing bindings of the keyId */
	private void bindKeyId(int keyId, int controlCode) {
		keyIdBindings.put(keyId, controlCode);
		pushToDisk(); //save bindings to system preferences as we supply them
	}

	/** Binds all given KeyIds to a given controlCode, overwriting any existing bindings for each keyId */
	private void bindKeyIds(int[] keyIds, int controlCode) {
		for (int keyId : keyIds)
			bindKeyId(keyId, controlCode);
	}

	/** Removes the binding for the given keyId, if one exists */
	private void unbindKeyId(int keyId) {
		keyIdBindings.remove(keyId);
		pushToDisk(); //update bindings on disk when they are removed as well as when they are added
	}

	/** @return a List of all keyIds bound to the given controlCode */
	public List<Integer> getBoundKeyIdsFor(int controlCode) {
		Set<Integer> fullKeyIdSet = keyIdBindings.keySet();
		List<Integer> boundKeyIds = new ArrayList<>();
		for (int i : fullKeyIdSet) {
			if (keyIdBindings.get(i) == controlCode)
				boundKeyIds.add(i);
		}
		Collections.sort(boundKeyIds);
		return boundKeyIds;
	}

	/** @return the main keyId for the given controlCode, based on a hard-coded preference heuristic */
	public int getMainBindingFor(int controlCode) {
		List<Integer> bindings = getBoundKeyIdsFor(controlCode);
		if (bindings.size() == 0)
			return K_UNBOUND;
		//prefer alphabetic characters, then arrow keys, then numbers, then anything else
		for (int i : bindings)
			if (Character.isAlphabetic(i))
				return i;
		for (int i : bindings)
			if (i == K_UP || i == K_LEFT || i == K_DOWN || i == K_RIGHT)
				return i;
		for (int i : bindings)
			if (Character.isDigit(i))
				return i;
		return bindings.get(0);
	}

	private Map<Integer, Interaction> controlCodeActions = new HashMap<>();

	/** Binds a given controlCode to a given action, such that when the key is pressed, action.on() will be called */
	private void bindControlCode(int controlCode, Interaction action) {
		controlCodeActions.put(controlCode, action);
	}

	private boolean[] controlCodeStates = new boolean[CTL_UNCAUGHT];

	/** @return whether the given controlCode is active (whether its associated keys are pressed down) */
	public boolean isPressed(int controlCode) {
		return controlCodeStates[controlCode];
	}

	private Button bindingButton;
	private int bindingControlCode;

	public void handleBind(Button button, int controlCode) {
		bindingButton = button;
		bindingControlCode = controlCode;
		bindingButton.text = getKeyIdString(K_BINDING);
	}

	public void cancelBind() {
		if (bindingButton != null) {
			bindingButton.text = getKeyIdString(bindingButton.tag);
			bindingButton = null;
		}
	}

	/** Handles key presses and releases based on current key bindings */
	public void handleKeyEvent(int keyId, boolean down) {
		if (bindingButton != null && down) {
			if (keyId == K_ESC) {
				//allow users to unbind keys with esc
				unbindKeyId(bindingButton.tag);
				bindingButton.tag = K_UNBOUND;
			} else if (!keyIdBindings.containsKey(keyId)) {
				//if there are no conflicts, rebind the key to match
				unbindKeyId(bindingButton.tag);
				bindKeyId(keyId, bindingControlCode);
				bindingButton.tag = keyId;
			}

			//if there is a conflict with an existing binding, do nothing - the button be reset to its previous state by cancelBind()
			cancelBind();
		} else if (keyIdBindings.containsKey(keyId)) {
			int controlCode = keyIdBindings.get(keyId);

			//keep track of key state
			controlCodeStates[controlCode] = down;

			//activate any associated Interactions
			if (down && controlCodeActions.containsKey(controlCode)) {
				controlCodeActions.get(controlCode).interact(down);
			}
		}
	}

	private static final String PREF_SUBNODE = "inputv1"; //$NON-NLS-1$
	private Preferences diskStore = Preferences.userNodeForPackage(getClass()).node(PREF_SUBNODE);

	/** @author alexg */
	private void pushToDisk() {
		resetDiskPreferences();

		for (Map.Entry<Integer, Integer> entry : keyIdBindings.entrySet())
			diskStore.put(entry.getKey().toString(), entry.getValue().toString());
	}

	/**
	 * @return true if settings were found and correctly loaded,
	 *         false indicates nothing happened.
	 * @author alexg
	 */
	private boolean pullFromDisk() {
		boolean pulledAnId = false;

		try {
			for(String key : diskStore.keys()) {
				keyIdBindings.put(Integer.parseInt(key), Integer.parseInt(diskStore.get(key, null)));
				pulledAnId = true;
			}
		} catch (BackingStoreException e) {
			return false;
		}

		return pulledAnId;
	}

	/** @author alexg */
	private void resetDiskPreferences() {
		try {
			for(String key : diskStore.keys())
				diskStore.remove(key);
		} catch (BackingStoreException e) {
			//If this errors out, there is nothing to be done
		}
	}
}
