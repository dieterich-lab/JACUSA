package accusa2.cli.parameters;

import accusa2.method.call.statistic.StatisticCalculator;

public class StatisticParameters {

	// filter: statistic
	private double stat;
	private StatisticCalculator statisticCalculator;
	private int permutations;
	
	public StatisticParameters() {
		stat				= 0.3;
		statisticCalculator = getDefaultStatisticCalculator();
		permutations		= 100;
	}

	public int getPermutations() {
		return permutations;
	}

	public void setPermutations(int permutations) {
		this.permutations = permutations;
	}

	protected StatisticCalculator getDefaultStatisticCalculator() {
		return null; // FIXME
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
	
}