package accusa2.pileup.iterator;

import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.FilterConfig;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractWindowIterator implements Iterator<ParallelPileup> {

	protected final AnnotatedCoordinate coordinate;
	protected FilterConfig filterconfig;
	protected Variant filter;
	
	public AbstractWindowIterator(final AnnotatedCoordinate annotatedCoordinate, final Variant filter, final AbstractParameters parameters) {
		this.coordinate = annotatedCoordinate;

		this.filter		= filter;
		filterconfig 	= parameters.getFilterConfig();
	}

	protected void initLocation(Location location, final boolean isDirectional, final AbstractPileupBuilder[] pileupBuilders) {
		final SAMRecord record = getNextValidRecord(coordinate.getStart(), pileupBuilders);
		if (record == null) {
			location.strand = STRAND.UNKNOWN;
			location.genomicPosition = -1;
			return;
		}

		final int genomicPosition = record.getAlignmentStart();
		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			pileupBuilder.adjustWindowStart(genomicPosition);
		}

		location.genomicPosition = genomicPosition;
		if (isDirectional) {
			if (record.getReadNegativeStrandFlag()) {
				location.strand = STRAND.REVERSE;
			} else {
				location.strand = STRAND.FORWARD;
			}
		}
	}
		
	public abstract boolean hasNext();
	public abstract ParallelPileup next();
	protected abstract void advance();
	protected abstract void advance(Location location);
	
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
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader[] readers, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		AbstractPileupBuilder[] pileupBuilders = new AbstractPileupBuilder[readers.length];

		for(int i = 0; i < readers.length; ++i) {
			pileupBuilders[i] = pileupBuilderFactory.newInstance(annotatedCoordinate, readers[i], sample, parameters);
		}

		return pileupBuilders;
	}

	// TODO at least two need to be covered
	protected SAMRecord getNextValidRecord(final int targetGenomicPosition, final AbstractPileupBuilder[] pileupBuilders) {
		return pileupBuilders[0].getNextValidRecord(targetGenomicPosition);
	}
	/*
		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			record = pileupBuilder.getNextValidRecord(targetGenomicPosition);
			if (record != null) {
				return record;
			}
		}
	}
	*/

	// TODO make this more quantitative
	protected boolean isCovered(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(location.genomicPosition);
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

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(location.genomicPosition);
		for(int i = 0; i < n; ++i) {
			pileups[i] = pileupBuilders[i].getPileup(windowPosition, location.strand);
		}

		return pileups;
	}

	protected Counts[][] getCounts(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Counts[][] counts = new Counts[n][filterconfig.getFactories().size()];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(location.genomicPosition);
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuilders[i].getFilteredCounts(windowPosition, location.strand);
		}

		return counts;
	}

	protected boolean adjustCurrentGenomicPosition(Location location, AbstractPileupBuilder[] pileupBuilders) {
		if (! pileupBuilders[0].isContainedInWindow(location.genomicPosition)) {
			return adjustWindowStart(location, pileupBuilders);
		}

		return true;
	}

	protected boolean adjustWindowStart(Location location, AbstractPileupBuilder[] pileupBuilders) {
		if (! pileupBuilders[0].adjustWindowStart(location.genomicPosition)) {
			SAMRecord record = getNextValidRecord(pileupBuilders[0].getWindowEnd(), pileupBuilders);
			if (record == null) {
				return false;
			}
			location.genomicPosition = record.getAlignmentStart();
			return adjustWindowStart(location, pileupBuilders);
		}
		location.genomicPosition = pileupBuilders[0].getGenomicWindowStart();
		for (int i = 1; i < pileupBuilders.length; ++i) {
			pileupBuilders[i].adjustWindowStart(location.genomicPosition);
		}

		return true;
	}

	protected boolean hasNext(Location location, final AbstractPileupBuilder[] pileupBuilders) {
		// within
		while (location.genomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0].isContainedInWindow(location.genomicPosition)) {
				if (isCovered(location, pileupBuilders)) {
					return true;
				} else {
					// move along the window
					advance(location);
				}
			} else {
				if (! adjustWindowStart(location, pileupBuilders)) {
					return false;
				}
			}
		}

		return false;
	}

	protected Pileup[] complementPileups(Pileup[] pileups) {
		Pileup[] complementedPileups = new DefaultPileup[pileups.length];

		for (int i = 0; i < pileups.length; ++i) {
			complementedPileups[i] = pileups[i].complement();
		}

		return complementedPileups;
	}
	
	protected Counts[][] complementCounts(Counts[][] counts) {
		int replicates = counts.length;
		int filterCount = filterconfig.getFactories().size();
		Counts[][] complementedCounts = new Counts[replicates][filterCount];

		for (int i = 0; i < replicates; ++i) {
			for (int j = 0; j < filterCount; ++j) {
				complementedCounts[i][j] = counts[i][j]; // TODO check
				complementedCounts[i][j].invertCounts();
			}
		}

		return complementedCounts;
	}
	
	public AnnotatedCoordinate getAnnotatedCoordinate() {
		return coordinate;
	}

	@Override
	public void remove() {
		// not needed
	}

	/*
	 * Mean to be used only by Iterator(s)
	 */

	protected class Location {

		protected int genomicPosition;
		protected STRAND strand;

		public Location(int genomicPosition, STRAND strand) {
			this.genomicPosition = genomicPosition;
			this.strand = strand;
		}

	}

}