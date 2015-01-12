package jacusa.pileup.iterator.variant;

import jacusa.pileup.ParallelPileup;

public class AllParallelPileup implements Variant {

	@Override
	public boolean isValid(ParallelPileup parallelPileup) {
		return true;
	}

}