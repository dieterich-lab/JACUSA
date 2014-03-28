package accusa2.filter.process;

import accusa2.pileup.ParallelPileup;

// CHECKED
public class AlleleParallelPileupFilter extends AbstractParallelPileupFilter {

	public AlleleParallelPileupFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		this.filteredParallelPileup = parallelPileup;

		// filter if there are more than 2 alleles
		if(parallelPileup.getPooledPileup().getAlleles().length > 2) {
			return true;
		}

		this.filteredParallelPileup = parallelPileup;
		return false;
	}

	@Override
	public boolean quitFiltering() {
		return filteredParallelPileup == null;
	}

}
