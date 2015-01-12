package jacusa.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.samtools.CigarOperator;

import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.util.WindowCoordinates;

public class FilterContainer {

	private FilterConfig filterConfig;
	private AbstractFilterStorage<?>[] filters; 
	private WindowCoordinates windowCoordinates;
	
	private Map<CigarOperator, Set<AbstractFilterStorage<?>>> cigar2filter;
	
	public FilterContainer(final FilterConfig filterConfig, final AbstractFilterStorage<?>[] filters, WindowCoordinates windowCoordinates) {
		this.filterConfig = filterConfig;
		this.filters = filters;
		this.windowCoordinates = windowCoordinates;
		
		cigar2filter = new HashMap<CigarOperator, Set<AbstractFilterStorage<?>>>();
		for (AbstractFilterStorage<?> filter : filters) {
			final char c = filter.getC();
			final int i = filterConfig.c2i(c);

			for (CigarOperator cigarOperator : filterConfig.getFactories().get(i).getCigarOperators()) {
				if (! cigar2filter.containsKey(cigarOperator)) {
					cigar2filter.put(cigarOperator, new HashSet<AbstractFilterStorage<?>>());
				}
				cigar2filter.get(cigarOperator).add(filter);
			}
		}
	}

	public void clear() {
		for (AbstractFilterStorage<?> filter : filters) {
			filter.clearContainer();
		}
	}

	public AbstractFilterStorage<?> get(int filterI) {
		return filters[filterI];
	}
	
	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

	public WindowCoordinates getWindowCoordinates() {
		return windowCoordinates;
	}

	public Set<AbstractFilterStorage<?>> get(CigarOperator cigarOperator) {
		if (cigar2filter.containsKey(cigarOperator)) {
			return cigar2filter.get(cigarOperator);
		} else {
			return new HashSet<AbstractFilterStorage<?>>();
		}
	}

}