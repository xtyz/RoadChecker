package cz.pochoto.roadchecker.utils;

public class Mean {
	private double count = 0;
	private double value = 0;
	private double mean = 0;
	private double max = 0;
	
	
	public double getMean(){		
		return mean;
	}
	
	public double addValue(double value){
		this.value = this.value + value;
		count++;
		mean = this.value / count;
		if(mean > max){
			max = mean;
		}
		return mean;
	}
	
	public double getMax(){
		return max;
	}
	
	public void reset(){
		count = 0;
		value = 0;
		mean = 0;
		max = 0;
	}	

}
