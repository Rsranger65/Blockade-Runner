package net.kopeph.ld31;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.menu.EndScreen;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.menu.MenuWidget;
import net.kopeph.ld31.menu.TextBox;
import net.kopeph.ld31.util.Profiler;
import net.kopeph.ld31.util.ThreadPool;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PImage;

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

	private static LD31 context; //for static access so we don't have to pass this reference around so much
	private static TextBox buildVersion, footer;

	private final Profiler profiler = new Profiler();
	private final ThreadPool texturingPool = new ThreadPool();

	private Level level;
	private EndScreen win, die;
	private Menu mainMenu, settingsMenu, pauseMenu, dummyCampaignMenu;
	private volatile int gameState;
	private int fadePhase;

	public boolean interacting = false; //mouse interaction (used by MenuButton)

	private PImage textureRed    , rawTextureRed;
	private PImage textureGreen  , rawTextureGreen;
	private PImage textureBlue   , rawTextureBlue;
	private PImage textureCyan   , rawTextureCyan;
	private PImage textureMagenta, rawTextureMagenta;
	private PImage textureYellow , rawTextureYellow;
	private PImage textureGrey   , rawTextureGrey;
	private PImage textureWhite  , rawTextureWhite;
	private Font fontWhite;

	@Override
	public void setup() {
		context = this;

		size(800, 600);
		frameRate(60);
		noStroke();
		frame.setResizable(true);
		frame.setTitle("Blockade Runner");
		//TODO: give the window a custom icon

		//load raw textures
		rawTextureRed     = loadImage("res/red-background.jpg"    ); //$NON-NLS-1$
		rawTextureGreen   = loadImage("res/green-background.jpg"  ); //$NON-NLS-1$
		rawTextureBlue    = loadImage("res/blue-background.jpg"   ); //$NON-NLS-1$
		rawTextureCyan    = loadImage("res/cyan-background.jpg"   ); //$NON-NLS-1$
		rawTextureMagenta = loadImage("res/magenta-background.jpg"); //$NON-NLS-1$
		rawTextureYellow  = loadImage("res/yellow-background.jpg" ); //$NON-NLS-1$
		rawTextureGrey    = loadImage("res/grey-background.jpg"   ); //$NON-NLS-1$
		rawTextureWhite   = loadImage("res/white-background.jpg"  ); //$NON-NLS-1$

		fontWhite = new Font("res/font-16-white.png"); //$NON-NLS-1$

		buildVersion = new TextBox(fontWhite, 0, 4, width, 8, buildVersion());
		buildVersion.xAnchor = MenuWidget.ANCHOR_RIGHT;
		buildVersion.xPos    = width - buildVersion.text.length() * 8 - 4;

		footer = new TextBox(fontWhite, 4, height - 12, width, 8, MSG_FOOTER);
		footer.yAnchor = MenuWidget.ANCHOR_BOTTOM;

		//setup end screens
		win = new EndScreen(fontWhite, MSG_WIN, MSG_FOOTER_END, color(0, 120, 0));
		die = new EndScreen(fontWhite, MSG_DIE, MSG_FOOTER_END, color(120, 0, 0));

		//setup main menu
		mainMenu = new Menu();
		mainMenu.add(new TextBox(fontWhite,  "Blockade Runner", 0, -175));
		//$LAMBDA:Interaction
		mainMenu.add(new MenuButton(fontWhite, "Free Play"    , 0, -100, 400, 50, () -> { gameState = ST_RESET_HARD; }));
		//$LAMBDA:Interaction
		mainMenu.add(new MenuButton(fontWhite, "Campaign Mode", 0, - 40, 400, 50, () -> { gameState = ST_CAMPAIGN;   }));
		//$LAMBDA:Interaction
		mainMenu.add(new MenuButton(fontWhite, "Settings"     , 0, + 20, 400, 50, () -> { gameState = ST_SETTINGS;   }));
		//$LAMBDA:Interaction
		mainMenu.add(new MenuButton(fontWhite, "Exit"         , 0, +120, 400, 50, () -> { exit();                    }));

		//setup settings menu
		settingsMenu = new Menu();
		settingsMenu.add(new TextBox(fontWhite, "Settings Menu"               , 0, -175));
		//$LAMBDA:Interaction
		settingsMenu.add(new MenuButton(fontWhite, "Back", 0, -100, 400, 50, () -> { gameState = ST_MENU; }));
		settingsMenu.add(new TextBox(fontWhite, "No settings to change yet :(", 0,  150));
		//TODO: add options for key bindings

		//setup dummy campaign menu
		dummyCampaignMenu = new Menu();
		dummyCampaignMenu.add(new TextBox(fontWhite, "Campaign Mode", 0, -175));
		//$LAMBDA:Interaction
		dummyCampaignMenu.add(new MenuButton(fontWhite, "Back", 0, -100, 400, 50, () -> { gameState = ST_MENU; }));
		dummyCampaignMenu.add(new TextBox(fontWhite, "This game mode hasn't been implemented yet :(", 0,  150));

		//setup pause menu
		pauseMenu = new Menu(); //Dynamic size. see drawPause();
		pauseMenu.add(new TextBox(fontWhite, "Game Paused", 0, -200));
		//$LAMBDA:Interaction
		pauseMenu.add(new MenuButton(fontWhite, "Resume Playing"     , 0,  -120, 200, 50, () -> { gameState = ST_RUNNING; }));
		//$LAMBDA:Interaction
		pauseMenu.add(new MenuButton(fontWhite, "Reset"              , 0,   -50, 200, 50, () -> { gameState = ST_RESET;   }));
		//$LAMBDA:Interaction
		pauseMenu.add(new MenuButton(fontWhite, "Return to Main Menu", 0,    50, 200, 50, () -> { gameState = ST_MENU;    }));
		//$LAMBDA:Interaction
		pauseMenu.add(new MenuButton(fontWhite, "Quit Game"          , 0,   120, 200, 50, () -> { exit();                 }));

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

		//crop textures
		textureRed     = Util.crop(rawTextureRed    , 0, 0, width, height);
		textureGreen   = Util.crop(rawTextureGreen  , 0, 0, width, height);
		textureBlue    = Util.crop(rawTextureBlue   , 0, 0, width, height);
		textureCyan    = Util.crop(rawTextureCyan   , 0, 0, width, height);
		textureMagenta = Util.crop(rawTextureMagenta, 0, 0, width, height);
		textureYellow  = Util.crop(rawTextureYellow , 0, 0, width, height);
		textureGrey    = Util.crop(rawTextureGrey   , 0, 0, width, height);
		textureWhite   = Util.crop(rawTextureWhite  , 0, 0, width, height);
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
		level.player.move(InputHandler.isPressed(InputHandler.UP   ),
		                  InputHandler.isPressed(InputHandler.DOWN ),
		                  InputHandler.isPressed(InputHandler.LEFT ),
		                  InputHandler.isPressed(InputHandler.RIGHT));
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

		float taskSize = pixels.length/texturingPool.poolSize;
		for (int i = 0; i < texturingPool.poolSize; ++i) {
			final int j = i;
			//$LAMBDA:java.lang.Runnable
			texturingPool.post(() -> { applyTexture(pixels, PApplet.round(j*taskSize), PApplet.round((j+1)*taskSize)); });
		}

		texturingPool.forceSync();

		//draw all entities
		profiler.swap(Profiler.TEXTURE, Profiler.ENTITY_DRAW);
		level.objective.draw(Entity.COLOR_OBJECTIVE);
		level.player.draw(Entity.COLOR_PLAYER);
		for (Enemy e : level.enemies)
			e.draw();

		//draw expanding and contracting circle around objective (uses integer triangle wave algorithm as distance)
		//$LAMBDA:PointPredicate
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
			//$LAMBDA:PointPredicate
			Trace.rectangle(level.player.x() - Entity.SIZE, level.player.y() - Entity.SIZE, Entity.SIZE*2 + 1, Entity.SIZE*2 + 1, (x, y) -> {
				set(x, y, Entity.COLOR_PLAYER);
				return true;
			});
			//draw a circle closing in on the player
			//$LAMBDA:PointPredicate
			Trace.circle(level.player.x(), level.player.y(), max(0, -fadePhase - 255), (x, y) -> {
				set(x, y, Entity.COLOR_PLAYER);
				return true;
			});
		}

		//Print out Text for directions
		footer.render();

		profiler.report(this);
	}

	private void applyTexture(final int[] image, int iBegin, int iEnd) {
		for (int i = iBegin; i < iEnd; ++i) {
			switch (pixels[i]) {
				case Level.FLOOR_NONE: break; //I don't know if this helps speed or not
				case Level.FLOOR_RED:     image[i] = textureRed.pixels[i];     break;
				case Level.FLOOR_GREEN:   image[i] = textureGreen.pixels[i];   break;
				case Level.FLOOR_BLUE:    image[i] = textureBlue.pixels[i];    break;
				case Level.FLOOR_CYAN:    image[i] = textureCyan.pixels[i];    break;
				case Level.FLOOR_MAGENTA: image[i] = textureMagenta.pixels[i]; break;
				case Level.FLOOR_YELLOW:  image[i] = textureYellow.pixels[i];  break;
				case Level.FLOOR_BLACK:   image[i] = textureGrey.pixels[i];    break;
				case Level.FLOOR_WHITE:   image[i] = textureWhite.pixels[i];   break;
			}
		}
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
		if (menuHeight > 200) { //XXX: MAGIC NUMBERS EVERYWHERE!!!
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
		image(rawTextureRed, 0, 0); //TODO: placeholder background
		mainMenu.render();
	}

	private void drawSettings() {
		image(rawTextureBlue, 0, 0); //TODO: placeholder background
		settingsMenu.render();
	}

	private void drawCampaign() {
		image(rawTextureGreen, 0, 0); //TODO: placeholder background
		dummyCampaignMenu.render();
	}

	@Override
	public void mousePressed() {
		interacting = true; //using context.mousePressed would cause continuous interaction, but we only want once-per-click interaction
	}

	@Override
	public void keyPressed() {
		//TODO: move this into setup()
		//$LAMBDA:Interaction
		InputHandler.addBehavior(InputHandler.RESTART, () -> {
			if (gameState == ST_RUNNING ||
				gameState == ST_WIN ||
				gameState == ST_DIE) {
				loop();
				gameState = ST_RESET;
			}
		});
		//$LAMBDA:Interaction
		InputHandler.addBehavior(InputHandler.PAUSE, () -> {
			if (gameState == ST_RUNNING) {
				gameState = ST_PAUSE;
				menuHeight = 1;
			} else if (gameState == ST_PAUSE) {
				gameState = ST_RUNNING;
			}
		});
		//$LAMBDA:Interaction
		InputHandler.addBehavior(InputHandler.ESCAPE, () -> {
			//capture ESC key so it takes us to the menu instead of quitting our program
			key = 0;
			if (gameState == ST_MENU) {
				exit();
			} else {
				loop();
				gameState = ST_MENU;
			}
		});

		InputHandler.handleInput(key == CODED ? keyCode : key, true);
	}

	@Override
	public void keyReleased() {
		InputHandler.handleInput(key == CODED ? keyCode : key, false);
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
			//auto-increment and the branch name

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
