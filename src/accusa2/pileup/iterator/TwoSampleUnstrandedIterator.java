package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleUnstrandedIterator extends TwoSampleIterator {

	public TwoSampleUnstrandedIterator(
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
				parallelPileup.setPosition(genomicPositionA);
				
				// complement bases if one sample is unstranded and 
				// the other is stranded and maps to the opposite strand
				parallelPileup.setPileupsA(getPileups(genomicPositionA, strandA, pileupBuildersA));
				parallelPileup.setPileupsB(getPileups(genomicPositionB, strandB, pileupBuildersB));

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

		parallelPileup.setFilterCountsA(getCounts(genomicPositionA, strandB, pileupBuildersA));
		parallelPileup.setFilterCountsB(getCounts(genomicPositionB, strandB, pileupBuildersB));

		// advance to the next position
		advance();

		return parallelPileup;
	}

	@Override
	protected void advance() {
		++genomicPositionA;
		++genomicPositionB;
	}

	@Override
	protected int advance(int genomicPosition, STRAND strand) {
		return ++genomicPosition;
	}

}