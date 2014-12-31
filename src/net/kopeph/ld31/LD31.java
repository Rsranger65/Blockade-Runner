package net.kopeph.ld31;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.menu.EndScreen;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.util.Profiler;
import net.kopeph.ld31.util.ThreadPool;
import net.kopeph.ld31.util.Util;
import processing.core.PApplet;
import processing.core.PImage;

/** Everything inside here works like it does in processing */
public class LD31 extends PApplet {


	private static final long serialVersionUID = 1L;

	//THESE ARE ALL THE USER MESSAGES
	private static final String //Sorry, this project is not MSG free
	    MSG_TITLE      = "Blockade Runner",
	    MSG_RANDOM     = "Free Play",
	    MSG_STORY      = "Campaign",
	    MSG_SETTINGS   = "Settings",
	    MSG_QUIT       = "Exit",

		MSG_GAME_DIR   = "WASD: Move    Space: Restart    Objective: Capture the Pink Square    Beware Of: White Light",
		MSG_END_DIR    = "              Space: Restart",
	    MSG_WIN        = "YA DID IT!",
	    MSG_DIE        = "You ded Jim!",

	    MSG_SETTINGS_TITLE = "Settings Menu",
		MSG_SETTINGS_BODY = "No settings to change yet :(",
		MSG_SETTINGS_RET = "Back",

		MSG_PAUSE_TITLE = "Game Paused",
		MSG_PAUSE_RESUME = "Resume Playing",
		MSG_PAUSE_MAINMENU = "Return to Main Menu";

	private static final int
		ST_RESET_HARD = -2,  // Window size has changed
		ST_RESET      = -1,  // Level needs regenerated
		ST_RUNNING    =  0,  // Normal Condition
		ST_DIE        =  1,  // Displaying lose screen
		ST_WIN        =  2,  // Displaying win screen
		ST_PAUSE	  =  3,  // Displaying Pause Menu
		ST_MAINMENU   =  4,  // Displaying Main Menu
		ST_SETTINGS   =  5;  // Displaying Settings Menu

	private static LD31 context;

	private final Profiler profiler = new Profiler();
	private final ThreadPool texturingPool = new ThreadPool();

	private Level level;
	private EndScreen win, die;
	private Menu mainMenu, settings, pause;
	private volatile int gameState;
	private int fadePhase;

	//player movement
	private boolean wPressed = false,
					aPressed = false,
					sPressed = false,
					dPressed = false,
					upPressed = false,
					dnPressed = false,
					ltPressed = false,
					rtPressed = false;

	//textures
	private PImage textureRed    , rawTextureRed;
	private PImage textureGreen  , rawTextureGreen;
	private PImage textureBlue   , rawTextureBlue;
	private PImage textureCyan   , rawTextureCyan;
	private PImage textureMagenta, rawTextureMagenta;
	private PImage textureYellow , rawTextureYellow;
	private PImage textureGrey   , rawTextureGrey;
	private PImage textureWhite  , rawTextureWhite;
	private Font   fontWhite;

	/** Global Entry Point */
	public static void main(String[] args) {
		PApplet.main(new String[] { LD31.class.getName() });
	}

	@Override
	public void setup() {
		context = this;
		size(800, 600);
		frameRate(60);
		noStroke();
		frame.setResizable(true);
		frame.setTitle(MSG_TITLE);
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
		fontWhite    = new Font(this, "res/font-16-white.png"     ); //$NON-NLS-1$

		win = new EndScreen(this, fontWhite, MSG_WIN, MSG_END_DIR   , color(0, 120, 0));
		die = new EndScreen(this, fontWhite, MSG_DIE, MSG_END_DIR   , color(120, 0, 0));
		setupMenu();
		setupSettings();
		setupPause();

		gameState = ST_MAINMENU;
	}

	public static LD31 getContext() {
		return context;
	}

