package accusa2.filter;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractParallelPileupFilter {

	final private char c;

	public AbstractParallelPileupFilter(char c) {
		this.c = c;
	}

	final public char getC() {
		return c;
	}
	
	public abstract boolean filter(final ParallelPileup parallelPileup);

}