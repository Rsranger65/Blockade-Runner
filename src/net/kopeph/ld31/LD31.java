package net.kopeph.ld31;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.Renderer;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.menu.EndScreen;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.MenuWidget;
import net.kopeph.ld31.menu.TextBox;
import net.kopeph.ld31.util.Profiler;
import processing.core.PApplet;

/** Everything inside here works like it does in processing */
public class LD31 extends PApplet {
	private static final long serialVersionUID = 1L;

	//THESE ARE ALL THE USER MESSAGES
	private static final String MSG_FOOTER =
	"%s%s%s%s: Move %8s: Restart    Objective: Capture the Pink Square    Beware Of: White Light";
	private static final String MSG_FOOTER_END =
	    "           %8s: Restart";
	private static final String MSG_WIN = "YA DID IT!";
	private static final String MSG_DIE = "You ded Jim!"; //Sorry, this project is not MSG free

	private static final String BG_MUSIC = "res/music.mp3"; //file path

	private static final int // Game state enum
		ST_RESET_HARD = -2,  // Window size has changed
		ST_RESET      = -1,  // Level needs regenerated
		ST_RUNNING    =  0,  // Normal Condition
		ST_DIE        =  1,  // Displaying lose screen
		ST_WIN        =  2,  // Displaying win screen
		ST_PAUSE      =  3,  // Displaying Pause Menu
		ST_MENU       =  4,  // Displaying Main Menu
		ST_SETTINGS   =  5,  // Displaying Settings Menu
		ST_CAMPAIGN   =  6;  // Displaying Dummy Campaign menu

	private static LD31 context; //for static access so we don't have to pass this reference around so much
	private static TextBox buildVersion, footer; //HUD text

	private final Profiler profiler = new Profiler();

	private InputHandler input = new InputHandler();
	private Audio audio;
	private Level level;
	private EndScreen win, die;
	private Menu mainMenu, settingsMenu, pauseMenu, dummyCampaignMenu;
	private volatile int gameState;
	private int fadePhase;

	public Renderer renderer;

