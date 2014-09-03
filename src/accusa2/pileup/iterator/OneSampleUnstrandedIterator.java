package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.util.AnnotatedCoordinate;

public class OneSampleUnstrandedIterator extends AbstractOneSampleIterator {

	public OneSampleUnstrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader[] readers,
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, readers, sample, parameters);
	}

	protected void advance() {
		++genomicPositionA;
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

		parallelPileup.setFilterCountsA(getCounts(genomicPositionA, strandA, pileupBuildersA));

		// advance to the next position
		advance();

		return parallelPileup;
	}
	
	@Override
	public boolean hasNext() {
		while (hasNextA()) {
			parallelPileup.setPosition(genomicPositionA);
				
			// complement bases if one sample is unstranded and 
			// the other is stranded and maps to the opposite strand
			parallelPileup.setPileupsA(getPileups(genomicPositionA, strandA, pileupBuildersA));
			// set B
			final boolean isVariant = isVariant(parallelPileup);
			if (isVariant) {
				return true;
			} else {
				advance();
			}
		}

		return false;
	}
	
	@Override
	public void remove() {
		// not needed
	}

}