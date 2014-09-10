package accusa2.filter;

import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class CountBasedFilter extends AbstractCountFilter {

	private double range;
	
	public CountBasedFilter(final char c, 
			final double range, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig) {
		super(c, baseConfig, filterConfig);
		this.range = range;
	}

	@Override
	public boolean filter(int variantBaseI, ParallelPileup parallelPileup) {
		int count = parallelPileup.getPooledPileup().getBaseCount()[variantBaseI];
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup);
		int filteredCount = filtered.getPooledPileup().getBaseCount()[variantBaseI];
		
		return count - filteredCount >= range;
	}

	public double getMinRation() {
		return range;
	}
	
}