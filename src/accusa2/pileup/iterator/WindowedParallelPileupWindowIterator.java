package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.Parameters;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.util.AnnotatedCoordinate;

// TODO
public class WindowedParallelPileupWindowIterator extends ParallelPileupWindowIterator {

	// output
	protected ParallelPileup parallelPileup;

	public WindowedParallelPileupWindowIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readersA, final SAMFileReader[] readersB, final Parameters parameters) {
		super(annotatedCoordinate, readersA, readersB, parameters);
	}

	/**
	 * 
	 * @param pileupBuilderFactory
	 * @param annotatedCoordinate
	 * @param readers
	 * @param parameters
	 * @return
	 */
	@Override
	protected AbstractPileupBuilder[] createPileupBuilders(final PileupBuilderFactory pileupBuilderFactory, final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readers, final Parameters parameters) {
		AbstractPileupBuilder[] pileupBuilders = new AbstractPileupBuilder[readers.length];

		int windowSize = annotatedCoordinate.getEnd() - annotatedCoordinate.getStart() + 1;
		for(int i = 0; i < readers.length; ++i) {
			pileupBuilders[i] = pileupBuilderFactory.newInstance(annotatedCoordinate, readers[i], windowSize , parameters);
		}

		return pileupBuilders;
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

	protected int advance(int currentGenomicPosition, STRAND strand) {
		switch (strand) {
		case FORWARD:
			strand = STRAND.REVERSE;
			break;
		
		case REVERSE:
			strand = STRAND.FORWARD;
			currentGenomicPosition++;
		
		case UNKNOWN:
		default:
			currentGenomicPosition++;
			break;
		}

		return currentGenomicPosition;
	}

	protected int hasNext(final int currentGenomicPosition, STRAND strand, final AbstractPileupBuilder[] pileupBuilders) {
		// within
		while (currentGenomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0].isContainedInWindow(currentGenomicPosition)) {
				if (isCovered(currentGenomicPosition, strand, pileupBuilders)) {
					return currentGenomicPosition;
				} else {
					// move along the window
					advance(currentGenomicPosition, strand);
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
				final STRAND strandA = parallelPileup.getStrandA();
				final STRAND strandB = parallelPileup.getStrandB();

				parallelPileup.setPosition(genomicPositionA);
				
				// complement bases if one sample is unstranded and 
				// the other is stranded and maps to the opposite strand
				parallelPileup.setPileupsA(getPileups(genomicPositionA, strandA, pileupBuildersA));
				if(strandA == STRAND.UNKNOWN && strandB == STRAND.REVERSE) {
					parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
				}
				parallelPileup.setPileupsB(getPileups(genomicPositionB, strandB, pileupBuildersB));
				if(strandB == STRAND.UNKNOWN && strandA == STRAND.REVERSE) {
					parallelPileup.setPileupsB(complementPileups(parallelPileup.getPileupsB()));
				}

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

		parallelPileup.setFilterCountsA(getCounts(genomicPositionA, strandA, pileupBuildersA));
		parallelPileup.setFilterCountsB(getCounts(genomicPositionB, strandB, pileupBuildersB));

		// advance to the next position
		advance();

		return parallelPileup;
	}

	protected void advance() {
		if (strandA == STRAND.UNKNOWN) {
			if (strandB == STRAND.UNKNOWN || strandB == STRAND.REVERSE) {
				genomicPositionA++;
				genomicPositionB++;
			} else if (strandB == STRAND.FORWARD){
				strandB = STRAND.REVERSE;
			}
		}
		if (strandB == STRAND.UNKNOWN) {
			if (strandA == STRAND.REVERSE) {
				genomicPositionA++;
				genomicPositionB++;
			} else if (strandA == STRAND.FORWARD){
				strandA = STRAND.REVERSE;
			}
		}
		if (strandA == STRAND.FORWARD && strandB == STRAND.FORWARD) {
			strandA = STRAND.REVERSE;
			strandB = STRAND.REVERSE;
		} else {
			genomicPositionA++;
			genomicPositionB++;
		}
	}

}