package accusa2.filter;

import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;

public class CountBasedFilter extends AbstractCacheFilter {

	private double range;
	
	public CountBasedFilter(char c, double range, Parameters parameters) {
		super(c, parameters);
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