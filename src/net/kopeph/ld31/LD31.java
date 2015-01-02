package net.kopeph.ld31;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.Font;
import net.kopeph.ld31.graphics.RenderContainer;
import net.kopeph.ld31.graphics.Trace;
import net.kopeph.ld31.graphics.context.ContextImage;
import net.kopeph.ld31.graphics.context.GraphicsContext;
import net.kopeph.ld31.menu.EndScreen;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.MenuButton;
import net.kopeph.ld31.util.Profiler;
import net.kopeph.ld31.util.ThreadPool;
import net.kopeph.ld31.util.Util;

public class LD31 extends RenderContainer {

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

	private final Profiler profiler = new Profiler();
	private final ThreadPool texturingPool = new ThreadPool();
	private GraphicsContext ctx;

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
	private ContextImage textureRed    , rawTextureRed;
	private ContextImage textureGreen  , rawTextureGreen;
	private ContextImage textureBlue   , rawTextureBlue;
	private ContextImage textureCyan   , rawTextureCyan;
	private ContextImage textureMagenta, rawTextureMagenta;
	private ContextImage textureYellow , rawTextureYellow;
	private ContextImage textureGrey   , rawTextureGrey;
	private ContextImage textureWhite  , rawTextureWhite;
	private Font   fontWhite;

	/** Global Entry Point */
	@SuppressWarnings("unused")
	public static void main(String[] args) { new LD31(); }

	private LD31() {
		GraphicsContext.init(GraphicsContext.PROCESSING, this::setup, this::render);
	}

	private void setup() {
		ctx = GraphicsContext.getInstance();

		ctx.size(800, 600);
		ctx.frameRate(60);
		ctx.noStroke();
		ctx.allowResize(true);
		ctx.setFrameTitle(MSG_TITLE);
		ctx.setKeyHandlers(this::keyPressed, this::keyReleased);
		//TODO: give the window a custom icon

		//load raw textures
		rawTextureRed     = ctx.loadImage("res/red-background.jpg"    ); //$NON-NLS-1$
		rawTextureGreen   = ctx.loadImage("res/green-background.jpg"  ); //$NON-NLS-1$
		rawTextureBlue    = ctx.loadImage("res/blue-background.jpg"   ); //$NON-NLS-1$
		rawTextureCyan    = ctx.loadImage("res/cyan-background.jpg"   ); //$NON-NLS-1$
		rawTextureMagenta = ctx.loadImage("res/magenta-background.jpg"); //$NON-NLS-1$
		rawTextureYellow  = ctx.loadImage("res/yellow-background.jpg" ); //$NON-NLS-1$
		rawTextureGrey    = ctx.loadImage("res/grey-background.jpg"   ); //$NON-NLS-1$
		rawTextureWhite   = ctx.loadImage("res/white-background.jpg"  ); //$NON-NLS-1$
		fontWhite         =  new Font("res/font-16-white.png"     ); //$NON-NLS-1$

		win = new EndScreen(fontWhite, MSG_WIN, MSG_END_DIR   , ctx.color(0, 120, 0));
		die = new EndScreen(fontWhite, MSG_DIE, MSG_END_DIR   , ctx.color(120, 0, 0));
		setupMenu();
		setupSettings();
		setupPause();

		gameState = ST_MAINMENU;
	}

	@Override
	public void render() {
		//detect and react to window resizing
		if (gameState == ST_RUNNING)
			if (level.LEVEL_WIDTH != ctx.width() || level.LEVEL_HEIGHT != ctx.height())
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
		ctx.loadPixels(); //must be done whenever the size of pixels changes

		//crop textures
		textureRed     = rawTextureRed    .crop(0, 0, ctx.width(), ctx.height());
		textureGreen   = rawTextureGreen  .crop(0, 0, ctx.width(), ctx.height());
		textureBlue    = rawTextureBlue   .crop(0, 0, ctx.width(), ctx.height());
		textureCyan    = rawTextureCyan   .crop(0, 0, ctx.width(), ctx.height());
		textureMagenta = rawTextureMagenta.crop(0, 0, ctx.width(), ctx.height());
		textureYellow  = rawTextureYellow .crop(0, 0, ctx.width(), ctx.height());
		textureGrey    = rawTextureGrey   .crop(0, 0, ctx.width(), ctx.height());
		textureWhite   = rawTextureWhite  .crop(0, 0, ctx.width(), ctx.height());
	}

