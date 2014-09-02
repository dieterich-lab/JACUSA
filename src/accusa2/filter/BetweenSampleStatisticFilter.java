package accusa2.filter;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class BetweenSampleStatisticFilter extends AbstractCacheFilter {

	final private StatisticCalculator statistic;
	final private double minScore;
	
	public BetweenSampleStatisticFilter(
			final char c, 
			final double minScore,
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig,
			final StatisticParameters parameters) {
		super(c, baseConfig, filterConfig);
		statistic = parameters.getStatisticCalculator().newInstance();
		this.minScore = minScore;
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		int[] variantBaseIs = getVariantBaseI(parallelPileup);

		int variantBaseI = variantBaseIs[0];
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup);
		double value = statistic.getStatistic(filtered);

		return value < minScore;
	}

}