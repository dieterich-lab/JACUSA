package jacusa.method.call.statistic;

import jacusa.pileup.ParallelPileup;

public interface StatisticCalculator {

	// Make sure this is always >= 0
	public double getStatistic(ParallelPileup parallelPileup);
	
	// filter everything < value  
	public boolean filter(double value);

	public StatisticCalculator newInstance();

	public String getName();
	public String getDescription();

	// process command line
	public void processCLI(final String line);

}