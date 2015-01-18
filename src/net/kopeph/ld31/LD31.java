package net.kopeph.ld31;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;

/** Everything inside here works like it does in processing */
public class LD31 extends PApplet {
	private static final long serialVersionUID = 1L;

	//THESE ARE ALL THE USER MESSAGES
	private static final String MSG_FOOTER =
	"WASD: Move    Space: Restart    Objective: Capture the Pink Square    Beware Of: White Light";
	private static final String MSG_FOOTER_END =
	"              Space: Restart";
	private static final String MSG_WIN = "YA DID IT!";
	private static final String MSG_DIE = "You ded Jim!"; //Sorry, this project is not MSG free


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

	//XXX: this is a lot of repeated values
	private static final String //control ID enum
		CTL_UP       = "game.up",    //$NON-NLS-1$
		CTL_LEFT     = "game.left",  //$NON-NLS-1$
		CTL_DOWN     = "game.down",  //$NON-NLS-1$
		CTL_RIGHT    = "game.right", //$NON-NLS-1$
		CTL_RESTART  = "game.reset", //$NON-NLS-1$
		CTL_PAUSE    = "game.pause", //$NON-NLS-1$
		CTL_ESCAPE   = "sys.esc"; 	 //$NON-NLS-1$

	private static final int CTL_SLOTS = 3; //how many columns to show the user
	private static final List<String> CTL_IDS = Arrays.asList(
			CTL_UP,
			CTL_LEFT,
			CTL_DOWN,
			CTL_RIGHT,
			CTL_RESTART,
			CTL_PAUSE,
			CTL_ESCAPE
			);
	private static final Map<String, String> CTL_NAMES = new HashMap<>();
	static {
		CTL_NAMES.put(CTL_UP     , "Up"   );
		CTL_NAMES.put(CTL_LEFT   , "Left" );
		CTL_NAMES.put(CTL_DOWN   , "Down" );
		CTL_NAMES.put(CTL_RIGHT  , "Right");
		CTL_NAMES.put(CTL_RESTART, "Reset");
		CTL_NAMES.put(CTL_PAUSE  , "Pause");
		CTL_NAMES.put(CTL_ESCAPE , "Menu");
	}

	private static LD31 context; //for static access so we don't have to pass this reference around so much
	private static TextBox buildVersion, footer;

	private final Profiler profiler = new Profiler();

	private Input input = new Input();
	private Level level;
	private EndScreen win, die;
	private Menu mainMenu, settingsMenu, pauseMenu, dummyCampaignMenu;
	private volatile int gameState;
	private int fadePhase;

	public boolean interacting = false; //mouse interaction (used by MenuButton)
	
	private Renderer renderer;

