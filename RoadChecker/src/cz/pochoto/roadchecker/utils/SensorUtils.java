package cz.pochoto.roadchecker.utils;

import java.util.ArrayList;
import java.util.List;

public class SensorUtils {
	private Mean mean1;
	private Mean mean2;
	private Mean mean3;
	private Mean mean4;
	private StandardDeviation st1;
	private StandardDeviation st2;
	private StandardDeviation st3;
	private StandardDeviation st4;

	private byte timer = 1;
	private final int timeline = 20000;
	private List<Double> displacements = new ArrayList<Double>();
	
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

	public double compute(float u, float v) {
		double finalMean = 0;
		double finalDeviation = 0;

		// compute timer and reset neccessery
		byte timer = getTimer();
		resetMeans(timer, this.timer);
		this.timer = timer;

		currentAcc = Math.sqrt(u * u + v * v);
		// add new
		mean1.addValue(currentAcc);
		mean2.addValue(currentAcc);
		mean3.addValue(currentAcc);
		mean4.addValue(currentAcc);
		st1.addValue(currentAcc, mean1.getResult());
		st2.addValue(currentAcc, mean2.getResult());
		st3.addValue(currentAcc, mean3.getResult());
		st4.addValue(currentAcc, mean4.getResult());

		// compute average
		finalMean = computeAverage(timer, mean1, mean2, mean3, mean4);
		finalDeviation = computeAverage(timer, st1, st2, st3, st4);

//		System.out.println(timer+" - "+finalMean);
//		System.out.println(finalDeviation);

		resoleveDisplacements(currentAcc, finalMean, finalDeviation);
		
		return finalMean;
	}
	
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

	private void resoleveDisplacements(double acc, double mean, double finalDeviation) {
		if(acc + finalDeviation < mean || acc - finalDeviation > mean){
			displacements.add(acc);
		}
		
		if(!displacements.isEmpty()){			
//			System.out.println("Displacements - "+displacements.size());
			if(displacements.size() > 500){
				displacements.clear();
			}
		}
	}

	public double findDisplacements() {
		return currentAcc/4;
	}

	private byte getTimer() {
		long currentTime = System.currentTimeMillis() % timeline;
//		System.out.println(currentTime);
		byte timer = 1;

		if (currentTime > 0) {
			timer = 1;
		}
		if (currentTime > timeline / 4) {
			timer = 2;
		}
		if (currentTime > timeline / 2) {
			timer = 3;
		}
		if (currentTime > timeline / 4 * 3) {
			timer = 4;
		}

		return timer;
	}

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
