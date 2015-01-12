package jacusa.pileup.iterator.variant;

import jacusa.pileup.ParallelPileup;

public class VariantParallelPileup implements Variant {
	
	@Override
	public boolean isValid(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
	}

}