	@Override
	public void setup() {
		context = this;
		renderer = new Renderer();

		size(800, 600);
		frameRate(60);
		noStroke();
		frame.setResizable(true);
		frame.setTitle("Blockade Runner");
		//TODO: give the window a custom icon

		buildVersion = new TextBox(renderer.font, 0, 4, width, 8, buildVersion());
		buildVersion.xAnchor = MenuWidget.ANCHOR_RIGHT;
		buildVersion.xPos    = width - buildVersion.text.length() * 8 - 4;

		footer = new TextBox(renderer.font, 4, height - 12, width, 8, MSG_FOOTER);
		footer.yAnchor = MenuWidget.ANCHOR_BOTTOM;

		//setup end screens
		win = new EndScreen(renderer.font, MSG_WIN, MSG_FOOTER_END, color(0, 120, 0));
		die = new EndScreen(renderer.font, MSG_DIE, MSG_FOOTER_END, color(120, 0, 0));

		//setup main menu
		mainMenu = new Menu();
		mainMenu.add(new TextBox(renderer.font,  "Blockade Runner", 0, -175));
		mainMenu.add(new MenuButton(renderer.font, "Free Play"    , 0, -100, 400, 50, () -> { gameState = ST_RESET_HARD; }));
		mainMenu.add(new MenuButton(renderer.font, "Campaign Mode", 0, - 40, 400, 50, () -> { gameState = ST_CAMPAIGN;   }));
		mainMenu.add(new MenuButton(renderer.font, "Settings"     , 0, + 20, 400, 50, () -> { gameState = ST_SETTINGS;   }));
		mainMenu.add(new MenuButton(renderer.font, "Exit"         , 0, +120, 400, 50, () -> { exit();                    }));

		//setup settings menu
		settingsMenu = new Menu();
		settingsMenu.add(new TextBox(renderer.font, "Settings Menu"               , 0, -175));

		TextBox[][] widgets = new TextBox[CTL_NAMES.size() + 1][CTL_SLOTS + 1]; //[y][x]
		//seed the (unused) top left corner of the table
		widgets[0][0] = new TextBox(renderer.font, "",
									-30*widgets[0].length,
									-20*widgets.length + 10);

		//TODO: move relative calculation based layout to TextBox ctor
		//fill in the first row with slot numbers
		for (int col = 1; col < widgets[0].length; ++col) {
			widgets[0][col] = new TextBox(renderer.font, String.valueOf(col),
										  widgets[0][col - 1].xPos + 60,
										  widgets[0][0].yPos);
			settingsMenu.add(widgets[0][col]); //adding widgets to the menu as we go
		}

		//Fill each row with key title, then a set of buttons for each slot
		int row = 1;
		for (final String id : CTL_IDS) {
			widgets[row][0] = new TextBox(renderer.font, CTL_NAMES.get(id),
										  widgets[0][0].xPos,
										  widgets[row - 1][0].yPos + 30);
			settingsMenu.add(widgets[row][0]); //adding widgets to the menu as we go

			for (int col = 1; col < widgets[row].length; ++col) {
				final int r = row, c = col; //needed for the behavior to work
				//Don't bother with the displayed until draw time
				widgets[r][c] = new MenuButton(renderer.font, "",
											   widgets[0][c].xPos,
											   widgets[r][0].yPos,
											   50, 20,
				() -> {
					input.handleBind(id, c - 1, CTL_ESCAPE);
				});
				widgets[r][c].tag = id;
				settingsMenu.add(widgets[r][c]); //adding widgets to the menu as we go
			}
			row++;
		}

		settingsMenu.add(new MenuButton(renderer.font, "Back", 0, 120, 400, 50, () -> { gameState = ST_MENU; }));

		//setup dummy campaign menu
		dummyCampaignMenu = new Menu();
		dummyCampaignMenu.add(new TextBox(renderer.font, "Campaign Mode", 0, -175));
		dummyCampaignMenu.add(new MenuButton(renderer.font, "Back", 0, -100, 400, 50, () -> { gameState = ST_MENU; }));
		dummyCampaignMenu.add(new TextBox(renderer.font, "This game mode hasn't been implemented yet :(", 0,  150));

		//setup pause menu
		pauseMenu = new Menu(); //Dynamic size. see drawPause();
		pauseMenu.add(new TextBox(renderer.font, "Game Paused", 0, -200));
		pauseMenu.add(new MenuButton(renderer.font, "Resume Playing"     , 0,  -120, 200, 50, () -> { gameState = ST_RUNNING; }));
		pauseMenu.add(new MenuButton(renderer.font, "Reset"              , 0,   -50, 200, 50, () -> { gameState = ST_RESET;   }));
		pauseMenu.add(new MenuButton(renderer.font, "Return to Main Menu", 0,    50, 200, 50, () -> { gameState = ST_MENU;    }));
		pauseMenu.add(new MenuButton(renderer.font, "Quit Game"          , 0,   120, 200, 50, () -> { exit();                 }));

		//setup input interaction
		input.addAction(CTL_RESTART, () -> {
			if (gameState == ST_RUNNING ||
				gameState == ST_WIN ||
				gameState == ST_DIE) {
				loop();
				gameState = ST_RESET;
			}
		}, (int)'R', (int)' ', Input.K_ENTER);
		input.addAction(CTL_PAUSE, () -> {
			if (gameState == ST_RUNNING) {
				gameState = ST_PAUSE;
				menuHeight = 1;
			} else if (gameState == ST_PAUSE) {
				gameState = ST_RUNNING;
			}
		}, (int)'P', Input.K_TAB);
		input.addAction(CTL_ESCAPE, () -> {
			if (gameState == ST_MENU) {
				exit();
			} else if (gameState == ST_RUNNING) {
				gameState = ST_PAUSE;
				menuHeight = 1;
			}
			else {
				loop();
				gameState = ST_MENU;
			}
		}, Input.K_ESC);
		input.addMonitor(CTL_UP,	(int)'W', (int)'8', Input.K_UP);
		input.addMonitor(CTL_LEFT,	(int)'A', (int)'4', Input.K_LEFT);
		input.addMonitor(CTL_DOWN,	(int)'S', (int)'2', Input.K_DOWN);
		input.addMonitor(CTL_RIGHT, (int)'D', (int)'6', Input.K_RIGHT);

		gameState = ST_MENU;
	}

	public static LD31 getContext() {
		return context;
	}

	@Override
	public void draw() {
		//detect and react to window resizing
		//TODO: fix so that resizing while paused doesn't kick you out of the pause menu
		if (gameState == ST_RUNNING || gameState == ST_PAUSE)
			if (level.LEVEL_WIDTH != width || level.LEVEL_HEIGHT != height)
				gameState = ST_RESET_HARD;

		switch (gameState) {
			case ST_RESET_HARD: resetHard();    break;
			case ST_RESET:      reset();        break;
			case ST_RUNNING:    drawRunning();  break;
			case ST_WIN:        drawWin();      break;
			case ST_DIE:        drawDie();      break;
			case ST_PAUSE:      drawPause();    break;
			case ST_MENU:       drawMenu();     break;
			case ST_SETTINGS:   drawSettings(); break;
			case ST_CAMPAIGN:   drawCampaign(); break;
		}
		buildVersion.render();

		interacting = false; //mouse interactions should only last one frame
	}

