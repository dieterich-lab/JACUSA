package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.Pileup;
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
		final Location location1 = locationAdvance.getLocation1();
		final Location location2 = locationAdvance.getLocation2();

		while (hasNext1() && hasNext2()) {
			final int compare = new Integer(location1.genomicPosition).compareTo(location2.genomicPosition);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific position
				locationAdvance.setLocation1(location2);
				adjustCurrentGenomicPosition(location2, pileupBuilders1);
				
				break;

			case 0:
				if (! locationAdvance.isValidStrand()) {
					location1.strand = STRAND.REVERSE;
					location2.strand = STRAND.REVERSE;
					if (! isCovered(location1, pileupBuilders1) || ! isCovered(location2, pileupBuilders2)) {
						locationAdvance.advance();
						break;
					}
				}
				final Location location = locationAdvance.getLocation();
				
				parallelPileup.setContig(coordinate.getSequenceName());
				parallelPileup.setPosition(location.genomicPosition);

				parallelPileup.setStrand(location.strand);
				parallelPileup.setPileups1(getPileups(location, pileupBuilders1));
				parallelPileup.setPileups2(getPileups(location, pileupBuilders2));

				if (filter.isValid(parallelPileup)) {
					return true;
				} else {
					parallelPileup.setPileups1(new Pileup[0]);
					parallelPileup.setPileups2(new Pileup[0]);

					locationAdvance.advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific position
				locationAdvance.setLocation2(location1);
				adjustCurrentGenomicPosition(location1, pileupBuilders2);
				
				break;
			}
		}

		return false;
	}

	@Override
	public Location next() {
		Location current = new Location(locationAdvance.getLocation());;

		// advance to the next position
		locationAdvance.advance();

		return current;
	}

}
