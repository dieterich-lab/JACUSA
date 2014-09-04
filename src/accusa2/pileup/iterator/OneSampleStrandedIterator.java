package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.util.AnnotatedCoordinate;

public class OneSampleStrandedIterator extends AbstractOneSampleIterator {

	public OneSampleStrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readers, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, filter, readers, sample, parameters);
	}

	@Override
	protected void advance(Location location) {
		switch (location.strand) {
		case FORWARD:
			location.strand = STRAND.REVERSE;
			break;
		
		case REVERSE:
			location.strand = STRAND.FORWARD;
			location.genomicPosition++;
		
		case UNKNOWN:
		default:
			location.genomicPosition++;
			break;
		}
	}

	@Override
	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		if(location.strand == STRAND.REVERSE) {
			parallelPileup.setFilterCountsA(complementCounts(getCounts(location, pileupBuilders)));
		}

		// TODO set B
		
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
			if(location.strand == STRAND.REVERSE) {
				parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
			}

			// TODO set B

			if (filter.isValid(parallelPileup)) {
				return true;
			} else {
				advance();
			}
		}

		return false;
	}

	protected void advance() {
		if (location.strand == STRAND.FORWARD) {
			location.strand = STRAND.REVERSE;
		} else {
			location.genomicPosition++;
		}
	}

}