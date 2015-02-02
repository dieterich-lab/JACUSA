package jacusa.filter;

import jacusa.filter.counts.AbstractCountFilter;
import jacusa.filter.counts.MinCountFilter;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class HomopolymerStorageFilter extends AbstractWindowStorageFilter {

	private AbstractCountFilter countFilter;
	
	public HomopolymerStorageFilter(final char c, BaseConfig baseConfig, FilterConfig filterConfig) {
		super(c);

		countFilter = new MinCountFilter(c, 1, baseConfig, filterConfig);
	}

	@Override
	public boolean filter(ParallelPileup parallelPileup, Location location, AbstractWindowIterator windowIterator) {
		Counts[] counts1 = getCounts(location, windowIterator.getFilterContainers4Replicates1(location));
		Counts[] counts2 = getCounts(location, windowIterator.getFilterContainers4Replicates2(location));

		final int[] variantBaseIs = countFilter.getVariantBaseIs(parallelPileup);
		if (variantBaseIs.length == 0 || variantBaseIs.length > 1) {
			return false;
		}
		
		return countFilter.filter(variantBaseIs, parallelPileup, counts1, counts2);
	}
	
}