	private void reset() {
		Util.forceClose(level); //prevent resource leak from earlier ThreadPool (if any)
		//level verifies itself so we don't do that here anymore
		level = new Level(ctx.width(), ctx.height());
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
		if (Util.dist(level.player.x(), level.player.y(), level.objective.x(), level.objective.y()) < 5) {
			gameState = ST_WIN;
			return;
		}

		//calculate lighting
		profiler.swap(Profiler.PLAYER_MOVE, Profiler.LIGHTING);
		level.calculateLighting(ctx.pixels());

		//enemy pathing (this must be done before we apply textures over the lighting)
		profiler.swap(Profiler.LIGHTING, Profiler.ENEMY_PATH);

		//check which enemies should be following the player
		if (ctx.pixels()[level.player.y()*level.LEVEL_WIDTH + level.player.x()] == Level.FLOOR_WHITE)
			for (Enemy e : level.enemies)
				e.checkPursuing(null); //pass in null to indicate the call isn't a referral from another enemy

		//allow enemies to move
		for (Enemy e : level.enemies) {
			e.moveAuto();

			//losing condition
			if (Math.abs(e.x() - level.player.x()) < Entity.SIZE && Math.abs(e.y() - level.player.y()) < Entity.SIZE) {
				gameState = ST_DIE;
				return;
			}
		}

		//paint the image with the proper textures
		profiler.swap(Profiler.ENEMY_PATH, Profiler.TEXTURE);

		int taskSize = ctx.pixels().length/texturingPool.poolSize;
		for (int i = 0; i < texturingPool.poolSize; ++i) {
			final int j = i;
			texturingPool.post(() -> applyTexture(ctx.pixels(), j*taskSize, (j+1)*taskSize));
		}

		texturingPool.forceSync();

		//draw all entities
		profiler.swap(Profiler.TEXTURE, Profiler.ENTITY_DRAW);
		level.objective.render();
		level.player.render();
		for (Enemy e : level.enemies)
			e.render();

		//draw expanding and contracting circle around objective (uses integer triangle wave algorithm as distance)
		Trace.circle(level.objective.x(), level.objective.y(), Math.abs(ctx.frameCount() % 50 - 25) + 50, (x, y) -> {
			if (level.inBounds(x, y))
				ctx.pixels()[y*ctx.width() + x] = Entity.COLOR_OBJECTIVE;
			return true;
		});

		//update pixels/wrap things up
		profiler.swap(Profiler.ENTITY_DRAW, Profiler.PIXEL_UPDATE);
		ctx.updatePixels();
		profiler.end(Profiler.PIXEL_UPDATE);

		//fade in and draw circle closing in on player at beginning of level
		if (fadePhase < 0) {
			ctx.fill(ctx.color(ctx.COLOR_BLACK, Util.clamp(-(fadePhase += 4), 0, 255)));
			ctx.rect(0, 0, ctx.width(), ctx.height());
			ctx.loadPixels();
			level.player.render();
			Trace.circle(level.player.x(), level.player.y(), Math.max(0, -fadePhase - 255), (x, y) -> {
				if (level.inBounds(x, y))
					ctx.pixels()[y*ctx.width() + x] = Entity.COLOR_PLAYER;
				return true;
			});
			ctx.updatePixels();
		}

		//Print out Text for directions
		fontWhite.render(MSG_GAME_DIR, 8, ctx.height() - 16);

		profiler.report(ctx);
	}

