package accusa2.filter;

import accusa2.cli.parameters.Parameters;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.ParallelPileup;

public class WithinSampleStatisticFilter extends AbstractCacheFilter {

	private double minScore;
	private StatisticCalculator statistic;
	
	public WithinSampleStatisticFilter(char c, double minScore, Parameters parameters) {
		super(c, parameters);
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