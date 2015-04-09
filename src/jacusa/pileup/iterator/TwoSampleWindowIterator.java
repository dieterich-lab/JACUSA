package jacusa.pileup.iterator;

import java.util.ArrayList;
import java.util.List;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
// import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

// Initial version

public class TwoSampleWindowIterator extends AbstractTwoSampleIterator {

	private List<Pileup> pileups1;
	private List<Pileup> pileups2;

	public TwoSampleWindowIterator(
			final Coordinate coordinate,
			final Variant filter,
			final SAMFileReader[] readers1,
			final SAMFileReader[] readers2,
			final SampleParameters sample1,
			final SampleParameters sample2,
			AbstractParameters parameters) {
		super(coordinate, filter, readers1, readers2, sample1, sample2, parameters);
		
		int n = 50;
		pileups1 = new ArrayList<Pileup>(n);
		pileups2 = new ArrayList<Pileup>(n);
	}

	@Override
	public boolean hasNext() {
		final Location location1 = locationAdvancer.getLocation1();
		final Location location2 = locationAdvancer.getLocation2();

		final Location location = locationAdvancer.getLocation();
		pileups1.clear();
		pileups2.clear();
		int n = 0;

		// set coordinates for window
		parallelPileup.setContig(location.contig);
		parallelPileup.setStrand(location.strand);

		while (hasNext1() && hasNext2()) {
			final int compare = new Integer(location1.genomicPosition).compareTo(location2.genomicPosition);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific position
				locationAdvancer.setLocation1(location2);
				adjustCurrentGenomicPosition(location2, pileupBuilders1);
				break;

			case 0:
				/*
				if (! locationAdvancer.isValidStrand()) {
					location1.strand = STRAND.REVERSE;
					location2.strand = STRAND.REVERSE;
					if (! isCovered(location1, pileupBuilders1) || ! isCovered(location2, pileupBuilders2)) {
						locationAdvancer.advance();
						break;
					}
				}
				*/
				// check that we have a variant position
				parallelPileup.setPileups1(getPileups(location, pileupBuilders1));
				parallelPileup.setPileups2(getPileups(location, pileupBuilders2));
				if (filter.isValid(parallelPileup)) {
					pileups1.add(getPileups(location, pileupBuilders1)[0]);
					pileups2.add(getPileups(location, pileupBuilders2)[0]);
					++n;
				} else {
					// reset
					parallelPileup.setPileups1(new Pileup[0]);
					parallelPileup.setPileups2(new Pileup[0]);
				}
				locationAdvancer.advance();
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific position
				locationAdvancer.setLocation2(location1);
				adjustCurrentGenomicPosition(location1, pileupBuilders2);
				
				break;
			}
		}

		if (n == 0) {
			return false;
		}

		// set start
		parallelPileup.setStart(pileups1.get(0).getPosition() - 1);
		parallelPileup.setEnd(pileups1.get(n - 1).getPosition());
		// set end
		parallelPileup.setPileups1(pileups1.toArray(new Pileup[n]));
		parallelPileup.setPileups2(pileups2.toArray(new Pileup[n]));

		return true;
	}

	@Override
	public Location next() {
		Location current = new Location(locationAdvancer.getLocation());;

		// advance to the next position
		locationAdvancer.advance();

		return current;
	}
	
}