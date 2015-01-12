package jacusa.filter.counts;

import jacusa.filter.FilterConfig;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;

public class RangeCountFilter extends AbstractCountFilter {

	private double range;
	
	public RangeCountFilter(final char c, 
			final double range, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig) {
		super(baseConfig, filterConfig);
		this.range = range;
	}

	@Override
	protected boolean filter(int variantBaseI, ParallelPileup parallelPileup, Counts[] counts1, Counts[] counts2) {
		int count = parallelPileup.getPooledPileup().getCounts().getBaseCount()[variantBaseI];
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup, counts1, counts2);
		int filteredCount = filtered.getPooledPileup().getCounts().getBaseCount()[variantBaseI];
		
		return count - filteredCount >= range;
	}

	public double getRange() {
		return range;
	}
	
}