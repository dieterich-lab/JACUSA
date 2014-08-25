package accusa2.filter.cache;

import accusa2.pileup.ParallelPileup;

public class PolymorphismParallelPileupFilter extends AbstractParallelPileupFilter {

	public PolymorphismParallelPileupFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		this.filtered = null;

		// true when all bases are A in pileup1 and C in pileup pileup2
		if(parallelPileup.getPooledPileupA().getAlleles().length ==  1 && parallelPileup.getPooledPileupB().getAlleles().length ==  1) {
			return true;
		}

		this.filtered = parallelPileup;
		return false;
	}

	@Override
	public boolean quitFiltering() {
		return filtered == null;
	}

}
