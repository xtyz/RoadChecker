package cz.pochoto.roadchecker.utils;

import java.util.ArrayList;
import java.util.List;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.models.Displacement;

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
	private final int timeline = 10000;
	private List<Double> displacements = new ArrayList<Double>();
	private List<Displacement> maxDisplacements = new ArrayList<Displacement>();
	
	private boolean displacementFound = false;
	private boolean foundNewMax = false;
	
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

		// compute average
		finalMean = computeAverage(timer, mean1, mean2, mean3, mean4);
		finalDeviation = computeAverage(timer, st1, st2, st3, st4);

		resoleveDisplacements(currentAcc, finalMean, finalDeviation);		
	}
	
	public double getFinalMean() {
		return finalMean;
	}

	public double getFinalDeviation() {
		return finalDeviation;
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
		if(!displacements.isEmpty()){
			if(displacements.size() > 500){
				displacements.clear();
			}
		}
		
		if(acc > mean + 1){
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
				foundNewMax = true;
				Displacement d = new Displacement(System.currentTimeMillis(), lastMaxDisplacement);
				if(MainActivity.location != null){
					d.setLoc(MainActivity.location);
				}
				maxDisplacements.add(d);
				System.out.println(d.getValue());
				lastMaxDisplacement = 0;
			}
			displacements.add(0d);
		}

	}
	
	public Displacement getMaxDisplacement(){
		if(foundNewMax){
			foundNewMax = false;
			int size = maxDisplacements.size();
			if(size > 1){
				long timeDif = maxDisplacements.get(size - 1).getTime() - maxDisplacements.get(size - 2).getTime();
				System.out.println(timeDif / 1000l + " s");
				if(timeDif < 1000){//TODO èas se musí øídít rychlostí, pravdepodobne bude nutné pøidat casch která bude analyzovat jen ty vyssi 
					return null;
				}
			}
			return maxDisplacements.get(size - 1);
		}
		return null;
	}

	public double getDisplacement() {
		if(displacements.isEmpty()){
			return 0;
		}
		return displacements.get(displacements.size()-1);
	}	
	
	private byte getTimer() {
		long currentTime = System.currentTimeMillis() % timeline;
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
