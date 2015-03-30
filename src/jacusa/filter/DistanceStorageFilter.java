package jacusa.filter;

import jacusa.filter.counts.AbstractCountFilter;
import jacusa.filter.counts.RatioCountFilter;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.result.Result;
import jacusa.util.Location;

public class DistanceStorageFilter extends AbstractWindowStorageFilter {

	private AbstractCountFilter countFilter;
	
	public DistanceStorageFilter(final char c, final double minRatio, final int minCount, final BaseConfig baseConfig, final FilterConfig filterConfig) {
		super(c);

		countFilter = new RatioCountFilter(minRatio, baseConfig, filterConfig);
		// TODO use minCount
	}

	@Override
	public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
		final ParallelPileup parallelPileup = result.getParellelPileup();

		Counts[] counts1 = getCounts(location, windowIterator.getFilterContainers4Replicates1(location));
		Counts[] counts2 = getCounts(location, windowIterator.getFilterContainers4Replicates2(location));

		final int[] variantBaseIs = countFilter.getVariantBaseIs(parallelPileup);
		if (variantBaseIs.length == 0) {
			return false;
		}
		
		return countFilter.filter(variantBaseIs, parallelPileup, counts1, counts2);
	}

}