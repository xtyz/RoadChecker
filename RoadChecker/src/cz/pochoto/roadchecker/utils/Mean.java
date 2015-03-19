package cz.pochoto.roadchecker.utils;

public class Mean extends AbstractCounter{

	public void addValue(double curentAcc){
		addValue(curentAcc, 0);
	}
	
	@Override
	protected double count() {
		value = this.value + aValue;
		result = this.value / count;
		return result;
	}	

}
