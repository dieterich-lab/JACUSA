package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterConfig;
import jacusa.filter.FilterContainer;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.builder.AbstractPileupBuilder;
import jacusa.pileup.builder.PileupBuilderFactory;
import jacusa.pileup.iterator.location.AbstractLocationAdvancer;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;

import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

public abstract class AbstractWindowIterator implements Iterator<Location> {

	protected final Coordinate coordinate;
	protected FilterConfig filterconfig;
	protected Variant filter;
	
	protected ParallelPileup parallelPileup;
	
	protected AbstractLocationAdvancer locationAdvance;
	
	public AbstractWindowIterator(final Coordinate coordinate, final Variant filter, final AbstractParameters parameters) {
		this.coordinate = coordinate;

		this.filter		= filter;
		filterconfig 	= parameters.getFilterConfig();
		parallelPileup  = new DefaultParallelPileup();
	}

	protected Location initLocation(Coordinate coordinate, final boolean isDirectional, final AbstractPileupBuilder[] pileupBuilders) {
		parallelPileup.setContig(coordinate.getSequenceName());

		// not within coordinate
		Location location = new Location(coordinate.getSequenceName(), Integer.MAX_VALUE, STRAND.UNKNOWN);
		if (isDirectional) {
			location.strand = STRAND.FORWARD;
		}
		
		final SAMRecord record = getNextValidRecord(coordinate.getStart(), pileupBuilders);
		if (record == null) {
			return location;
		}

		// find genomicPosition within coordinate.getStart() coordinate.getEnd();
		int genomicPosition = Math.max(coordinate.getStart(), record.getAlignmentStart());
		if (genomicPosition > coordinate.getEnd()) {
			return location;
		}

		genomicPosition = Math.min(genomicPosition, coordinate.getEnd());
		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			pileupBuilder.adjustWindowStart(genomicPosition);
		}

		location.genomicPosition = genomicPosition;
		return location;
	}
		
	public abstract boolean hasNext();
	public abstract Location next();
	public abstract FilterContainer[] getFilterContainers4Replicates1(Location location);
	public abstract FilterContainer[] getFilterContainers4Replicates2(Location location);
	
	/**
	 * 
	 * @param pileupBuilderFactory
	 * @param annotatedCoordinate
	 * @param readers
	 * @param parameters
	 * @return
	 */
	protected AbstractPileupBuilder[] createPileupBuilders(
			final PileupBuilderFactory pileupBuilderFactory, 
			final Coordinate annotatedCoordinate, 
			final SAMFileReader[] readers, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		AbstractPileupBuilder[] pileupBuilders = new AbstractPileupBuilder[readers.length];

		for(int i = 0; i < readers.length; ++i) {
			pileupBuilders[i] = pileupBuilderFactory.newInstance(annotatedCoordinate, readers[i], sample, parameters);
		}

		return pileupBuilders;
	}

	protected SAMRecord getNextValidRecord(final int targetGenomicPosition, final AbstractPileupBuilder[] pileupBuilders) {
		return pileupBuilders[0].getNextValidRecord(targetGenomicPosition);
	}

	// Change here for more quantitative evaluation
	protected boolean isCovered(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int windowPosition = pileupBuilders[0]
				.getWindowCoordinates()
				.convertGenomicPosition2WindowPosition(location.genomicPosition);
		if (windowPosition < 0) {
			return false;
		}
		
		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			if (! pileupBuilder.isCovered(windowPosition, location.strand)) {
				return false;
			}
		}

		return true;
	}
	
	protected Pileup[] getPileups(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Pileup[] pileups = new DefaultPileup[n];

		int windowPosition = pileupBuilders[0].getWindowCoordinates().convertGenomicPosition2WindowPosition(location.genomicPosition);
		for(int i = 0; i < n; ++i) {
			pileups[i] = pileupBuilders[i].getPileup(windowPosition, location.strand);
		}

		return pileups;
	}

	protected FilterContainer[] getFilterCaches4Replicates(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int replicates = pileupBuilders.length;
		FilterContainer[] filterContainers = new FilterContainer[replicates];

		int windowPosition = pileupBuilders[0].getWindowCoordinates().convertGenomicPosition2WindowPosition(location.genomicPosition);
		for (int i = 0; i < replicates; ++i) {
			filterContainers[i] = pileupBuilders[i].getFilterContainer(windowPosition, location.strand);
		}

		return filterContainers;
	}

	protected boolean adjustCurrentGenomicPosition(Location location, AbstractPileupBuilder[] pileupBuilders) {
		if (! pileupBuilders[0].getWindowCoordinates().isContainedInWindow(location.genomicPosition)) {
			return adjustWindowStart(location, pileupBuilders);
		}

		return true;
	}

	protected boolean adjustWindowStart(Location location, AbstractPileupBuilder[] pileupBuilders) {
		if (! pileupBuilders[0].adjustWindowStart(location.genomicPosition)) {
			SAMRecord record = getNextValidRecord(pileupBuilders[0].getWindowCoordinates().getGenomicWindowEnd(), pileupBuilders);
			if (record == null) {
				return false;
			}
			location.genomicPosition = record.getAlignmentStart();
			return adjustWindowStart(location, pileupBuilders);
		}
		location.genomicPosition = pileupBuilders[0]
				.getWindowCoordinates()
				.getGenomicWindowStart();
		for (int i = 1; i < pileupBuilders.length; ++i) {
			pileupBuilders[i].adjustWindowStart(location.genomicPosition);
		}

		return true;
	}

	protected boolean hasNext(Location location, final AbstractPileupBuilder[] pileupBuilders) {
		// within
		while (location.genomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0]
					.getWindowCoordinates()
					.isContainedInWindow(location.genomicPosition)) {
				if (isCovered(location, pileupBuilders)) {
					return true;
				} else {
					// move along the window
					locationAdvance.advanceLocation(location);
				}
			} else {
				if (! adjustWindowStart(location, pileupBuilders)) {
					return false;
				}
			}
		}

		return false;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public ParallelPileup getParallelPileup() {
		return parallelPileup;
	}

	@Override
	public void remove() {
		// not needed
	}
	
}
