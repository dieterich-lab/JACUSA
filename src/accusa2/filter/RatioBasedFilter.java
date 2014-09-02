package accusa2.filter;

import accusa2.pileup.ParallelPileup;

public class RatioBasedFilter extends AbstractCacheFilter {

	private double minRatio;
	
	public RatioBasedFilter(char c, double minRatio) {
		super(c);
		this.minRatio = minRatio;
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		int[] variantBaseIs = getVariantBaseI(parallelPileup);
		
		int variantBaseI = variantBaseIs[0];
		int count = parallelPileup.getPooledPileup().getBaseCount()[variantBaseI];
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup);
		int filteredCount = filtered.getPooledPileup().getBaseCount()[variantBaseI];
		
		return (double)filteredCount / (double)count <= minRatio;
	}

	public double getMinRation() {
		return minRatio;
	}
	
}