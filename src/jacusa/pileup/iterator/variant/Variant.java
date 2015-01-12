package jacusa.pileup.iterator.variant;

import jacusa.pileup.ParallelPileup;

public interface Variant {
	
	boolean isValid(ParallelPileup parallelPileup);
	
}