package jacusa.pileup.iterator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.BaseConfig;
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

	@Override
	public boolean hasNext() {
		while (hasNext1()) {
			Location location = locationAdvancer.getLocation();
			
			parallelPileup.setContig(coordinate.getSequenceName());
			parallelPileup.setStart(location.genomicPosition);
			parallelPileup.setEnd(parallelPileup.getStart());

			parallelPileup.setStrand(location.strand);
			parallelPileup.setPileups1(getPileups(location, pileupBuilders));
			parallelPileup.setPileups2(new Pileup[0]);

			int commonBaseI = -1;
			boolean noReference = false;
			if (parallelPileup.getPooledPileup1().getRefBase() != 'N') {
				char refBase = parallelPileup.getPooledPileup1().getRefBase();
				commonBaseI = BaseConfig.BYTE_BASE2INT_BASE[(byte)refBase];
				if (parallelPileup.getPooledPileup1().getCounts().getBaseCount(commonBaseI) == 0) {
					noReference = true;
				}
			}
			
			if (filter.isValid(parallelPileup) || noReference) {
				int[] allelesIs = parallelPileup.getPooledPileup1().getAlleles();
				if (commonBaseI == -1) {
					int commonBaseCount = 0;

					for (int baseI : allelesIs) {
						int count = parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI);
						if (count > commonBaseCount) {
							commonBaseCount = count;
							commonBaseI = baseI;
						}
					}
				}
				int [] tmpVariantBasesIs = new int[allelesIs.length];
				int i = 0;
				for (int j = 0; j < allelesIs.length; ++j) {
					if (allelesIs[j] != commonBaseI) {
						tmpVariantBasesIs[i] = allelesIs[j];
						++i;
					}
				}
				int[] variantBasesIs = Arrays.copyOf(tmpVariantBasesIs, i);
				parallelPileup.setPileups2(DefaultPileup.flat(parallelPileup.getPileups1(), variantBasesIs, commonBaseI));
				
				return true;
			} else {
				// reset
				parallelPileup.setPileups1(new Pileup[0]);
				parallelPileup.setPileups2(new Pileup[0]);

				locationAdvancer.advance();
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
