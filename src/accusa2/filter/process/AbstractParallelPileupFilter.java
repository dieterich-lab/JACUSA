package accusa2.filter.process;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractParallelPileupFilter {
	
	private char c;
	protected ParallelPileup filteredParallelPileup;
	
	public AbstractParallelPileupFilter(char c) {
		this.c = c;
	}

	public abstract boolean filter(ParallelPileup parallelPileup);

	public boolean quitFiltering() {
		return false;
	}

	public ParallelPileup getFilteredParallelPileup() {
		return filteredParallelPileup;
	}

	public final char getC() {
		return c;
	}

}
