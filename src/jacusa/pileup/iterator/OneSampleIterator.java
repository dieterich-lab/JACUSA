package jacusa.pileup.iterator;

import java.util.HashSet;
import java.util.Set;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.DefaultPileup;
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

	/*
	@Override
	public boolean hasNext() {
		while (hasNextA()) {
			parallelPileup.setContig(coordinate.getSequenceName());
			parallelPileup.setStart(location.genomicPosition);

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
	*/

	protected void advance() {
		if (location.strand == STRAND.FORWARD) {
			location.strand = STRAND.REVERSE;
		} else {
			location.genomicPosition++;
		}
	}

	@Override
	public boolean hasNext() {
		Location location = locationAdvancer.getLocation1();

		while (hasNextA()) {
			if (! locationAdvancer.isValidStrand()) {
				location.strand = STRAND.REVERSE;
				if (! isCovered(location, pileupBuilders)) {
					locationAdvancer.advance();
					break;
				}
			}
			location = locationAdvancer.getLocation();
			
			parallelPileup.setContig(coordinate.getSequenceName());
			parallelPileup.setStart(location.genomicPosition);
			parallelPileup.setEnd(parallelPileup.getStart());

			parallelPileup.setStrand(location.strand);
			parallelPileup.setPileups1(getPileups(location, pileupBuilders));
			parallelPileup.setPileups2(new Pileup[0]);

			if (filter.isValid(parallelPileup)) {
				int commonBaseI = 0;
				int commonBaseCount = 0;
				
				int[] allelesIs = parallelPileup.getPooledPileup1().getAlleles();
				for (int baseI : allelesIs) {
					int count = parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI);
					if (count > commonBaseCount) {
						commonBaseCount = count;
						commonBaseI = baseI;
					}
				}
				int [] variantBasesIs = new int[allelesIs.length - 1];
				int i = 0;
				for (int j = 0; j < allelesIs.length; ++j) {
					if (allelesIs[j] != commonBaseI) {
						variantBasesIs[i] = allelesIs[j];
						++i;
					}
				}

				parallelPileup.setPileups2(DefaultPileup.flat(parallelPileup.getPileups2(), variantBasesIs, commonBaseI));
				
				return true;
			} else {
				// reset
				parallelPileup.setPileups1(new Pileup[0]);
				parallelPileup.setPileups2(new Pileup[0]);

				locationAdvancer.advance();
			}
			break;
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
		alleles.addAll(getAlleles(location, pileupBuilders));

		return alleles.size();
	}
	
	@Override
	public int getAlleleCount1(Location location) {
		return getAlleleCount(location, pileupBuilders);
	}
	
	@Override
	public int getAlleleCount2(Location location) {
		return 0;
	}
	
}
