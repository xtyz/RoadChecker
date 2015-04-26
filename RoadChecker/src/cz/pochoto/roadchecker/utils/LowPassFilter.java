package cz.pochoto.roadchecker.utils;

/**
 * Class for filter data from sensors
 * @author Tomáš Pochobradský
 *
 */
public class LowPassFilter {
	/**
	 * ALPHA is used for smoothing results
	 */
	public static float ALPHA = 0.5f;

	/**
	 * Method for smoothing sensor data.  
	 * @param input New sesnor values
	 * @param old Values from last filtering
	 * @return
	 */
	public static float[] filter(float[] input, float[] old) {
		if (old == null)
			return input;

		for (int i = 0; i < input.length; i++) {
			old[i] = old[i] + ALPHA * (input[i] - old[i]);
		}
		return old;
	}
}
