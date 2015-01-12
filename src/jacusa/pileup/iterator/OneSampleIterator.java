package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.Pileup;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

public class OneSampleIterator extends AbstractOneSampleIterator {

	public OneSampleIterator(
			final Coordinate annotatedCoordinate,
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
			break;
		
		case UNKNOWN:
		default:
			location.genomicPosition++;
			break;
		}
	}

	@Override
	public Location next() {
		Location current = new Location(location);

		// advance to the next position
		advance();

		return current;
	}

	@Override
	public boolean hasNext() {
		while (hasNextA()) {
			parallelPileup.setContig(coordinate.getSequenceName());
			parallelPileup.setPosition(location.genomicPosition);

			parallelPileup.setPileups1(parallelPileup.getPileups1());
			int baseI = getHomomorphBaseI(parallelPileup.getPooledPileup1());
			Pileup[] homoMorph = removeBase(baseI, parallelPileup.getPileups1());
			parallelPileup.setPileups2(homoMorph);

			if (filter.isValid(parallelPileup)) {
				return true;
			} else {
				parallelPileup.setPileups1(new Pileup[0]);
				parallelPileup.setPileups2(new Pileup[0]);

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
