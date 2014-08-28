package accusa2.filter;

import accusa2.pileup.ParallelPileup;

public class PolymorphismFilter extends AbstractParallelPileupFilter {

	public PolymorphismFilter(char c) {
		super(c);
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		// true when all bases are A in pileup1 and C in pileup pileup2
		if(parallelPileup.getPooledPileupA().getAlleles().length ==  1 && parallelPileup.getPooledPileupB().getAlleles().length ==  1) {
			return true;
		}

		return false;
	}

}