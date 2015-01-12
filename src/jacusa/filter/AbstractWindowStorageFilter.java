package jacusa.filter;

import jacusa.pileup.Counts;
import jacusa.pileup.builder.WindowCache;

public abstract class AbstractWindowStorageFilter extends AbstractStorageFilter<WindowCache> {

	public AbstractWindowStorageFilter(final char c) {
		super(c);
	}

	protected Counts[] getCounts(int genomicPosition, FilterContainer[] replicateFilterContainer) {
		final int n = replicateFilterContainer.length;
		Counts[] counts = new Counts[n];

		for (int i = 0; i < n; ++i) {
			final FilterContainer filterContainer = replicateFilterContainer[i];
			final WindowCache windowCache = getData(filterContainer);
			final int windowPosition = genomicPosition - filterContainer.getWindowCoordinates().getGenomicWindowStart();
			counts[i] = new Counts(windowCache.getBaseI(windowPosition), windowCache.getQual(windowPosition));
		}

		return counts;
	}

}