package net.kopeph.ld31;

import java.util.Arrays;

import net.kopeph.ld31.entity.Enemy;
import net.kopeph.ld31.entity.Entity;
import net.kopeph.ld31.graphics.HUD;
import net.kopeph.ld31.graphics.Renderer;
import net.kopeph.ld31.menu.EndScreen;
import net.kopeph.ld31.menu.Menu;
import net.kopeph.ld31.menu.impl.*;
import net.kopeph.ld31.util.Profiler;
import processing.core.PApplet;

/** Everything inside here works like it does in processing */
public class LD31 extends PApplet {
	private static final long serialVersionUID = 1L;

	private static final String BG_MUSIC = "res/sound/music.mp3"; //file path
	public static final String TEST_LEVEL = "res/test-level.txt"; //file path

	public static final int // Game state enum
		ST_RESET_HARD = -2, // Window size has changed
		ST_RESET      = -1, // Level needs regenerated
		ST_RUNNING    =  0, // Normal Condition
		ST_DIE        =  1, // Displaying lose screen
		ST_WIN        =  2, // Displaying win screen
		ST_PAUSE      =  3, // Displaying Pause Menu
		ST_MENU       =  4, // Displaying Main Menu
		ST_SETTINGS   =  5, // Displaying Settings Menu
		ST_KEYBIND    =  6, // Displaying Key Bindings Menu
		ST_CAMPAIGN   =  7, // Displaying Dummy Campaign menu
		ST_FREE_PLAY  =  8; // Displaying Free Play Menu

	private static LD31 context; //for static access so we don't have to pass this reference around so much

	private Profiler profiler;
	private InputHandler input;
	private Audio audio;
	private Level level;
	private EndScreen win, die;
	private Menu mainMenu, settingsMenu, keyBindingsMenu, pauseMenu, dummyCampaignMenu, freePlayMenu;
	private int gameState;
	private int fadePhase;
	private String currentLevel;
	private int freePlayWidth = 800, freePlayHeight = 600; //these defaults don't matter, as they will be overwritten later

	public Renderer renderer;
	public boolean inGame; //used for determining where to return to from the settings menu

	@Override
	public void setup() {
		context = this;
		renderer = new Renderer();
		audio = new Audio();
		profiler = new Profiler();

		size(800, 600);
		frameRate(60);
		noStroke();
		frame.setResizable(true);
		frame.setTitle("Blockade Runner");
		//TODO: give the window a custom icon

		//Setup Audio
		audio.load(BG_MUSIC, Audio.VOL_MUSIC);
		audio.play(BG_MUSIC, true);

		//setup end screens
		win = new EndScreen(renderer.font, HUD.MSG_WIN, color(0, 120, 0));
		die = new EndScreen(renderer.font, HUD.MSG_DIE, color(120, 0, 0));

		//setup behaviors for keyboard controls
		input = new InputHandler();

		//setup game menus
		mainMenu = new MainMenu();
		freePlayMenu = new FreePlayMenu();
		dummyCampaignMenu = new CampaignMenu();
		pauseMenu = new PauseMenu();
		settingsMenu = new SettingsMenu(audio);
		setupKeyBindingsMenu();
		HUD.updateFooterText(input);

		gameState = ST_MENU;
	}

	/** helper function for setup(), which is also called by the "Revert to Defaults" button in the settings menu */
	public void setupKeyBindingsMenu() {
		keyBindingsMenu = new KeyBindingsMenu(input);
	}

	/**
	 * ONLY USE THIS AFTER setup() IS CALLED - DO NOT REFERENCE IN STATIC INITIALIZERS
	 * @return the current LD31 context (a singleton), context should always == this
	 */
	public static LD31 getContext() {
		return context;
	}

	/** For use in classes that need to know the game state, so we don't have to pass it around */
	public int gameState() {
		return gameState;
	}

	public void setGameState(int state) {
		gameState = state;
	}

	public void setLevelSize(int width, int height) {
		freePlayWidth = width;
		freePlayHeight = height;
	}

	public void setLevelPath(String path) {
		currentLevel = path;
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
			case ST_RESET_HARD: resize(); reset(); break; //order of resize/reset is important
			case ST_RESET:      reset();           break;
			case ST_RUNNING:    drawRunning();     break;
			case ST_WIN:        drawWin();         break;
			case ST_DIE:        drawDie();         break;
			case ST_PAUSE:      drawPause();       break;
			case ST_MENU:       drawMenu();        break;
			case ST_SETTINGS:   drawSettings();    break;
			case ST_KEYBIND:    drawKeyBindMenu(); break;
			case ST_CAMPAIGN:   drawCampaign();    break;
			case ST_FREE_PLAY:  drawFreePlay();    break;
		}

		HUD.render();
	}

	private void resize() {
		loadPixels(); //must be done whenever the size of pixels changes
		lastWidth = width;
		lastHeight = height;
		renderer.cropTextures(lastWidth, lastHeight);
	}

	/** Reloads the current level completely */
	private void reset() {
		if (currentLevel == null) level = new Level(freePlayWidth, freePlayHeight);
		else                      level = new Level(currentLevel);
		fadePhase = -(255 + 100);
		HUD.updateFooterText(input);
		gameState = ST_RUNNING;
		//clear out previously rendered data in case the player starts near the edge of the map
		Arrays.fill(pixels, 0);
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
			if (abs(e.x() - level.player.x()) < Entity.SIZE*2 && abs(e.y() - level.player.y()) < Entity.SIZE*2) {
				gameState = ST_DIE;
				return;
			}
		}

		//paint the image with the proper textures
		profiler.swap(Profiler.ENEMY_PATH, Profiler.TEXTURE);
		renderer.applyTexture(pixels);

		//update pixels/wrap things up
		profiler.swap(Profiler.TEXTURE, Profiler.PIXEL_UPDATE);
		updatePixels();

		//draw all entities
		profiler.swap(Profiler.PIXEL_UPDATE, Profiler.ENTITY_DRAW);
		renderer.renderEntities(level);
		profiler.end(Profiler.ENTITY_DRAW);

		//fade in and draw circle closing in on player at beginning of level
		if (fadePhase < 0) {
			fill(0, -(fadePhase += 4));
			rect(0, 0, width, height);
			level.player.renderAlternate(max(0, -fadePhase - 255));
		}

		profiler.report(this);
	}

	private void drawWin() {
		win.render();
	}

	private void drawDie() {
		die.render();
	}

	private void drawPause() {
		pauseMenu.render();
	}

	private void drawMenu() {
		image(renderer.textureRed, 0, 0);
		mainMenu.render();
	}

	private void drawSettings() {
		if (inGame)
			updatePixels();
		else
			image(renderer.textureBlue, 0, 0);
		settingsMenu.render();
	}

	private void drawKeyBindMenu() {
		if (inGame)
			updatePixels();
		else
			image(renderer.textureGreen, 0, 0);
		keyBindingsMenu.render();
	}

	private void drawCampaign() {
		image(renderer.textureMagenta, 0, 0);
		dummyCampaignMenu.render();
	}

	private void drawFreePlay() {
		image(renderer.textureCyan, 0, 0);
		freePlayMenu.render();
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
}