	@Override
	public void draw() {
		//detect and react to window resizing
		if (gameState == ST_RUNNING)
			if (level.LEVEL_WIDTH != width || level.LEVEL_HEIGHT != height)
				gameState = ST_RESET_HARD;

		switch (gameState) {
			case ST_RESET_HARD: resetHard();    break;
			case ST_RESET:      reset();        break;
			case ST_RUNNING:    drawRunning();  break;
			case ST_WIN:        drawWin();      break;
			case ST_DIE:        drawDie();      break;
			case ST_PAUSE:      drawPause();    break;
			case ST_MAINMENU:   drawMenu();     break;
			case ST_SETTINGS:   drawSettings(); break;
		}
	}

	private void resetHard() {
		reset();
		loadPixels(); //must be done whenever the size of pixels changes

		//crop textures
		textureRed     = Util.crop(this, rawTextureRed    , 0, 0, width, height);
		textureGreen   = Util.crop(this, rawTextureGreen  , 0, 0, width, height);
		textureBlue    = Util.crop(this, rawTextureBlue   , 0, 0, width, height);
		textureCyan    = Util.crop(this, rawTextureCyan   , 0, 0, width, height);
		textureMagenta = Util.crop(this, rawTextureMagenta, 0, 0, width, height);
		textureYellow  = Util.crop(this, rawTextureYellow , 0, 0, width, height);
		textureGrey    = Util.crop(this, rawTextureGrey   , 0, 0, width, height);
		textureWhite   = Util.crop(this, rawTextureWhite  , 0, 0, width, height);
	}

	private void reset() {
		Util.forceClose(level); //prevent resource leak from earlier ThreadPool (if any)
		level = new Level(this, width, height); //level verifies itself so we don't do that here anymore
		fadePhase = -(255 + 100);
		gameState = ST_RUNNING;
	}

	private void drawRunning() {
		//move player
		profiler.start(Profiler.PLAYER_MOVE);
		level.player.move(wPressed || upPressed,
				          sPressed || dnPressed,
				          aPressed || ltPressed,
				          dPressed || rtPressed);

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

		int taskSize = pixels.length/texturingPool.poolSize;
		for (int i = 0; i < texturingPool.poolSize; ++i) {
			final int j = i;
			texturingPool.post(() -> applyTexture(pixels, j*taskSize, (j+1)*taskSize));
		}

		texturingPool.forceSync();

		//draw all entities
		profiler.swap(Profiler.TEXTURE, Profiler.ENTITY_DRAW);
		level.objective.render();
		level.player.render();
		for (Enemy e : level.enemies)
			e.render();

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
			loadPixels();
			level.player.render();
			Trace.circle(level.player.x(), level.player.y(), max(0, -fadePhase - 255), (x, y) -> {
				if (level.inBounds(x, y))
					pixels[y*width + x] = Entity.COLOR_PLAYER;
				return true;
			});
			updatePixels();
		}

		//Print out Text for directions
		fontWhite.render(MSG_GAME_DIR, 8, height - 16);

