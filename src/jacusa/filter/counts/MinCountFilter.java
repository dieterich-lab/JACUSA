package jacusa.filter.counts;

import jacusa.filter.FilterConfig;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;

public class MinCountFilter extends AbstractCountFilter {

	private double minCount;
	
	public MinCountFilter(final char c, 
			final double minCount, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig) {
		super(baseConfig, filterConfig);
		this.minCount = minCount;
	}

	@Override
	protected boolean filter(int variantBaseI, ParallelPileup parallelPileup, Counts[] counts1, Counts[] counts2) {
		int count = parallelPileup.getPooledPileup().getCounts().getBaseCount()[variantBaseI];
		if (count == 0) {
			return false;
		}

		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup, counts1, counts2);
		int filteredCount = filtered.getPooledPileup().getCounts().getBaseCount()[variantBaseI];

		return count - filteredCount >= minCount;
	}

	public double getMinCount() {
		return minCount;
	}
	
}