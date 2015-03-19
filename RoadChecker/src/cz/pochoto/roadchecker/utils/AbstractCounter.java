package cz.pochoto.roadchecker.utils;

public abstract class AbstractCounter {
	/**
	 * Po�et hodnot (jmenovatel)
	 */
	protected double count = 0;
	/**
	 * Sou�et prvk� v �itateli
	 */
	protected double value = 0;
	/**
	 * Kone�n� v�sledek
	 */
	protected double result = 0;
	
	protected double aValue = 0;
	protected double aMean = 0;
	
	public double getResult(){		
		return result;
	}
	
	public void addValue(double aValue, double aMean){
		this.aValue = aValue;
		this.aMean = aMean;
		count++;		
		count();
	}
	
	public void reset(){
		count = 0;
		value = 0;
		result = 0;
	}
	
	protected abstract double count();
}