		profiler.report(this);
	}

	private void applyTexture(final int[] image, int iBegin, int iEnd) {
		for (int i = iBegin; i < iEnd; ++i) {
			switch (pixels[i]) {
				case Level.FLOOR_NONE: break; //I don't know if this helps speed or not
				case Level.FLOOR_RED:     pixels[i] = textureRed.pixels[i];     break;
				case Level.FLOOR_GREEN:   pixels[i] = textureGreen.pixels[i];   break;
				case Level.FLOOR_BLUE:    pixels[i] = textureBlue.pixels[i];    break;
				case Level.FLOOR_CYAN:    pixels[i] = textureCyan.pixels[i];    break;
				case Level.FLOOR_MAGENTA: pixels[i] = textureMagenta.pixels[i]; break;
				case Level.FLOOR_YELLOW:  pixels[i] = textureYellow.pixels[i];  break;
				case Level.FLOOR_BLACK:   pixels[i] = textureGrey.pixels[i];    break;
				case Level.FLOOR_WHITE:   pixels[i] = textureWhite.pixels[i];   break;
			}
		}
	}

	private void drawWin() {
		win.render();
	}

	private void drawDie() {
		die.render();
	}
	private void setupPause() {
		pause = new Menu(this, fontWhite, MSG_PAUSE_TITLE);
		pause.add(new MenuButton(this, fontWhite, MSG_PAUSE_MAINMENU, height/2 - 60, 200, 40, () -> {
			gameState = ST_MAINMENU; //return to main menu
		}));
		pause.add(new MenuButton(this, fontWhite, MSG_PAUSE_RESUME, height/2, 200, 40, () -> {
			gameState = ST_RUNNING; //resume game
		}));
		pause.add(new MenuButton(this, fontWhite, MSG_QUIT, height/2 + 60, 200, 40, () -> {
			exit(); //exit game
		}));
	}

	private void drawPause() {
		updatePixels();
		pause.render();
	}

	private void setupMenu() {
		mainMenu = new Menu(this, fontWhite, MSG_TITLE);
		mainMenu.add(new MenuButton(this, fontWhite, MSG_RANDOM, 200, 400, 50, () -> {
			gameState = ST_RESET_HARD;
		}));
		mainMenu.add(new MenuButton(this, fontWhite, MSG_STORY, 260, 400, 50, () -> {
			//no-op for now
		}));
		mainMenu.add(new MenuButton(this, fontWhite, MSG_SETTINGS, 320, 400, 50, () -> {
			gameState = ST_SETTINGS; //open settings menu
		}));
		mainMenu.add(new MenuButton(this, fontWhite, MSG_QUIT, 420, 400, 50, () -> {
			exit(); //exit the game
		}));
	}

	private void drawMenu() {
		image(rawTextureRed, 0, 0); //placeholder background
		mainMenu.render();
	}

	private void setupSettings() {
		settings = new Menu(this, fontWhite, MSG_SETTINGS_TITLE);
		settings.add(new MenuButton(this, fontWhite, MSG_SETTINGS_RET, 200, 400, 50, () -> {
			gameState = ST_MAINMENU;
		}));
	}

	private void drawSettings() {
		image(rawTextureBlue, 0, 0); //placeholder background
		settings.render();
		fontWhite.renderCentered(MSG_SETTINGS_BODY, width/2, 450);
	}

	@Override
	public void keyPressed() {
		switch (key) {
			case 'w': case 'W': wPressed = true; break;
			case 's': case 'S': sPressed = true; break;
			case 'a': case 'A': aPressed = true; break;
			case 'd': case 'D': dPressed = true; break;
			case ' ':
				if (gameState == ST_RUNNING ||
					gameState == ST_WIN ||
					gameState == ST_DIE) {
					loop();
					gameState = ST_RESET;
				}
				break;
//			case 'p':
//				if (gameState == ST_RUNNING) {
//					gameState = ST_PAUSE;
//				} else if (gameState == ST_PAUSE) {
//					gameState = ST_RUNNING;
//				}
//				break;
			case ESC:
				//capture ESC key so it takes us to the menu instead of quitting our program
				key = 0;
				if (gameState == ST_MAINMENU)
					exit();
				else if (gameState == ST_PAUSE)
					gameState = ST_MAINMENU;
				else {
					loop();
					gameState = ST_PAUSE;
				}
				break;
			case CODED:
				switch (keyCode) {
					case UP:    upPressed = true; break;
					case DOWN:  dnPressed = true; break;
					case LEFT:  ltPressed = true; break;
					case RIGHT: rtPressed = true; break;
				}
		}
	}

	@Override
	public void keyReleased() {
		switch (key) {
			case 'w': case 'W': wPressed = false; break;
			case 's': case 'S': sPressed = false; break;
			case 'a': case 'A': aPressed = false; break;
			case 'd': case 'D': dPressed = false; break;
			case CODED:
				switch (keyCode) {
					case UP:    upPressed = false; break;
					case DOWN:  dnPressed = false; break;
					case LEFT:  ltPressed = false; break;
					case RIGHT: rtPressed = false; break;
				}
		}
	}
}
