package accusa2.method.call.statistic;

import accusa2.pileup.ParallelPileup;

public interface StatisticCalculator {

	// Make sure this is always >= 0
	public double getStatistic(ParallelPileup parallelPileup);
	public boolean filter(double value);

	public StatisticCalculator newInstance();

	public String getName();
	public String getDescription();

}
