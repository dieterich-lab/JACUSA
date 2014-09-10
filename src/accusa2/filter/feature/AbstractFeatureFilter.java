package accusa2.filter.feature;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractFeatureFilter {

	protected final char c;
	
	public AbstractFeatureFilter(final char c) {
		this.c = c;
	}

	final public char getC() {
		return c;
	}

	public abstract boolean filter(final ParallelPileup parallelPileup);

}