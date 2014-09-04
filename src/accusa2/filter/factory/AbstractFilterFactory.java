package accusa2.filter.factory;

import accusa2.filter.cache.AbstractCountFilterCache;
import accusa2.filter.feature.AbstractFeatureFilter;

public abstract class AbstractFilterFactory {

	public final static char SEP = ':';

	protected char c;
	protected String desc;
	
	public AbstractFilterFactory(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public abstract AbstractFeatureFilter getFilterInstance();
	public abstract AbstractCountFilterCache getFilterCountInstance();

	public final char getC() {
		return c;
	}

	public final String getDesc() {
		return desc;
	}

	public void processCLI(final String line) throws IllegalArgumentException {
		// implement to change behavior via CLI
	}

}