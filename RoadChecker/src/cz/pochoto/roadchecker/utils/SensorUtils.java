package cz.pochoto.roadchecker.utils;

public class SensorUtils {
	private Mean mean1;
	private Mean mean2;
	private Mean mean3;
	private Mean mean4;

	private byte timer = 1;
	private final int timeline = 20000; 

	public SensorUtils() {
		mean1 = new Mean();
		mean2 = new Mean();
		mean3 = new Mean();
		mean4 = new Mean();
	}

	public double computeAverage(float u, float v) {
		double result = 0;

		// compute timer and reset neccessery
		byte timer = getTimer();
		resetMeans(timer, this.timer);
		this.timer = timer;

		double currentAcc = Math.sqrt(u * u + v * v);
		// add new
		mean1.addValue(currentAcc);
		mean2.addValue(currentAcc);
		mean3.addValue(currentAcc);
		mean4.addValue(currentAcc);

		// compute average
		switch (timer) {
		case 1:
			result = (mean2.getMean() + mean3.getMean() + mean4.getMean()) / 3;
			break;
		case 2:
			result = (mean3.getMean() + mean4.getMean() + mean1.getMean()) / 3;
			break;
		case 3:
			result = (mean4.getMean() + mean1.getMean() + mean2.getMean()) / 3;
			break;
		case 4:
			result = (mean1.getMean() + mean2.getMean() + mean3.getMean()) / 3;
			break;
		}

		System.out.println(timer+" - "+result);

		return result;
	}

	public double findDisplacements() {
		return 0;
	}

	private byte getTimer() {
		long currentTime = System.currentTimeMillis() % timeline;
		System.out.println(currentTime);
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
				System.out.println("Reset mean2");
			}
			if (timer == 3 && oldTimer == 2) {
				mean3.reset();
				System.out.println("Reset mean3");
			}
			if (timer == 4 && oldTimer == 3) {
				mean4.reset();
				System.out.println("Reset mean4");
			}
			if (timer == 1 && oldTimer == 4) {
				mean1.reset();
				System.out.println("Reset mean1");
			}
		}
	}
}
