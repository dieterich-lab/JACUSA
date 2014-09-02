package accusa2.cli.parameters;

import accusa2.filter.FilterConfig;
import accusa2.method.statistic.StatisticCalculator;

public class StatisticParameters {

	// filter: statistic
	private double stat;
	private StatisticCalculator statisticCalculator;
	private FilterConfig filterConfig;
	
	public StatisticParameters() {
		stat				= 0.3;
		statisticCalculator = getDefaultStatisticCalculator();
		filterConfig 		= new FilterConfig(this); // TODO
	}

	/**
	 * @return the filterConfig
	 */
	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

	protected StatisticCalculator getDefaultStatisticCalculator() {
		return null; // TODO
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