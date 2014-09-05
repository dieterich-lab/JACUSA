package accusa2.filter;

import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class RatioBasedFilter extends AbstractCountFilter {

	private double minRatio;
	
	public RatioBasedFilter(char c, double minRatio, BaseConfig baseConfig, FilterConfig filterConfig) {
		super(c, baseConfig, filterConfig);
		this.minRatio = minRatio;
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		int[] variantBaseIs = getVariantBaseI(parallelPileup);

		// FIXME what is a variant?
		if (variantBaseIs.length == 0) {
			return false;
		}

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