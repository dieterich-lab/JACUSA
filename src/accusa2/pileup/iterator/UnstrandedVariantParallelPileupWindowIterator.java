package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.Parameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.util.AnnotatedCoordinate;

public class UnstrandedVariantParallelPileupWindowIterator extends AbstractParallelPileupWindowIterator {

	public UnstrandedVariantParallelPileupWindowIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readersA, final SAMFileReader[] readersB, final Parameters parameters) {
		super(annotatedCoordinate, readersA, readersB, parameters);
	}

	protected void advance() {
		++genomicPositionA;
		++genomicPositionB;
	}

	@Override
	protected int advance(int genomicPosition, STRAND strand) {
		return ++genomicPosition;
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
	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
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
	public void remove() {
		// not needed
	}

}