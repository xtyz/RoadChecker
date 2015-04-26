package cz.pochoto.roadchecker.utils;

/**
 * Mean Counting
 * @author Tomáš Pochobradský
 *
 */
public class Mean extends AbstractCounter{

	/**
	 * Extends {@link cz.pochoto.roadchecker.utils.AbstractCounter#addValue(double, double)}
	 * @param curentAcc
	 */
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
