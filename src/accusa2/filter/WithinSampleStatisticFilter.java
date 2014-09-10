package accusa2.filter;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class WithinSampleStatisticFilter extends AbstractCountFilter {

	private double minScore;
	private StatisticCalculator statistic;
	
	public WithinSampleStatisticFilter(
			final char c,
			final double minScore, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig,
			final StatisticParameters parameters) {
		super(c, baseConfig, filterConfig);
		this.minScore = minScore;
		statistic = parameters.getStatisticCalculator().newInstance();
	}

	@Override
	public boolean filter(int variantBaseI, ParallelPileup parallelPileup) {
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup);
		
		double value = statistic.getStatistic(filtered);
		return value < minScore;
	}

}