package cz.pochoto.roadchecker.utils;

/**
 * Absract class used for counting means, standard deviations, ats. Statistical functions, where is able to add new values and recaunt result
 * @author Tomáš Pochobradský
 *
 */
public abstract class AbstractCounter {
	/**
	 * Poèet hodnot (jmenovatel)
	 */
	protected double count = 0;
	/**
	 * Souèet prvkù v èitateli
	 */
	protected double value = 0;
	/**
	 * Koneèný výsledek
	 */
	protected double result = 0;
	
	protected double aValue = 0;
	protected double aMean = 0;
	
	/**
	 * Returns result of counting
	 * @return
	 */
	public double getResult(){		
		return result;
	}
	
	/**
	 * Adds new values for recounting result
	 * @param aValue
	 * @param aMean
	 */
	public void addValue(double aValue, double aMean){
		this.aValue = aValue;
		this.aMean = aMean;
		count++;		
		count();
	}
	
	/**
	 * Reset values
	 */
	public void reset(){
		count = 0;
		value = 0;
		result = 0;
	}
	
	/**
	 * Counts result from values given by 
	 * {@link cz.pochoto.roadchecker.utils.AbstractCounter#addValue(double, double)}
	 * @return
	 */
	protected abstract double count();
}
