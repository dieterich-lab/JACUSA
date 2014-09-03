package accusa2.filter.feature;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractFeatureFilter {

	final private char c;

	public AbstractFeatureFilter(char c) {
		this.c = c;
	}

	final public char getC() {
		return c;
	}
	
	public abstract boolean filter(final ParallelPileup parallelPileup);

}