package accusa2.filter.process;

import accusa2.pileup.ParallelPileup;

public class PolymorphismParallelPileupFilter extends AbstractParallelPileupFilter {

	public PolymorphismParallelPileupFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		this.filteredParallelPileup = null;

		// true when all bases are A in pileup1 and C in pileup pileup2
		if(parallelPileup.getPooledPileup1().getAlleles().length ==  1 && parallelPileup.getPooledPileup2().getAlleles().length ==  1) {
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