	private void resetHard() {
		reset();
		loadPixels(); //must be done whenever the size of pixels changes
		renderer.cropTextures(width, height);
	}

	private void reset() {
		Util.forceClose(level); //prevent resource leak from earlier ThreadPool (if any)
		level = new Level(width, height); //level verifies itself so we don't do that here anymore
		fadePhase = -(255 + 100);
		gameState = ST_RUNNING;
	}

	private void drawRunning() {
		//move player
		profiler.start(Profiler.PLAYER_MOVE);
		level.player.move(input.getKey(CTL_UP   ),
		                  input.getKey(CTL_DOWN ),
		                  input.getKey(CTL_LEFT ),
		                  input.getKey(CTL_RIGHT));
		//check win condition
		if (dist(level.player.x(), level.player.y(), level.objective.x(), level.objective.y()) < 5) {
			gameState = ST_WIN;
			return;
		}

		//calculate lighting
		profiler.swap(Profiler.PLAYER_MOVE, Profiler.LIGHTING);
		level.calculateLighting(pixels);

		//enemy pathing (this must be done before we apply textures over the lighting)
		profiler.swap(Profiler.LIGHTING, Profiler.ENEMY_PATH);

		//check which enemies should be following the player
		if (pixels[level.player.y()*level.LEVEL_WIDTH + level.player.x()] == Level.FLOOR_WHITE)
			for (Enemy e : level.enemies)
				e.checkPursuing(null); //pass in null to indicate the call isn't a referral from another enemy

		//allow enemies to move
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
		level.objective.draw(Entity.COLOR_OBJECTIVE);
		level.player.draw(Entity.COLOR_PLAYER);
		for (Enemy e : level.enemies)
			e.draw();

		//draw expanding and contracting circle around objective (uses integer triangle wave algorithm as distance)
		Trace.circle(level.objective.x(), level.objective.y(), PApplet.abs(frameCount % 50 - 25) + 50, (x, y) -> {
			if (level.inBounds(x, y))
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
			Trace.rectangle(level.player.x() - Entity.SIZE, level.player.y() - Entity.SIZE, Entity.SIZE*2 + 1, Entity.SIZE*2 + 1, (x, y) -> {
				set(x, y, Entity.COLOR_PLAYER);
				return true;
			});
			//draw a circle closing in on the player
			Trace.circle(level.player.x(), level.player.y(), max(0, -fadePhase - 255), (x, y) -> {
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

	private void drawPause() {
		updatePixels();

		menuHeight += 20;
		if(menuHeight > MAX_MENUHEIGHT) menuHeight = MAX_MENUHEIGHT;

		pauseMenu.setBounds(240, menuHeight);
		if (menuHeight > 300) { //XXX: MAGIC NUMBERS EVERYWHERE!!!
			pauseMenu.render();
		} else {
			pushStyle();
			fill(100, 200); //THERE'S MAGIC IN THE AIR CAN YOU FEEL IT???
			rectMode(CENTER);
			rect(width/2, height/2, 240, menuHeight, 10);
			popStyle();
		}
	}

	private void drawMenu() {
		image(renderer.rawTextureRed, 0, 0); //TODO: placeholder background
		mainMenu.render();
	}

	private void drawSettings() {
		image(renderer.rawTextureBlue, 0, 0); //TODO: placeholder background
		syncKeyMaps();
		settingsMenu.render();
	}

	private void syncKeyMaps() {
		Map<String, List<String>> keyMap = input.keyMap();
		Map<String, Integer> indices = new HashMap<>();

		for (int i = 0; i < settingsMenu.getChildCount(); i++) {
			MenuWidget widget = settingsMenu.getChild(i);

			if (widget instanceof MenuButton && widget.tag != null) {
				TextBox textBoxWidget = (TextBox) widget;
				List<String> keyRow = keyMap.get(widget.tag);
				Integer index = indices.get(widget.tag);
				if (index == null)
					index = 0;

				if (keyRow != null && index < keyRow.size())
					textBoxWidget.text = keyRow.get(index++);
				else
					textBoxWidget.text = Input.getKeyTitle(Input.K_UNBOUND);
				indices.put(widget.tag, index);
			}
		}
	}

	private void drawCampaign() {
		image(renderer.rawTextureGreen, 0, 0); //TODO: placeholder background
		dummyCampaignMenu.render();
	}

	@Override
	public void mousePressed() {
		interacting = true; //using context.mousePressed would cause continuous interaction, but we only want once-per-click interaction
	}

	@Override
	public void keyPressed() {
		input.eventKey(key, keyCode, true);
		key = 0; //Stop ESC from closing the program
	}

	@Override
	public void keyReleased() {
		input.eventKey(key, keyCode, false);
	}

	/** Global Entry Point */
	public static void main(String[] args) {
		PApplet.main(new String[] { LD31.class.getName() });
	}

	private static String buildVersion() {
		try {
			return ResourceBundle.getBundle("version").getString("build.versionString");
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
