package cz.pochoto.roadchecker.utils;

/**
 * Standard Deviation Counting
 * @author Tomáš Pochobradský
 *
 */
public class StandardDeviation extends AbstractCounter{
	
	@Override
	protected double count() {
		value = value + ((aValue - aMean)*(aValue - aMean));
		result = this.value / count;
		return result;
	}	

}
