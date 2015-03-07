package net.kopeph.ld31;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

/**
 * Filename strings are used as keys for clips.
 * @author alexg
 */
public class Audio {
	//Volume Categories
	public static final int  VOL_MUSIC  = 0,
	                         VOL_GAME   = 1;
	private static final int VOL_LENGTH = 2; //here for setting the proper length only

	private static final float MIN_DB = -60;

	private final Minim minim = new Minim(LD31.getContext());
	private Map<String, AudioPlayer> files = new HashMap<>();
	private List<List<String>> volumeClasses = new ArrayList<>(VOL_LENGTH);

	public Audio() {
		for (int i = 0; i < VOL_LENGTH; i++)
			volumeClasses.add(new ArrayList<>());
	}

	/**
	 * @param volumeClass possible values: VOL_*
	 */
	public void load(String filename, int volumeClass) {
		files.put(filename, minim.loadFile(filename));
		volumeClasses.get(volumeClass).add(filename);
	}

	public void play(String filename, boolean loop) {
		files.get(filename).play();
		if (loop)
			files.get(filename).loop();
	}
	public void pause(String filename) {
		files.get(filename).pause();
	}
	public void stop(String filename) {
		files.get(filename).pause();
		files.get(filename).rewind();
	}

	/**
	 * @param volumeClass possible values: VOL_*
	 */
	public void setVolume(int volumeClass, float volume) {
		for (String filename : volumeClasses.get(volumeClass))
			files.get(filename).setGain((volume - 1) * -MIN_DB);
	}

	public void shiftVolume(int volumeClass, float fromVolume, float toVolume, int time) {
		for (String filename : volumeClasses.get(volumeClass))
			files.get(filename).shiftGain((fromVolume - 1) * -MIN_DB, (toVolume - 1) * -MIN_DB, time);
	}
}
