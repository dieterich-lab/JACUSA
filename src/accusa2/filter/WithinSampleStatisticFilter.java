package accusa2.filter;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class WithinSampleStatisticFilter extends AbstractCountFilter {

	private double minScore;
	private StatisticCalculator statistic;
	
	public WithinSampleStatisticFilter(
			char c, 
			double minScore, 
			BaseConfig baseConfig, 
			FilterConfig filterConfig,
			StatisticParameters parameters) {
		super(c, baseConfig, filterConfig);
		this.minScore = minScore;
		statistic = parameters.getStatisticCalculator().newInstance();
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