package jacusa.filter;

import jacusa.filter.counts.AbstractCountFilter;
import jacusa.filter.counts.RatioCountFilter;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class DistanceStorageFilter extends AbstractWindowStorageFilter {

	private double ratio = 0.5;
	private AbstractCountFilter countFilter;
	
	public DistanceStorageFilter(final char c, BaseConfig baseConfig, FilterConfig filterConfig) {
		super(c);

		countFilter = new RatioCountFilter(ratio, baseConfig, filterConfig);
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup, Location location, AbstractWindowIterator windowIterator) {
		Counts[] counts1 = getCounts(parallelPileup.getPosition(), windowIterator.getFilterContainers4Replicates1(location));
		Counts[] counts2 = getCounts(parallelPileup.getPosition(), windowIterator.getFilterContainers4Replicates2(location));

		return countFilter.filter(parallelPileup, counts1, counts2);
	}

}