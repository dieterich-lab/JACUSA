package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.Parameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.util.AnnotatedCoordinate;

public class UnstrandedParallelPileupWindowIterator implements ParallelPileupIterator {

	protected int genomicPositionA;
	protected int genomicPositionB;

	protected final AnnotatedCoordinate coordinate;
	protected final Parameters parameters;

	// pileupBuilders
	protected final AbstractPileupBuilder[] pileupBuildersA;
	protected final AbstractPileupBuilder[] pileupBuildersB;

	protected int filterCount;

	// output
	protected ParallelPileup parallelPileup;

	public UnstrandedParallelPileupWindowIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readersA, final SAMFileReader[] readersB, final Parameters parameters) {
		this.coordinate = annotatedCoordinate;
		this.parameters = parameters;

		pileupBuildersA = createPileupBuilders(parameters.getPileupBuilderFactoryA(), annotatedCoordinate, readersA, parameters);
		pileupBuildersB = createPileupBuilders(parameters.getPileupBuilderFactoryB(), annotatedCoordinate, readersB, parameters);

		filterCount = parameters.getFilterConfig().getFactories().size();

		// init
		parallelPileup = new DefaultParallelPileup(pileupBuildersA.length, pileupBuildersB.length);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());
		
		genomicPositionA = init(pileupBuildersA);
		genomicPositionB = init(pileupBuildersB);
	}

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

	protected int init(final AbstractPileupBuilder[] pileupBuilders) {
		final SAMRecord record = getNextValidRecord(coordinate.getStart(), pileupBuilders);
		if (record == null) {
			return -1;
		}

		final int genomicPosition = record.getAlignmentStart();
		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			pileupBuilder.adjustWindowStart(genomicPosition);
		}

		return genomicPosition;
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

	protected boolean hasNextA() {
		int newGenomicPosition = hasNext(genomicPositionA, pileupBuildersA);
		if (newGenomicPosition < 0) {
			return false;
		}

		genomicPositionA = newGenomicPosition;
		return true;
	}

	protected boolean hasNextB() {
		int newGenomicPosition = hasNext(genomicPositionB, pileupBuildersB);
		if (newGenomicPosition < 0) {
			return false;
		}
		
		genomicPositionB = newGenomicPosition;
		return true;
	}

	protected int hasNext(int currentGenomicPosition, final AbstractPileupBuilder[] pileupBuilders) {
		// within
		while (currentGenomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0].isContainedInWindow(currentGenomicPosition)) {
				if (isCovered(currentGenomicPosition, pileupBuilders)) {
					return currentGenomicPosition;
				} else {
					// move along the window
					currentGenomicPosition++;
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

	// TODO make this more quantitative
	protected boolean isCovered(int genomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			if (! pileupBuilder.isCovered(windowPosition, STRAND.UNKNOWN)) {
				return false;
			}
		}

		return true;
	}

	protected Pileup[] getPileups(int genomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Pileup[] pileups = new DefaultPileup[n];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			pileups[i] = pileupBuilders[i].getPileup(windowPosition, STRAND.UNKNOWN);
		}

		return pileups;
	}

	protected Counts[][] getCounts(int genomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Counts[][] counts = new Counts[n][filterCount];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuilders[i].getFilteredCounts(windowPosition, STRAND.UNKNOWN);
		}

		return counts;
	}

	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 0;
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

	protected Pileup[] complementPileups(Pileup[] pileups) {
		Pileup[] complementedPileups = new DefaultPileup[pileups.length];

		for (int i = 0; i < pileups.length; ++i) {
			complementedPileups[i] = pileups[i].complement();
		}

		return complementedPileups;
	}

	public boolean hasNext() {
		while (hasNextA() && hasNextB()) {
			final int compare = new Integer(genomicPositionA).compareTo(genomicPositionB);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(genomicPositionB, pileupBuildersA);
				genomicPositionA = genomicPositionB;
				break;

			case 0:
				parallelPileup.setPosition(genomicPositionA);
				
				// complement bases if one sample is unstranded and 
				// the other is stranded and maps to the opposite strand
				parallelPileup.setPileupsA(getPileups(genomicPositionA, pileupBuildersA));
				parallelPileup.setPileupsB(getPileups(genomicPositionB, pileupBuildersB));

				final boolean isVariant = isVariant(parallelPileup);
				if (isVariant) {
					return true;
				} else {
					advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(genomicPositionA, pileupBuildersB);
				genomicPositionB = genomicPositionA;
				break;
			}
		}

		return false;
	}

	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		parallelPileup.setFilterCountsA(getCounts(genomicPositionA, pileupBuildersA));
		parallelPileup.setFilterCountsB(getCounts(genomicPositionB, pileupBuildersB));

		// advance to the next position
		advance();

		return parallelPileup;
	}

	protected void advance() {
		genomicPositionA++;
		genomicPositionB++;
	}

	public AnnotatedCoordinate getAnnotatedCoordinate() {
		return coordinate;
	}

	@Override
	public void remove() {
		// not needed
	}

}