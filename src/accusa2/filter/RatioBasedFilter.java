package accusa2.filter;

import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class RatioBasedFilter extends AbstractCountFilter {

	private double minRatio;
	
	public RatioBasedFilter(final char c, 
			final double minRatio, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig) {
		super(c, baseConfig, filterConfig);
		this.minRatio = minRatio;
	}

	@Override
	public boolean filter(int variantBaseI, ParallelPileup parallelPileup) {
		int count = parallelPileup.getPooledPileup().getBaseCount()[variantBaseI];
		ParallelPileup filtered = applyFilter(variantBaseI, parallelPileup);
		int filteredCount = filtered.getPooledPileup().getBaseCount()[variantBaseI];
		
		return (double)filteredCount / (double)count <= minRatio;
	}

	public double getMinRation() {
		return minRatio;
	}

}