package accusa2.pileup.iterator;

import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.Parameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractParallelPileupWindowIterator implements Iterator<ParallelPileup> {

	protected int genomicPositionA;
	protected int genomicPositionB;
	
	protected STRAND strandA;
	protected STRAND strandB;
	
	protected final AnnotatedCoordinate coordinate;
	protected final AbstractParameters parameters;

	// pileupBuilders
	protected final AbstractPileupBuilder[] pileupBuildersA;
	protected final AbstractPileupBuilder[] pileupBuildersB;

	protected int filterCount;

	// output
	protected ParallelPileup parallelPileup;

	public AbstractParallelPileupWindowIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readersA, final SAMFileReader[] readersB, final AbstractParameters parameters) {
		this.coordinate = annotatedCoordinate;
		this.parameters = parameters;

		pileupBuildersA = createPileupBuilders(parameters.getPileupBuilderFactoryA(), annotatedCoordinate, readersA, parameters);
		pileupBuildersB = createPileupBuilders(parameters.getPileupBuilderFactoryB(), annotatedCoordinate, readersB, parameters);

		strandA 		= STRAND.UNKNOWN;
		strandB 		= STRAND.UNKNOWN;
		
		filterCount 	= parameters.getFilterConfig().getFactories().size();

		// init
		parallelPileup = new DefaultParallelPileup(pileupBuildersA.length, pileupBuildersB.length);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());
		
		genomicPositionA = init(strandA, parameters.getPileupBuilderFactoryA().isDirected(), pileupBuildersA);
		genomicPositionB = init(strandB, parameters.getPileupBuilderFactoryA().isDirected(), pileupBuildersB);
	}

	protected int init(STRAND strand, final boolean isDirectional, final AbstractPileupBuilder[] pileupBuilders) {
		final SAMRecord record = getNextValidRecord(coordinate.getStart(), pileupBuilders);
		if (record == null) {
			strand = STRAND.UNKNOWN;
			return -1;
		}

		final int genomicPosition = record.getAlignmentStart();
		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			pileupBuilder.adjustWindowStart(genomicPosition);
		}

		if (isDirectional) {
			if (record.getReadNegativeStrandFlag()) {
				strand = STRAND.REVERSE;
			} else {
				strand = STRAND.FORWARD;
			}
		}

		return genomicPosition;
	}
	
	
		
	public abstract boolean hasNext();
	public abstract ParallelPileup next();
	protected abstract void advance();
	protected abstract int advance(int genomicPosition, STRAND strand);
	
	/**
	 * 
	 * @param pileupBuilderFactory
	 * @param annotatedCoordinate
	 * @param readers
	 * @param parameters
	 * @return
	 */
	protected AbstractPileupBuilder[] createPileupBuilders(final PileupBuilderFactory pileupBuilderFactory, final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readers, final Parameters parameters) {
		AbstractPileupBuilder[] pileupBuilders = new AbstractPileupBuilder[readers.length];

		for(int i = 0; i < readers.length; ++i) {
			pileupBuilders[i] = pileupBuilderFactory.newInstance(annotatedCoordinate, readers[i], parameters.getWindowSize(), parameters);
		}

		return pileupBuilders;
	}

	// TODO at least two need to be covered
	protected SAMRecord getNextValidRecord(final int targetGenomicPosition, final AbstractPileupBuilder[] pileupBuilders) {
		SAMRecord record = null;

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			record = pileupBuilder.getNextValidRecord(targetGenomicPosition);
			if (record != null) {
				return record;
			}
		}

		return record;
	}

	// TODO make this more quantitative
	protected boolean isCovered(int genomicPosition, STRAND strand, AbstractPileupBuilder[] pileupBuilders) {
		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			if (! pileupBuilder.isCovered(windowPosition, strand)) {
				return false;
			}
		}

		return true;
	}

	protected Pileup[] getPileups(int genomicPosition, STRAND strand, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Pileup[] pileups = new DefaultPileup[n];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			pileups[i] = pileupBuilders[i].getPileup(windowPosition, strand);
		}

		return pileups;
	}

	protected Counts[][] getCounts(int genomicPosition, STRAND strand, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Counts[][] counts = new Counts[n][filterCount];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuilders[i].getFilteredCounts(windowPosition, strand);
		}

		return counts;
	}

	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return true;
	}

	protected boolean adjustCurrentGenomicPosition(int targetGenomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		boolean ret = false;

		if (! pileupBuilders[0].isContainedInWindow(targetGenomicPosition)) {
			ret = adjustWindowStart(targetGenomicPosition, pileupBuilders);
		}

		return ret;
	}

	protected boolean adjustWindowStart(int genomicWindowStart, AbstractPileupBuilder[] pileupBuilders) {
		boolean ret = false;

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			ret |= pileupBuilder.adjustWindowStart(genomicWindowStart);
		}

		return ret;
	}

	protected int hasNext(int currentGenomicPosition, STRAND strand, final AbstractPileupBuilder[] pileupBuilders) {
		// within
		while (currentGenomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0].isContainedInWindow(currentGenomicPosition)) {
				if (isCovered(currentGenomicPosition, strand, pileupBuilders)) {
					return currentGenomicPosition;
				} else {
					// move along the window
					currentGenomicPosition = advance(currentGenomicPosition, strand);
				}
			} else {
				final SAMRecord record = getNextValidRecord(currentGenomicPosition, pileupBuilders);
				if (record == null) {
					return -1;
				} if (! adjustWindowStart(currentGenomicPosition, pileupBuilders)) {
					return -1;
				}
			}
		}

		return -1;
	}

	protected boolean hasNextA() {
		int newGenomicPosition = hasNext(genomicPositionA, strandA, pileupBuildersA);
		if (newGenomicPosition < 0) {
			return false;
		}

		genomicPositionA = newGenomicPosition;
		return true;
	}
	
	protected boolean hasNextB() {
		int newGenomicPosition = hasNext(genomicPositionB, strandB, pileupBuildersB);
		if (newGenomicPosition < 0) {
			return false;
		}
		
		genomicPositionB = newGenomicPosition;
		return true;
	}
	
	public AnnotatedCoordinate getAnnotatedCoordinate() {
		return coordinate;
	}

	@Override
	public void remove() {
		// not needed
	}

}