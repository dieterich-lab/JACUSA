package accusa2.filter.cache;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractParallelPileupFilter {

	private char c;
	protected ParallelPileup filtered;

	public AbstractParallelPileupFilter(char c) {
		this.c = c;
	}

	public abstract boolean filter(ParallelPileup parallelPileup);
	public boolean quitFiltering() {
		return false;
	}

	public ParallelPileup getFilteredParallelPileup() {
		return filtered;
	}

	public final char getC() {
		return c;
	}

}