package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleStrandedIterator extends AbstractTwoSampleIterator {

	public TwoSampleStrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final SAMFileReader[] readersA,
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			AbstractParameters parameters) {
		super(annotatedCoordinate, readersA, readersB, sampleA, sampleB, parameters);
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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
	
}