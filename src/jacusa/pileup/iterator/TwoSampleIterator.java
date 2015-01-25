package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

public class TwoSampleIterator extends AbstractTwoSampleIterator {

	public TwoSampleIterator(
			final Coordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readers1,
			final SAMFileReader[] readers2,
			final SampleParameters sample1,
			final SampleParameters sample2,
			AbstractParameters parameters) {
		super(annotatedCoordinate, filter, readers1, readers2, sample1, sample2, parameters);
	}

	@Override
	public boolean hasNext() {
		while (hasNext1() && hasNext2()) {
			final int compare = new Integer(location1.genomicPosition).compareTo(location2.genomicPosition);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				
				if (location1.strand != STRAND.UNKNOWN || location2.strand != STRAND.UNKNOWN) {
					location2.strand = STRAND.FORWARD;
				}
				adjustCurrentGenomicPosition(location2, pileupBuilders1);
				location1.genomicPosition = location2.genomicPosition;
				location1.strand = location2.strand;
				break;

			case 0:
				if (location1.strand != STRAND.UNKNOWN && location2.strand != STRAND.UNKNOWN && location1.strand != location2.strand) {
					location1.strand = STRAND.REVERSE;
					location2.strand = STRAND.REVERSE;
					if (! isCovered(location1, pileupBuilders1) || ! isCovered(location2, pileupBuilders2)) {
						advance();
						break;
					}
				}

				parallelPileup.setContig(coordinate.getSequenceName());
				parallelPileup.setPosition(location1.genomicPosition);

				parallelPileup.setPileups1(getPileups(location1, pileupBuilders1));
				parallelPileup.setPileups2(getPileups(location2, pileupBuilders2));

				if (filter.isValid(parallelPileup)) {
					// TODO check and enhance see Location next() - duplicate...
					if (location1.strand.integer() > 0) {
						parallelPileup.setStrand(location1.strand);
					} else if (location2.strand.integer() > 0) {
						parallelPileup.setStrand(location2.strand);
					}
					return true;
				} else {
					parallelPileup.setPileups1(new Pileup[0]);
					parallelPileup.setPileups2(new Pileup[0]);

					advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				if (location1.strand != STRAND.UNKNOWN || location2.strand != STRAND.UNKNOWN) {
					location1.strand = STRAND.FORWARD;
				}
				adjustCurrentGenomicPosition(location1, pileupBuilders2);
				location2.genomicPosition = location1.genomicPosition;
				location2.strand = location1.strand;

				break;
			}
		}

		return false;
	}

	@Override
	public Location next() {
		// TODO
		Location current = new Location(location1);;
		if (location2.strand.integer() > 0) {
			current = new Location(location2);
		}
		
		// advance to the next position
		advance();

		return current;
	}

	@Override
	protected void advance() {
		if (location1.strand == STRAND.UNKNOWN) {
			if (location2.strand == STRAND.UNKNOWN || location2.strand == STRAND.REVERSE) {
				++location1.genomicPosition;
				++location2.genomicPosition;
				return;
			} else if (location2.strand == STRAND.FORWARD){
				location2.strand = STRAND.REVERSE;
				return;
			}
		}

		if (location2.strand == STRAND.UNKNOWN) {
			if (location1.strand == STRAND.REVERSE) {
				++location1.genomicPosition;
				++location2.genomicPosition;
				return;
			} else if (location1.strand == STRAND.FORWARD){
				location1.strand = STRAND.REVERSE;
				return;
			}
		}
			
		if (location1.strand == STRAND.FORWARD && location2.strand == STRAND.FORWARD) {
			location1.strand = STRAND.REVERSE;
			location2.strand = STRAND.REVERSE;
			return;
		} else {
			++location1.genomicPosition;
			++location2.genomicPosition;

			location1.strand = STRAND.FORWARD;
			location2.strand = STRAND.FORWARD;
			return;	
		}
	}

	@Override
	protected void advance(Location location) {
		switch (location.strand) {
		case FORWARD:
			location.strand = STRAND.REVERSE;
			break;

		case REVERSE:
			++location.genomicPosition;
			location.strand = STRAND.FORWARD;
			break;

		case UNKNOWN:
		default:
			++location.genomicPosition;
			break;
		}
	}

}
