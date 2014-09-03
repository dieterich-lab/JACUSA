package accusa2.filter.feature;

import accusa2.pileup.ParallelPileup;

public class AlleleCountFilter extends AbstractFeatureFilter {

	public AlleleCountFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		// filter if there are more than 2 alleles
		return parallelPileup.getPooledPileup().getAlleles().length > 2;
	}

}