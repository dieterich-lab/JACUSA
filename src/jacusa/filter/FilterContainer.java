package jacusa.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.samtools.CigarOperator;

import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.WindowCoordinates;

public class FilterContainer {

	private FilterConfig filterConfig;
	private AbstractFilterStorage<?>[] filterStorage;
	private List<AbstractFilterStorage<?>> cigarFilters;
	private List<AbstractFilterStorage<?>> processRecordFilters;

	private WindowCoordinates windowCoordinates;
	private STRAND strand;
	
	private Map<CigarOperator, Set<AbstractFilterStorage<?>>> cigar2cFilter;
	
	public FilterContainer(
			final FilterConfig filterConfig, 
			final AbstractFilterStorage<?>[] filters, 
			final WindowCoordinates windowCoordinates,
			final STRAND strand) {
		this.filterConfig = filterConfig;
		this.windowCoordinates = windowCoordinates;
		this.filterStorage = filters;
		this.strand = strand;
		
		cigarFilters = new ArrayList<AbstractFilterStorage<?>>(filters.length);
		processRecordFilters = new ArrayList<AbstractFilterStorage<?>>(filters.length);
		cigar2cFilter = new HashMap<CigarOperator, Set<AbstractFilterStorage<?>>>();

		for (AbstractFilterStorage<?> filter : filters) {
			// get filter factory
			final char c = filter.getC();
			final int i = filterConfig.c2i(c);
			AbstractFilterFactory<?> filterFactory = filterConfig.getFactories().get(i);

			if (filterFactory.hasFilterByRecord()) {
				processRecordFilters.add(filter);
			}
			
			if (filterFactory.hasFilterByCigar()) {
				cigarFilters.add(filter);
				for (CigarOperator cigarOperator : filterFactory.getCigarOperators()) {
					if (! cigar2cFilter.containsKey(cigarOperator)) {
						cigar2cFilter.put(cigarOperator, new HashSet<AbstractFilterStorage<?>>());
					}
					cigar2cFilter.get(cigarOperator).add(filter);
				}
			}
		}
	}

	public STRAND getStrand() {
		return strand;
	}

	public void clear() {
		for (AbstractFilterStorage<?> filter : cigarFilters) {
			filter.clearContainer();
		}
	}

	public AbstractFilterStorage<?> get(int filterI) {
		return filterStorage[filterI];
	}
	
	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

	public WindowCoordinates getWindowCoordinates() {
		return windowCoordinates;
	}

	public List<AbstractFilterStorage<?>> getPR() {
		return processRecordFilters;
	}
	
	public Set<AbstractFilterStorage<?>> get(CigarOperator cigarOperator) {
		if (cigar2cFilter.containsKey(cigarOperator)) {
			return cigar2cFilter.get(cigarOperator);
		} else {
			return new HashSet<AbstractFilterStorage<?>>();
		}
	}

}