	private void applyTexture(final int[] image, int iBegin, int iEnd) {
		for (int i = iBegin; i < iEnd; ++i) {
			switch (ctx.pixels()[i]) {
				case Level.FLOOR_NONE: 												  break;
				case Level.FLOOR_RED:     ctx.pixels()[i] = textureRed.pixels()[i];     break;
				case Level.FLOOR_GREEN:   ctx.pixels()[i] = textureGreen.pixels()[i];   break;
				case Level.FLOOR_BLUE:    ctx.pixels()[i] = textureBlue.pixels()[i];    break;
				case Level.FLOOR_CYAN:    ctx.pixels()[i] = textureCyan.pixels()[i];    break;
				case Level.FLOOR_MAGENTA: ctx.pixels()[i] = textureMagenta.pixels()[i]; break;
				case Level.FLOOR_YELLOW:  ctx.pixels()[i] = textureYellow.pixels()[i];  break;
				case Level.FLOOR_BLACK:   ctx.pixels()[i] = textureGrey.pixels()[i];    break;
				case Level.FLOOR_WHITE:   ctx.pixels()[i] = textureWhite.pixels()[i];   break;
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
		pause = new Menu(fontWhite, MSG_PAUSE_TITLE);
		pause.add(new MenuButton(fontWhite, MSG_PAUSE_MAINMENU, ctx.height()/2 - 60, 200, 40, () -> {
			gameState = ST_MAINMENU; //return to main menu
		}));
		pause.add(new MenuButton(fontWhite, MSG_PAUSE_RESUME, ctx.height()/2, 200, 40, () -> {
			gameState = ST_RUNNING; //resume game
		}));
		pause.add(new MenuButton(fontWhite, MSG_QUIT, ctx.height()/2 + 60, 200, 40, () -> {
			ctx.exit(); //exit game
		}));
	}

	private void drawPause() {
		ctx.updatePixels();
		pause.render();
	}

	private void setupMenu() {
		mainMenu = new Menu(fontWhite, MSG_TITLE);
		mainMenu.add(new MenuButton(fontWhite, MSG_RANDOM, 200, 400, 50, () -> {
			gameState = ST_RESET_HARD;
		}));
		mainMenu.add(new MenuButton(fontWhite, MSG_STORY, 260, 400, 50, () -> {
			//no-op for now
		}));
		mainMenu.add(new MenuButton(fontWhite, MSG_SETTINGS, 320, 400, 50, () -> {
			gameState = ST_SETTINGS; //open settings menu
		}));
		mainMenu.add(new MenuButton(fontWhite, MSG_QUIT, 420, 400, 50, () -> {
			ctx.exit(); //exit the game
		}));
	}

	private void drawMenu() {
		ctx.image(rawTextureRed, 0, 0); //placeholder background
		mainMenu.render();
	}

	private void setupSettings() {
		settings = new Menu(fontWhite, MSG_SETTINGS_TITLE);
		settings.add(new MenuButton(fontWhite, MSG_SETTINGS_RET, 200, 400, 50, () -> {
			gameState = ST_MAINMENU;
		}));
	}

	private void drawSettings() {
		ctx.image(rawTextureBlue, 0, 0); //placeholder background
		settings.render();
		fontWhite.renderCentered(MSG_SETTINGS_BODY, ctx.width()/2, 450);
	}

	private void keyPressed(String keyId) {
		switch (keyId) {
			case " ":
				if (gameState == ST_RUNNING ||
					gameState == ST_WIN ||
					gameState == ST_DIE) {
					ctx.loop(true);
					gameState = ST_RESET;
				}
				break;
			case "ESC":
				if (gameState == ST_MAINMENU)
					ctx.exit();
				else if (gameState == ST_PAUSE)
					gameState = ST_MAINMENU;
				else {
					ctx.loop(true);
					gameState = ST_PAUSE;
				}
				break;
		}

		InputHandler.handleInput(keyId.toUpperCase(), true);
	}

	private void keyReleased(String keyId) {
		InputHandler.handleInput(keyId.toUpperCase(), false);
	}
}
