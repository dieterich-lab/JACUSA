package accusa2.method.statistic;

import accusa2.pileup.ParallelPileup;

public interface StatisticCalculator {

	/*
	 * must be always >= 0
	 */
	public double getStatistic(ParallelPileup parallelPileup);
	
	public StatisticCalculator newInstance();

	public String getName();
	public String getDescription();

}
