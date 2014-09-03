package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.util.AnnotatedCoordinate;

public class OneSampleStrandedIterator extends AbstractOneSampleIterator {

	public OneSampleStrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader[] readers, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, readers, sample, parameters);
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

	@Override
	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		if(strandA == STRAND.REVERSE) {
			parallelPileup.setFilterCountsA(complementCounts(getCounts(genomicPositionA, strandA, pileupBuildersA)));
		}

		// TODO set B
		
		// advance to the next position
		advance();

		return parallelPileup;
	}
	
	@Override
	public boolean hasNext() {
		while (hasNextA()) {
			final STRAND strandA = parallelPileup.getStrandA();

			parallelPileup.setPosition(genomicPositionA);

			// complement bases if one sample is unstranded and 
			// the other is stranded and maps to the opposite strand
			parallelPileup.setPileupsA(getPileups(genomicPositionA, strandA, pileupBuildersA));
			if(strandA == STRAND.REVERSE) {
				parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
			}

			// TODO set B

			final boolean isVariant = isVariant(parallelPileup);
			if (isVariant) {
				return true;
			} else {
				advance();
			}
		}

		return false;
	}

	protected void advance() {
		if (strandA == STRAND.FORWARD) {
			strandA = STRAND.REVERSE;
		} else {
			genomicPositionA++;
		}
	}

}