package jacusa.pileup.iterator.variant;

import jacusa.pileup.ParallelPileup;

public class VariantParallelPileup1 implements Variant {
	
	@Override
	public boolean isValid(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup1().getAlleles().length > 1;
	}

}