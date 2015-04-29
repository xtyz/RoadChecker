package cz.pochoto.roadchecker.utils;

import java.util.ArrayList;
import java.util.List;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.models.Displacement;

/**
 * Class accesses calculations for sensor operations
 * @author Tomáš Pochobradský
 *
 */
public class SensorUtils {
	private Mean mean1;
	private Mean mean2;
	private Mean mean3;
	private Mean mean4;
	private StandardDeviation st1;
	private StandardDeviation st2;
	private StandardDeviation st3;
	private StandardDeviation st4;
	
	private double finalMean = 0;
	private double finalDeviation = 0;
	private double lastMaxDisplacement = 0;
	private byte timer = 1;
	private float timeLimit = 1000l;
	private boolean displacementFound = false;
	
	private List<Double> displacements = new ArrayList<Double>();
	private List<Displacement> cacheDisplacements = new ArrayList<Displacement>();	
	/**
	 * Default timeline for mean counting
	 */
	private final static int TIMELINE = 10000;
	/**
	 * Limit for recognising displacements
	 */
	private final static int DISPLACEMENT_LIMIT = 1;
	/**
	 * Distance toleration for pothole - all potholes on 10 meters on the road is recognised as one
	 */
	private final static int DISTANCE_TOLERATION = 10;
	/**
	 * Default time limit for recognising potholes - 1s
	 */
	private final static float DEFAULT_TIME_LIMIT = 1000l;
		
	double currentAcc;

	public SensorUtils() {
		mean1 = new Mean();
		mean2 = new Mean();
		mean3 = new Mean();
		mean4 = new Mean();
		st1 = new StandardDeviation();
		st2 = new StandardDeviation();
		st3 = new StandardDeviation();
		st4 = new StandardDeviation();
	}

	/**
	 * Compute new values on calsulations with new double value of sensors
	 * @param value
	 */
	public void compute(double value) {
		// compute timer and reset neccessery
		byte timer = getTimer();
		resetMeans(timer, this.timer);
		this.timer = timer;

		currentAcc = value;
		// add new
		mean1.addValue(currentAcc);
		mean2.addValue(currentAcc);
		mean3.addValue(currentAcc);
		mean4.addValue(currentAcc);
		st1.addValue(currentAcc, mean1.getResult());
		st2.addValue(currentAcc, mean2.getResult());
		st3.addValue(currentAcc, mean3.getResult());
		st4.addValue(currentAcc, mean4.getResult());

		finalMean = computeAverage(timer, mean1, mean2, mean3, mean4);
		finalDeviation = computeAverage(timer, st1, st2, st3, st4);

		resoleveDisplacements(currentAcc, finalMean);		
	}
	
	/**
	 * Returns final mean from counitng
	 * @return
	 */
	public double getFinalMean() {
		return finalMean;
	}

	/**
	 * Returns final deviation from counitng
	 * @return
	 */
	public double getFinalDeviation() {
		return finalDeviation;
	}

	/**
	 * Compute four-component calculation based on timer
	 * @param timer
	 * @param ac1
	 * @param ac2
	 * @param ac3
	 * @param ac4
	 * @return
	 */
	private double computeAverage(byte timer, AbstractCounter ac1, AbstractCounter ac2, AbstractCounter ac3, AbstractCounter ac4){
		double result = 0;
		switch (timer) {
		case 1:
			result = (ac2.getResult() + ac3.getResult() + ac4.getResult()) / 3;
			break;
		case 2:
			result = (ac3.getResult() + ac4.getResult() + ac1.getResult()) / 3;
			break;
		case 3:
			result = (ac4.getResult() + ac1.getResult() + ac2.getResult()) / 3;
			break;
		case 4:
			result = (ac1.getResult() + ac2.getResult() + ac3.getResult()) / 3;
			break;
		}	
		return result;
	}

	/**
	 * Method resolving displacements based on current acceneration data, actual mean and  DISPLACEMENT_LIMIT
	 * @param acc
	 * @param mean
	 */
	private void resoleveDisplacements(double acc, double mean) {
		if(!displacements.isEmpty()){
			if(displacements.size() > 500){
				displacements.clear();
			}
		}
		
		if(acc > mean + DISPLACEMENT_LIMIT){
			if(!displacementFound){
				displacementFound = true;
			}
			if(acc > lastMaxDisplacement){
				lastMaxDisplacement = acc;
			}
			displacements.add(acc);
		}else{
			if(displacementFound){
				displacementFound = false;				
				Displacement d = new Displacement(System.currentTimeMillis(), lastMaxDisplacement);
				if(MainActivity.location != null){
					d.setLoc(MainActivity.location);
				}
				cacheDisplacements.add(d);
				System.out.println(d.getValue());
				lastMaxDisplacement = 0;
			}
			displacements.add(0d);
		}

	}
	
	/**
	 * Method returns max displacement found above limit
	 * @return
	 */
	public Displacement getMaxDisplacement(){		
		
		if(!cacheDisplacements.isEmpty()){
			int size = cacheDisplacements.size();
			long timeDif = System.currentTimeMillis() - cacheDisplacements.get(size - 1).getTime();				
			
			System.out.println(timeDif / 1000l + " s");
			//pokud uplynul urcity cas od posledni vychylky vypocitat max a vratit vysledek
			
			if(MainActivity.location != null && MainActivity.location.getSpeed() > 0){
				float speed = MainActivity.location.getSpeed() / 3.6f;
				timeLimit = DISTANCE_TOLERATION / speed;				
			}else{
				timeLimit = DEFAULT_TIME_LIMIT;
			}
			if(timeDif < timeLimit){
				return null;
			}	
			try{
				Displacement max = cacheDisplacements.get(0);
				for(Displacement d:cacheDisplacements){
					if(d.getValue() > max.getValue()){
						max = d;
					}
				}
				System.out.println("Nejvetší: "+max.getValue());
				return max;
			}finally{
				cacheDisplacements.clear();
			}
		}		
		
		return null;
	}

	/**
	 * Method returns all displacements found above limit
	 * @return
	 */
	public double getDisplacement() {
		if(displacements.isEmpty()){
			return 0;
		}
		return displacements.get(displacements.size()-1);
	}	
	
	/**
	 * Method devides actual time on four sections
	 * @return
	 */
	private byte getTimer() {
		long currentTime = System.currentTimeMillis() % TIMELINE;
		byte timer = 1;

		if (currentTime > 0) {
			timer = 1;
		}
		if (currentTime > TIMELINE / 4) {
			timer = 2;
		}
		if (currentTime > TIMELINE / 2) {
			timer = 3;
		}
		if (currentTime > TIMELINE / 4 * 3) {
			timer = 4;
		}

		return timer;
	}

	/**
	 * Method reset computings using timer
	 * @param timer
	 * @param oldTimer
	 */
	private void resetMeans(byte timer, byte oldTimer) {
		if (timer != oldTimer) {
			if (timer == 2 && oldTimer == 1) {
				mean2.reset();
				st2.reset();
			}
			if (timer == 3 && oldTimer == 2) {
				mean3.reset();
				st3.reset();
			}
			if (timer == 4 && oldTimer == 3) {
				mean4.reset();
				st4.reset();
			}
			if (timer == 1 && oldTimer == 4) {
				mean1.reset();
				st2.reset();
			}
		}
	}
}
