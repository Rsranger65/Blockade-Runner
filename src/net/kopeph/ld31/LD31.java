package net.kopeph.ld31;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.menu.EndScreen;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
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

	private static final int
		ST_RESET_HARD = -2,  // Window size has changed
		ST_RESET      = -1,  // Level needs regenerated
		ST_RUNNING    =  0,  // Normal Condition
		ST_DIE        =  1,  // Displaying lose screen
		ST_WIN        =  2,  // Displaying win screen
		ST_PAUSE	  =  3,  // Displaying Pause Menu
		ST_MENU       =  4,  // Displaying Main Menu
		ST_SETTINGS   =  5;  // Displaying Settings Menu

	private static LD31 context; //for static access so we don't have to pass this reference around so much

	private final Profiler profiler = new Profiler();
	private final ThreadPool texturingPool = new ThreadPool();

	private Level level;
	private EndScreen win, die;
	private Menu mainMenu, settingsMenu, pauseMenu;
	private volatile int gameState;
	private int fadePhase;

	//player movement
	//TODO: move key handling to a separate class
	private boolean wpressed = false,
					apressed = false,
					spressed = false,
					dpressed = false;
	private boolean uppressed = false,
					downpressed = false,
					leftpressed = false,
					rightpressed = false;

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

		//setup end screens
		win = new EndScreen(fontWhite, MSG_WIN, MSG_FOOTER_END, color(0, 120, 0));
		die = new EndScreen(fontWhite, MSG_DIE, MSG_FOOTER_END, color(120, 0, 0));

		//setup main menu
		mainMenu = new Menu(width - 200, height - 200);
		mainMenu.add(new TextBox(fontWhite, "Blockade Runner", 125));
		mainMenu.add(new MenuButton(fontWhite, "Free Play"    , 200, 400, 50, () -> { gameState = ST_RESET_HARD; }));
		mainMenu.add(new MenuButton(fontWhite, "Campaign Mode", 260, 400, 50, () -> {                            }));
		mainMenu.add(new MenuButton(fontWhite, "Settings"     , 320, 400, 50, () -> { gameState = ST_SETTINGS;   }));
		mainMenu.add(new MenuButton(fontWhite, "Exit"         , 420, 400, 50, () -> { exit();                    }));

		//setup settings menu
		settingsMenu = new Menu(width - 200, height - 200);
		settingsMenu.add(new TextBox(fontWhite, "Settings Menu"               , 125));
		settingsMenu.add(new MenuButton(fontWhite, "Back", 200, 400, 50, () -> { gameState = ST_MENU; }));
		settingsMenu.add(new TextBox(fontWhite, "No settings to change yet :(", 450));
		//TODO: add options for key bindings

		//setup pause menu
		pauseMenu = new Menu(width - 200, height - 200);
		pauseMenu.add(new TextBox(fontWhite, "Game Paused", height/2 - 200));
		pauseMenu.add(new MenuButton(fontWhite, "Return to Main Menu", height/2 - 60, 200, 40, () -> { gameState = ST_MENU;    }));
		pauseMenu.add(new MenuButton(fontWhite, "Resume Playing"     , height/2     , 200, 40, () -> { gameState = ST_RUNNING; }));
		pauseMenu.add(new MenuButton(fontWhite, "Quit Game"          , height/2 + 60, 200, 40, () -> { exit();                 }));

		gameState = ST_MENU;
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
			case ST_MENU:       drawMenu();     break;
			case ST_SETTINGS:   drawSettings(); break;
		}

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
		level.player.move(wpressed || uppressed, spressed || downpressed, apressed || leftpressed, dpressed || rightpressed);

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
			loadPixels();
			level.player.draw(Entity.COLOR_PLAYER);
			Trace.circle(level.player.x(), level.player.y(), max(0, -fadePhase - 255), (x, y) -> {
				if (level.inBounds(x, y))
					pixels[y*width + x] = Entity.COLOR_PLAYER;
				return true;
			});
			updatePixels();
		}

		//Print out Text for directions
		fontWhite.render(MSG_FOOTER, 8, height - 16);

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

	private int menuHeight;
	private static final int MAX_MENUHEIGHT = 320;

	private void drawPause() {
		updatePixels();

		menuHeight += 20;
		if(menuHeight > MAX_MENUHEIGHT) menuHeight = MAX_MENUHEIGHT;

		pauseMenu.setBounds(240, menuHeight);
		if (menuHeight > 200) { //MAGIC NUMBERS EVERYWHERE!!!
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
		image(rawTextureRed, 0, 0); //placeholder background
		mainMenu.setBounds(width - 200, height - 200);
		mainMenu.render();
	}

	private void drawSettings() {
		image(rawTextureBlue, 0, 0); //placeholder background
		settingsMenu.setBounds(width - 200, height - 200);
		settingsMenu.render();
	}

	@Override
	public void mousePressed() {
		interacting = true; //using context.mousePressed would cause continuous interaction, but we only want once-per-click interaction
	}

	@Override
	public void keyPressed() {
		switch (key) {
			case 'w': case 'W': wpressed = true; break;
			case 's': case 'S': spressed = true; break;
			case 'a': case 'A': apressed = true; break;
			case 'd': case 'D': dpressed = true; break;
			case ' ':
				if (gameState == ST_RUNNING ||
					gameState == ST_WIN ||
					gameState == ST_DIE) {
					loop();
					gameState = ST_RESET;
				}
				break;
			case 'p':
				if (gameState == ST_RUNNING) {
					gameState = ST_PAUSE;
					menuHeight = 1;
				} else if (gameState == ST_PAUSE) {
					gameState = ST_RUNNING;
				}
				break;
			case ESC:
				//capture ESC key so it takes us to the menu instead of quitting our program
				key = 0;
				if (gameState == ST_MENU) {
					exit();
				} else {
					loop();
					gameState = ST_MENU;
				}
				break;
			case CODED:
				switch (keyCode) {
					case UP:    uppressed    = true; break;
					case DOWN:  downpressed  = true; break;
					case LEFT:  leftpressed  = true; break;
					case RIGHT: rightpressed = true; break;
				}
		}
	}

	@Override
	public void keyReleased() {
		switch (key) {
			case 'w': case 'W': wpressed = false; break;
			case 's': case 'S': spressed = false; break;
			case 'a': case 'A': apressed = false; break;
			case 'd': case 'D': dpressed = false; break;
			case CODED:
				switch (keyCode) {
					case UP:    uppressed    = false; break;
					case DOWN:  downpressed  = false; break;
					case LEFT:  leftpressed  = false; break;
					case RIGHT: rightpressed = false; break;
				}
		}
	}

	/** Global Entry Point */
	public static void main(String[] args) {
		PApplet.main(new String[] { LD31.class.getName() });
	}
}
