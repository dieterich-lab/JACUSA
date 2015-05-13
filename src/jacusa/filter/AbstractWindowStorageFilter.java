package jacusa.filter;


import jacusa.pileup.Counts;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.builder.WindowCache;
import jacusa.util.Location;

public abstract class AbstractWindowStorageFilter extends AbstractStorageFilter<WindowCache> {

	public AbstractWindowStorageFilter(final char c) {
		super(c);
	}

	protected Counts[] getCounts(final Location location, FilterContainer[] replicateFilterContainer) {
		final int n = replicateFilterContainer.length;
		Counts[] counts = new Counts[n];

		// correct orientation in U,S S,U cases
		boolean invert = false;
		if (location.strand == STRAND.REVERSE && replicateFilterContainer[0].getStrand() == STRAND.UNKNOWN) {
			invert = true;
		}
		
		for (int i = 0; i < n; ++i) {
			final FilterContainer filterContainer = replicateFilterContainer[i];
			final WindowCache windowCache = getData(filterContainer);
			final int windowPosition = filterContainer.getWindowCoordinates().convert2WindowPosition(location.genomicPosition);

			counts[i] = windowCache.getCounts(windowPosition);
			if (invert) {
				counts[i].invertCounts();
			}
			
		}

		return counts;
	}

}