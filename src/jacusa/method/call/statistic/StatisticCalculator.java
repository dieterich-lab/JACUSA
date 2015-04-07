package jacusa.method.call.statistic;

import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;


public interface StatisticCalculator {

	// Make sure this is always >= 0
	public void addStatistic(final Result result);
	public double getStatistic(final ParallelPileup parallelPileup);

	// filter everything < value  
	public boolean filter(final double value);

	public StatisticCalculator newInstance();

	public String getName();
	public String getDescription();

	// process command line
	public boolean processCLI(final String line);

}