package jacusa.pileup.iterator;


import java.util.HashSet;
import java.util.Set;

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
		final Location location1 = locationAdvancer.getLocation1();
		final Location location2 = locationAdvancer.getLocation2();

		while (hasNext1() && hasNext2()) {
			final int compare = new Integer(location1.genomicPosition).compareTo(location2.genomicPosition);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific position
				locationAdvancer.setLocation1(location2);
				adjustCurrentGenomicPosition(location2, pileupBuilders1);
				
				break;

			case 0:
				if (! locationAdvancer.isValidStrand()) {
					location1.strand = STRAND.REVERSE;
					location2.strand = STRAND.REVERSE;
					if (! isCovered(location1, pileupBuilders1) || ! isCovered(location2, pileupBuilders2)) {
						locationAdvancer.advance();
						break;
					}
				}
				final Location location = locationAdvancer.getLocation();
				
				parallelPileup.setContig(coordinate.getSequenceName());
				parallelPileup.setStart(location.genomicPosition);
				parallelPileup.setEnd(parallelPileup.getStart());

				parallelPileup.setStrand(location.strand);
				parallelPileup.setPileups1(getPileups(location, pileupBuilders1));
				parallelPileup.setPileups2(getPileups(location, pileupBuilders2));

				if (filter.isValid(parallelPileup)) {
					return true;
				} else {
					// reset
					parallelPileup.setPileups1(new Pileup[0]);
					parallelPileup.setPileups2(new Pileup[0]);

					locationAdvancer.advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific position
				locationAdvancer.setLocation2(location1);
				adjustCurrentGenomicPosition(location1, pileupBuilders2);

				break;
			}
		}

		return false;
	}

	@Override
	public Location next() {
		Location current = new Location(locationAdvancer.getLocation());;

		// advance to the next position
		locationAdvancer.advance();

		return current;
	}

	@Override
	public int getAlleleCount(Location location) {
		Set<Integer> alleles = new HashSet<Integer>(4); 
		alleles.addAll(getAlleles(location, pileupBuilders1));
		alleles.addAll(getAlleles(location, pileupBuilders2));

		return alleles.size();
	}
	
	@Override
	public int getAlleleCount1(Location location) {
		return getAlleleCount(location, pileupBuilders1);
	}
	
	@Override
	public int getAlleleCount2(Location location) {
		return getAlleleCount(location, pileupBuilders2);
	}
	
}
