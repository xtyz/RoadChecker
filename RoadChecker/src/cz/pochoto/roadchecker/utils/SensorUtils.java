package cz.pochoto.roadchecker.utils;

public class SensorUtils {

	double count1 = 0;
	double value1 = 0;
	double max = 0;

	double count2 = 0;
	double value2 = 0;

	public double computeAverage(float u, float v) {
		double result = 0;
		try {
			count1 = count1 + 1;
			value1 = value1 + (Math.sqrt(v * v + u * u));
			result = value1 / count1;
		} catch (Exception e) {
			e.printStackTrace();
			count1 = 0;
			value1 = 0;
		}
		System.out.println(count1 + " - " + result);
		if(result > max){
			max = result;
		}
		return result;
	}

	public double findDisplacements() {
		return max;
	}
}