	@Override
	public void setup() {
		context = this;
		renderer = new Renderer();
		audio = new Audio();

		size(800, 600);
		frameRate(60);
		noStroke();
		frame.setResizable(true);
		frame.setTitle("Blockade Runner");
		//TODO: give the window a custom icon
		
		//Setup Audio
		audio.load(BG_MUSIC, Audio.VOL_MUSIC);
		audio.shiftVolume(Audio.VOL_MUSIC, 0.0F, 1.0F, 10 * 1000); //fade in for 10 seconds
		audio.play(BG_MUSIC, true);

		//setup HUD
		buildVersion = new TextBox(renderer.font, 0, 4, width, 8, buildVersion());
		buildVersion.xAnchor = MenuWidget.ANCHOR_RIGHT;
		buildVersion.xPos    = width - buildVersion.text.length() * 8 - 4;

		footer = new TextBox(renderer.font, 4, height - 12, width, 8, ""); //$NON-NLS-1$
		footer.yAnchor = MenuWidget.ANCHOR_BOTTOM; //XXX: Positioning must match in EndScreen.java

		//setup end screens
		win = new EndScreen(renderer.font, MSG_WIN, "", color(0, 120, 0)); //$NON-NLS-1$
		die = new EndScreen(renderer.font, MSG_DIE, "", color(120, 0, 0)); //$NON-NLS-1$

		//setup main menu
		mainMenu = new Menu();
		mainMenu.add(new TextBox(renderer.font,  "Blockade Runner", 0, -175));
		mainMenu.add(new MenuButton(renderer.font, "Free Play"    , 0, -100, 400, 50, (down) -> { gameState = ST_RESET_HARD; }));
		mainMenu.add(new MenuButton(renderer.font, "Campaign Mode", 0, - 40, 400, 50, (down) -> { gameState = ST_CAMPAIGN;   }));
		mainMenu.add(new MenuButton(renderer.font, "Settings"     , 0, + 20, 400, 50, (down) -> { gameState = ST_SETTINGS;   }));
		mainMenu.add(new MenuButton(renderer.font, "Exit"         , 0, +120, 400, 50, (down) -> { exit();                    }));

		//setup behaviors for keyboard controls
		input.loadKeyIdBindings();
		input.bindControlCode(InputHandler.CTL_RESET, (down) -> {
			if (gameState == ST_RUNNING ||
				gameState == ST_WIN ||
				gameState == ST_DIE) {
				gameState = ST_RESET;
			}
		});
		input.bindControlCode(InputHandler.CTL_PAUSE, (down) -> {
			if (gameState == ST_RUNNING) {
				gameState = ST_PAUSE;
				menuHeight = 1;
			} else if (gameState == ST_PAUSE) {
				gameState = ST_RUNNING;
			}
		});
		input.bindControlCode(InputHandler.CTL_ESCAPE, (down) -> {
			if (gameState == ST_MENU) {
				exit();
			} else if (gameState == ST_RUNNING) {
				gameState = ST_PAUSE;
				menuHeight = 1;
			} else if (gameState == ST_PAUSE) {
				gameState = ST_RUNNING;
			} else {
				gameState = ST_MENU;
			}
		});

		//setup settings menu
		settingsMenu = new Menu();
		settingsMenu.add(new TextBox(renderer.font, "Settings Menu", 0, -175));
		
		//setup key binding buttons and labels
		final int MENU_COLS = 4; //1 label + MENU_COLS buttons per row
		//setup top label row
		for (int col = 1; col < MENU_COLS; ++col) {
			settingsMenu.add(new TextBox(renderer.font, String.valueOf(col), -30*MENU_COLS + 60*col, -150));
		}
		for (int row = 0; row < InputHandler.CTL_ESCAPE; ++row) {
			//setup left label column
			settingsMenu.add(new TextBox(renderer.font, InputHandler.getControlString(row), -30*MENU_COLS, -125 + 30*row));
			
			//setup each row of buttons
			final int r = row;
			List<Integer> bindings = input.getBoundKeyIdsFor(row);
			for (int col = 1; col < MENU_COLS; ++col) {
				final int keyId = (col <= bindings.size()? bindings.get(col - 1) : InputHandler.K_UNBOUND);
				final MenuButton b = new MenuButton(renderer.font, InputHandler.getKeyIdString(keyId),
													-30*MENU_COLS + 60*col, -125 + 30*row, 50, 20, (down) -> { /* dummy argument (gets replaced immediately) */ });
				b.replaceInteraction((down) -> { input.handleBind(b, r); }); //working around the strict lambda capture requirements
				b.tag = keyId;
				settingsMenu.add(b);
			}
		}
		
		settingsMenu.add(new MenuButton(renderer.font, "Back", 0, 120, 400, 50, (down) -> { gameState = ST_MENU; }));

		//setup dummy campaign menu
		dummyCampaignMenu = new Menu();
		dummyCampaignMenu.add(new TextBox(renderer.font, "Campaign Mode", 0, -175));
		dummyCampaignMenu.add(new MenuButton(renderer.font, "Back", 0, -100, 400, 50, (down) -> { gameState = ST_MENU; }));
		dummyCampaignMenu.add(new TextBox(renderer.font, "This game mode hasn't been implemented yet :(", 0,  150));

		//setup pause menu
		pauseMenu = new Menu(); //Dynamic size. see drawPause();
		pauseMenu.add(new TextBox(renderer.font, "Game Paused", 0, -200));
		pauseMenu.add(new MenuButton(renderer.font, "Resume Playing"     , 0,  -120, 200, 50, (down) -> { gameState = ST_RUNNING; }));
		pauseMenu.add(new MenuButton(renderer.font, "Reset"              , 0,   -50, 200, 50, (down) -> { gameState = ST_RESET;   }));
		pauseMenu.add(new MenuButton(renderer.font, "Return to Main Menu", 0,    50, 200, 50, (down) -> { gameState = ST_MENU;    }));
		pauseMenu.add(new MenuButton(renderer.font, "Quit Game"          , 0,   120, 200, 50, (down) -> { exit();                 }));

		applyFooterText();

		gameState = ST_MENU;
	}

	/**
	 * ONLY USE THIS AFTER setup() IS CALLED - DO NOT REFERENCE IN STATIC INITIALIZERS
	 * @return the current LD31 context (a singleton)
	 */
	public static LD31 getContext() {
		return context;
	}

