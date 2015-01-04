package net.kopeph.ld31;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;


/**
 * Filename strings are used as keys for clips.
 *
 * @author alexg
 */
public class AudioManager {
	public static final int //Volume Categories
		VOL_MUSIC  = 0,
		VOL_GAME   = 1,
		VOL_LENGTH = 2; //here for setting the proper length only

	private final Minim minim = new Minim(LD31.getContext());
	private Map<String, AudioPlayer> files = new HashMap<>();
	private List<List<String>> volumeClasses = new ArrayList<>(VOL_LENGTH);

	public AudioManager() {
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
			files.get(filename).setVolume(volume);
	}
}
