package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.util.AnnotatedCoordinate;

public class OneSampleUnstrandedIterator extends AbstractOneSampleIterator {

	public OneSampleUnstrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readers,
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, filter, readers, sample, parameters);
	}

	protected void advance() {
		location.genomicPosition++;
	}

	@Override
	protected void advance(Location location) {
		location.genomicPosition++;
	}

	@Override
	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		parallelPileup.setFilterCountsA(getCounts(location, pileupBuilders));

		// advance to the next position
		advance();

		return parallelPileup;
	}
	
	@Override
	public boolean hasNext() {
		while (hasNextA()) {
			parallelPileup.setContig(coordinate.getSequenceName());
			parallelPileup.setPosition(location.genomicPosition);
				
			// complement bases if one sample is unstranded and 
			// the other is stranded and maps to the opposite strand
			parallelPileup.setPileupsA(getPileups(location, pileupBuilders));
			// set B

			if (filter.isValid(parallelPileup)) {
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