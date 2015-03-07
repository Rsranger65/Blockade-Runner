package net.kopeph.ld31;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.menu.MenuButton;
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
		return (key == PConstants.CODED? (keyCode << 16) : Character.toUpperCase(key));
	}
	
	/** Loads saved key bindings from system preferences (must be called first, BAE) */
	public void loadKeyIdBindings() {
		resetKeyIdBindings();
		//XXX: stub
	}
	
	public void resetKeyIdBindings() {
		keyIdBindings.clear();
		bindKeyIds(new int[] { (int)'W', (int)'8', InputHandler.K_UP    }, CTL_UP     );
		bindKeyIds(new int[] { (int)'A', (int)'4', InputHandler.K_LEFT  }, CTL_LEFT   );
		bindKeyIds(new int[] { (int)'S', (int)'2', InputHandler.K_DOWN  }, CTL_DOWN   );
		bindKeyIds(new int[] { (int)'D', (int)'6', InputHandler.K_RIGHT }, CTL_RIGHT  );
		bindKeyIds(new int[] { (int)'R', (int)' ', InputHandler.K_ENTER }, CTL_RESET  );
		bindKeyIds(new int[] { (int)'P',           InputHandler.K_TAB   }, CTL_PAUSE  );
		bindKeyId(K_ESC, CTL_ESCAPE);
	}
	
	
	
	private Map<Integer, Integer> keyIdBindings = new HashMap<>();
	
	/** Binds a given keyId to a given controlCode, overwriting any existing bindings of the keyId */
	public void bindKeyId(int keyId, int controlCode) {
		keyIdBindings.put(keyId, controlCode);
		//TODO: save bindings to system preferences as we supply them
	}
	
	/** Binds all given KeyIds to a given controlCode, overwriting any existing bindings for each keyId */
	public void bindKeyIds(int[] keyIds, int controlCode) {
		for (int keyId : keyIds)
			bindKeyId(keyId, controlCode);
	}
	
	/** Removes the binding for the given keyId, if one exists */
	public void unbindKeyId(int keyId) {
		keyIdBindings.remove(keyId);
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
	public void bindControlCode(int controlCode, Interaction action) {
		controlCodeActions.put(controlCode, action);
	}
	
	private boolean[] controlCodeStates = new boolean[CTL_UNCAUGHT];
	
	/** @return whether the given controlCode is active (whether its associated keys are pressed down) */
	public boolean isPressed(int controlCode) {
		return controlCodeStates[controlCode];
	}
	
	private MenuButton bindingButton;
	private int bindingControlCode;
	
	public void handleBind(MenuButton button, int controlCode) {
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
}
