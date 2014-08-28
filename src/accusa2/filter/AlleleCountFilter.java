package accusa2.filter;

import accusa2.pileup.ParallelPileup;

public class AlleleCountFilter extends AbstractParallelPileupFilter {

	public AlleleCountFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		// filter if there are more than 2 alleles
		return parallelPileup.getPooledPileup().getAlleles().length > 2;
	}

}