	@Override
	public boolean contains(int x, int y) {
		return (x >= 0 && y >= 0 && x < lastWidth && y < lastHeight);
	}

	/**
	 * Used to avoid runtime out-of-bounds exceptions when the screen size changes in the middle of a tick
	 * Use these instead of width and height whenever you're doing something that depends on the screen size
	 */
	public int lastWidth, lastHeight;

	@Override
	public void draw() {
		if (width != lastWidth || height != lastHeight)
			resize(); //allows for free window resizing without affecting gameplay

		switch (gameState) {
			case ST_RESET_HARD: reset(); resize(); break;
			case ST_RESET:      reset();           break;
			case ST_RUNNING:    drawRunning();     break;
			case ST_WIN:        drawWin();         break;
			case ST_DIE:        drawDie();         break;
			case ST_PAUSE:      drawPause();       break;
			case ST_MENU:       drawMenu();        break;
			case ST_SETTINGS:   drawSettings();    break;
			case ST_CAMPAIGN:   drawCampaign();    break;
		}
		buildVersion.render();
	}

	private void resize() {
		loadPixels(); //must be done whenever the size of pixels changes
		Arrays.fill(pixels, 0);
		lastWidth = width;
		lastHeight = height;
		renderer.cropTextures(lastWidth, lastHeight);
	}

	/** Reloads the current level completely */
	private void reset() {
		level = new Level("res/test-level.txt"); //level verifies itself so we don't do that here anymore
		fadePhase = -(255 + 100);
		gameState = ST_RUNNING;
	}

	/** Updates the footer HUD text to reflect control bindings */
	//TODO: update this to work with new InputHandler class
	private void applyFooterText() {
//		String footerText = String.format(MSG_FOOTER,
//			input.keyMap(CTL_UP   , 0),
//			input.keyMap(CTL_LEFT , 0),
//			input.keyMap(CTL_DOWN , 0),
//			input.keyMap(CTL_RIGHT, 0),
//			input.keyMap(CTL_RESTART, 0));
//		String footerEndText = String.format(MSG_FOOTER_END,
//			input.keyMap(CTL_RESTART, 0));

//		footer.text = footerText;
//		win.footer  = footerEndText;
//		die.footer  = footerEndText;
	}

	/** tick logic for Free Play game mode */
	private void drawRunning() {
		//move player
		profiler.start(Profiler.PLAYER_MOVE);
		level.player.move(input.isPressed(InputHandler.CTL_UP   ),
		                  input.isPressed(InputHandler.CTL_DOWN ),
		                  input.isPressed(InputHandler.CTL_LEFT ),
		                  input.isPressed(InputHandler.CTL_RIGHT));
		//check win condition
		if (dist(level.player.x(), level.player.y(), level.objective.x(), level.objective.y()) < 5) {
			gameState = ST_WIN;
			return;
		}

		//calculate lighting
		profiler.swap(Profiler.PLAYER_MOVE, Profiler.LIGHTING);
		renderer.calculateLighting(pixels, level);

		//enemy pathing (this must be done before we apply textures over the lighting)
		profiler.swap(Profiler.LIGHTING, Profiler.ENEMY_PATH);
		for (Enemy e : level.enemies) {
			e.moveAuto();

			//losing condition
			if (abs(e.x() - level.player.x()) < Entity.SIZE && abs(e.y() - level.player.y()) < Entity.SIZE) {
				gameState = ST_DIE;
				return;
			}
		}

		//paint the image with the proper textures
		profiler.swap(Profiler.ENEMY_PATH, Profiler.TEXTURE);
		renderer.applyTexture(pixels);

		//draw all entities
		profiler.swap(Profiler.TEXTURE, Profiler.ENTITY_DRAW);
		renderer.renderEntities(level);

		//draw expanding and contracting circle around objective (uses integer triangle wave algorithm as distance)
		Trace.circle(level.objective.screenX(), level.objective.screenY(), PApplet.abs(frameCount % 50 - 25) + 50, (x, y) -> {
			if (contains(x, y) && level.inBounds(x, y))
				pixels[y*width + x] = Entity.COLOR_OBJECTIVE;
			return true;
		});

		//update pixels/wrap things up
		profiler.swap(Profiler.ENTITY_DRAW, Profiler.PIXEL_UPDATE);
		updatePixels();
		profiler.end(Profiler.PIXEL_UPDATE);

		//fade in and draw circle closing in on player at beginning of level
		if (fadePhase < 0) {
			fill(0, -(fadePhase += 4));
			rect(0, 0, width, height);
			//equivalent to the functionality of level.player.draw(Entity.COLOR_PLAYER)
			Trace.rectangle(level.player.screenX() - Entity.SIZE, level.player.screenY() - Entity.SIZE, Entity.SIZE*2 + 1, Entity.SIZE*2 + 1, (x, y) -> {
				set(x, y, Entity.COLOR_PLAYER);
				return true;
			});
			//draw a circle closing in on the player
			Trace.circle(level.player.screenX(), level.player.screenY(), max(0, -fadePhase - 255), (x, y) -> {
				if (level.inBounds(x, y))
					set(x, y, Entity.COLOR_PLAYER);
				return true;
			});
		}

		//Print out Text for directions
		footer.render();

		profiler.report(this);
	}

