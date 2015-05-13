package jacusa.filter.counts;

import jacusa.filter.FilterConfig;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;

public class RatioCountFilter extends AbstractCountFilter {

	private double minRatio;

	public RatioCountFilter(final double minRatio, final BaseConfig baseConfig, final FilterConfig filterConfig) {
		super(baseConfig, filterConfig);
		this.minRatio = minRatio;
	}

	@Override
	protected boolean filter(int variantBaseI, ParallelPileup parallelPileup, Counts[] counts1, Counts[] counts2) {
		int count = parallelPileup.getPooledPileup().getCounts().getBaseCount(variantBaseI);
		ParallelPileup filteredParallelPileup = applyFilter(variantBaseI, parallelPileup, counts1, counts2);
		int filteredCount = filteredParallelPileup.getPooledPileup().getCounts().getBaseCount(variantBaseI);
		return (double)filteredCount / (double)count <= minRatio;
	}

	public double getMinRatio() {
		return minRatio;
	}

}