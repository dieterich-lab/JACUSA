package jacusa.filter;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class HomopolymerStorageFilter extends AbstractWindowStorageFilter {

	public HomopolymerStorageFilter(final char c, BaseConfig baseConfig, FilterConfig filterConfig) {
		super(c);
	}

	@Override
	protected boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
		final Counts[] counts1 = getCounts(location, windowIterator.getFilterContainers4Replicates1(location));
		final Counts[] counts2 = getCounts(location, windowIterator.getFilterContainers4Replicates2(location));
		
		for (final Counts counts : counts1) {
			if (counts.getCoverage() > 0) {
				return true;
			}
		}
		for (final Counts counts : counts2) {
			if (counts.getCoverage() > 0) {
				return true;
			}
		}
		
		return false;
	}
	
}