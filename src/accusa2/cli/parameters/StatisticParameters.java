package accusa2.cli.parameters;

import accusa2.estimate.AbstractEstimateParameters;
import accusa2.method.call.statistic.StatisticCalculator;

public class StatisticParameters {

	// filter: statistic
	private StatisticCalculator statisticCalculator;
	@Deprecated
	private AbstractEstimateParameters estimateParameters;
	private double stat;
	private int permutations;
	
	public StatisticParameters() {
		stat				= 0.3;
		permutations		= 100;
	}

	public StatisticParameters(final StatisticCalculator statisticCalculator) {
		this();
		this.statisticCalculator = statisticCalculator;
	}

	@Deprecated
	public StatisticParameters(final StatisticCalculator statisticCalculator, final AbstractEstimateParameters estimateParameters) {
		this();
		this.statisticCalculator = statisticCalculator;
		this.estimateParameters = estimateParameters;
	}

	public int getPermutations() {
		return permutations;
	}

	public void setPermutations(int permutations) {
		this.permutations = permutations;
	}

	/**
	 * @return the stat
	 */
	public double getStat() {
		return stat;
	}

	/**
	 * @param stat the stat to set
	 */
	public void setStat(double stat) {
		this.stat = stat;
	}
	
	/**
	 * @return the statisticCalculator
	 */
	public StatisticCalculator getStatisticCalculator() {
		return statisticCalculator;
	}

	/**
	 * @param statisticCalculator the statisticCalculator to set
	 */
	public void setStatisticCalculator(StatisticCalculator statisticCalculator) {
		this.statisticCalculator = statisticCalculator;
	}
	
	@Deprecated
	public AbstractEstimateParameters getEstimateParameters() {
		return estimateParameters;
	}

	@Deprecated
	public void setEstimateParameters(AbstractEstimateParameters estimateParameters) {
		this.estimateParameters = estimateParameters;
	}

}