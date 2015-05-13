package jacusa.pileup.sample;

import jacusa.pileup.ParallelPileup;

@Deprecated
public interface PermutateParallelPileup {

	/**
	 * 
	 * @param parallelPileup
	 * @return a permutated version of parallelPileup
	 */
	ParallelPileup permutate(ParallelPileup parallelPileup);

}