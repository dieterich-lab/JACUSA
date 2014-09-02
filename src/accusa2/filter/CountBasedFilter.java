package accusa2.filter;

import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class CountBasedFilter extends AbstractCacheFilter {

	private double range;
	
	public CountBasedFilter(char c, double range, BaseConfig baseConfig, FilterConfig filterConfig) {
		super(c, baseConfig, filterConfig);
		this.range = range;
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		int[] variantBaseIs = getVariantBaseI(parallelPileup);
		
		int variantBaseI = variantBaseIs[0];
		int count = parallelPileup.getPooledPileup().getBaseCount()[variantBaseI];
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup);
		int filteredCount = filtered.getPooledPileup().getBaseCount()[variantBaseI];
		
		return count - filteredCount >= range;
	}

	public double getMinRation() {
		return range;
	}
	
}