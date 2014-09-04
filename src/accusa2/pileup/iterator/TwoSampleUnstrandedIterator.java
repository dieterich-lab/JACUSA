package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleUnstrandedIterator extends AbstractTwoSampleIterator {

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
			final int compare = new Integer(locationA.genomicPosition).compareTo(locationB.genomicPosition);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(locationB.genomicPosition, pileupBuildersA);
				locationA.genomicPosition = locationB.genomicPosition;
				break;

			case 0:
				parallelPileup.setPosition(locationA.genomicPosition);
				
				// complement bases if one sample is unstranded and 
				// the other is stranded and maps to the opposite strand
				parallelPileup.setPileupsA(getPileups(locationA, pileupBuildersA));
				parallelPileup.setPileupsB(getPileups(locationB, pileupBuildersB));

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
				adjustCurrentGenomicPosition(locationA.genomicPosition, pileupBuildersB);
				locationB.genomicPosition = locationA.genomicPosition;
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

		if (filterconfig.hasFiters()) {
			parallelPileup.setFilterCountsA(getCounts(locationA, pileupBuildersA));
			parallelPileup.setFilterCountsB(getCounts(locationB, pileupBuildersB));
		}

		// advance to the next position
		advance();

		return parallelPileup;
	}

	@Override
	protected void advance() {
		locationA.genomicPosition++;
		locationB.genomicPosition++;
	}

	@Override
	protected void advance(Location location) {
		location.genomicPosition++;
	}

}