package net.kopeph.ld31;

/**
 *
 * @author alexg
 */
public class AudioManager {
	public static final int
		VOL_MUSIC  = 0,
		VOL_GAME   = 1,
		VOL_LENGTH = 2;

	public AudioManager() {}

	public void load(String filename, boolean loop, int volumeClass) {}
	public void setPlaying(String filename, boolean playing) {}
	public void setVolume(int volumeClass, float volume) {}
}
