package accusa2.filter.cache;

import accusa2.pileup.ParallelPileup;

public class AlleleParallelPileupFilter extends AbstractParallelPileupFilter {

	public AlleleParallelPileupFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		this.filtered = null;

		// filter if there are more than 2 alleles
		if(parallelPileup.getPooledPileup().getAlleles().length > 2) {
			return true;
		}

		filtered = parallelPileup;
		return false;
	}

	@Override
	public boolean quitFiltering() {
		return filtered == null;
	}

}