	private void drawWin() {
		win.render();
	}

	private void drawDie() {
		die.render();
	}

	private int menuHeight;
	private static final int MAX_MENUHEIGHT = 320;
	private static final int PAUSE_MENU_WIDTH = 240;

	private void drawPause() {
		updatePixels(); //doing this every frame allows us to draw the pause menu over a still of the game

		menuHeight += 20; //menu expands vertically from height of 0 to MAX_MENUHEIGHT
		if(menuHeight > MAX_MENUHEIGHT) menuHeight = MAX_MENUHEIGHT;

		//since the pause menu is of irregular dimensions, we're taking manual control
		pauseMenu.setBounds(PAUSE_MENU_WIDTH, menuHeight); //Manual override! Get to your battle stations!
		if (menuHeight > MAX_MENUHEIGHT - 20) {
			pauseMenu.render(); //only render the menu (with buttons) if the menu height is enough that the buttons won't be off the menu
		} else { //otherwise, draw a rectangle identical to the menu background (this is sort of a dumb hack)
			//TODO: move this functionality into Menu
			pushStyle();
			fill(100, 200); //default menu background color (translucent grey)
			rectMode(CENTER);
			rect(width/2, height/2, PAUSE_MENU_WIDTH, menuHeight, 10);
			popStyle();
		}
	}

	private void drawMenu() {
		image(renderer.textureRed, 0, 0); //TODO: placeholder background
		mainMenu.render();
	}

	private void drawSettings() {
		image(renderer.textureBlue, 0, 0); //TODO: placeholder background
		settingsMenu.render();
	}

	private void drawCampaign() {
		image(renderer.textureGreen, 0, 0); //TODO: placeholder background
		dummyCampaignMenu.render();
	}

	@Override
	public void mousePressed() {
		input.cancelBind();
	}

	@Override
	public void keyPressed() {
		input.handleKeyEvent(InputHandler.convertToKeyId(key, keyCode), true);
		key = 0; //Stop ESC from closing the program
	}

	@Override
	public void keyReleased() {
		input.handleKeyEvent(InputHandler.convertToKeyId(key, keyCode), false);
	}

	/** Global Entry Point */
	public static void main(String[] args) {
		PApplet.main(new String[] { LD31.class.getName() });
	}

	private static String buildVersion() {
		try {
			return ResourceBundle.getBundle("version").getString("build.versionString"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MissingResourceException e) {
			//If the version file doesn't exist, make a version string based on
			//branch name and short hash

			String branchName = "?"; //$NON-NLS-1$
			String branchHash = "?"; //$NON-NLS-1$
			try (BufferedReader gitHead = new BufferedReader(new FileReader(".git/HEAD"))) { //$NON-NLS-1$
				branchName = gitHead.readLine();
				branchName = branchName.substring(branchName.lastIndexOf('/') + 1);
				try (BufferedReader gitRefHead = new BufferedReader(new FileReader(".git/refs/heads/" + branchName))) { //$NON-NLS-1$
					branchHash = gitRefHead.readLine().substring(0, 7);
				} catch (IOException ew) {
					//Oops. Ignore
				}
			} catch (IOException ew) {
				//Oops. Ignore
			}

			return String.format("git %S.%s", branchName, branchHash); //$NON-NLS-1$
		}
	}
}
