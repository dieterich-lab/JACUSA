package accusa2.filter.factory;

import accusa2.filter.AbstractParallelPileupFilter;
import accusa2.filter.FilterConfig;
import accusa2.filter.cache.AbstractPileupBuilderFilterCount;

public abstract class AbstractFilterFactory {

	public final static char SEP = ':';

	protected char c;
	protected String desc;
	protected FilterConfig filterConfig;
	
	public AbstractFilterFactory(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public abstract AbstractParallelPileupFilter getFilterInstance();
	public abstract AbstractPileupBuilderFilterCount getFilterCountInstance();

	public final char getC() {
		return c;
	}

	public final String getDesc() {
		return desc;
	}

	public void processCLI(final String line) throws IllegalArgumentException {
		// implement to change behavior via CLI
	}

	public void setFilterConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

}