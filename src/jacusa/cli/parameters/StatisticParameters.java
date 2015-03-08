package jacusa.cli.parameters;

import jacusa.method.call.statistic.StatisticCalculator;

public class StatisticParameters {

	// filter: statistic
	private StatisticCalculator statisticCalculator;
	private double maxStat;
	
	
	public StatisticParameters() {
		maxStat				= 0.3;
	}

	public StatisticParameters(final StatisticCalculator statisticCalculator) {
		this();
		this.statisticCalculator = statisticCalculator;
	}

	/**
	 * @return the maxStat
	 */
	public double getMaxStat() {
		return maxStat;
	}

	/**
	 * @param stat the stat to set
	 */
	public void setStat(double stat) {
		this.maxStat = stat;
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

}