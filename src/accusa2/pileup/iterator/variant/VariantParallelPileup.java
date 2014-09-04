package accusa2.pileup.iterator.variant;

import accusa2.pileup.ParallelPileup;

public class VariantParallelPileup implements Variant {
	
	@Override
	public boolean isValid(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
	}

}