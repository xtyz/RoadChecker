package cz.pochoto.roadchecker.utils;

public class LowPassFilter {
 public static float ALPHA = 1f;
 
 	public static float[] filter( float[] input, float[] old ) {
	    if ( old == null ) return input;
	     
	    for ( int i=0; i<input.length; i++ ) {
	        old[i] = old[i] + ALPHA * (input[i] - old[i]);
	    }
	    return old;
	}
}
