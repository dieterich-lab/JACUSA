package jacusa.method.call.statistic;

import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;

/**
 * 
 * @author Michael Piechotta
 */
public interface StatisticCalculator {

	/**
	 * Add test-statistic to result.
	 * May populate info fields of result.
	 * 
	 * @param result
	 */
	public void addStatistic(final Result result);
	
	/**
	 * Calculate test-statistic for parallelPileup.
	 * 
	 * @param parallelPileup
	 * @return
	 */
	public double getStatistic(final ParallelPileup parallelPileup);

	/**
	 * Indicates if a value is valid.
	 *   
	 * @param value
	 * @return
	 */
	public boolean filter(final double value);

	/**
	 * Returns a new instance of this StatisticCalculator.
	 * 
	 * @return
	 */
	public StatisticCalculator newInstance();

	/**
	 * Return the short name of this StatisticCalculator.
	 * @return
	 */
	public String getName();
	
	/**
	 * Return a short description of this StatisticCalculator.
	 * @return
	 */
	public String getDescription();

	/**
	 * Process command lines options.
	 * 
	 * @param line
	 * @return
	 */
	public boolean processCLI(final String line);

}