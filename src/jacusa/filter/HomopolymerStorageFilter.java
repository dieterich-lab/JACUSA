package jacusa.filter;

import jacusa.filter.counts.AbstractCountFilter;
import jacusa.filter.counts.RangeCountFilter;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class HomopolymerStorageFilter extends AbstractWindowStorageFilter {

	private AbstractCountFilter countFilter;
	
	public HomopolymerStorageFilter(final char c, BaseConfig baseConfig, FilterConfig filterConfig) {
		super(c);

		countFilter = new RangeCountFilter(c, 1, baseConfig, filterConfig);
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup, Location location, AbstractWindowIterator windowIterator) {
		Counts[] counts1 = getCounts(parallelPileup.getPosition(), windowIterator.getFilterContainers4Replicates1(location));
		Counts[] counts2 = getCounts(parallelPileup.getPosition(), windowIterator.getFilterContainers4Replicates2(location));

		return countFilter.filter(parallelPileup, counts1, counts2);
	}
	
}
