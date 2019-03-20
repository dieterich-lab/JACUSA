package jacusa.pileup.iterator;

import java.util.Arrays;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.DefaultPileup.STRAND;
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
			
			if (filter.isValid(parallelPileup) && parallelPileup.getPooledPileup1().getRefBase() != 'N') {
				int[] allelesIs = parallelPileup.getPooledPileup1().getAlleles();

				// pick reference base by MD
				// all other bases will be converted in pileup2 to refBaseI
				char refBase = parallelPileup.getPooledPileup1().getRefBase();
				int refBaseI = BaseConfig.BYTE_BASE2INT_BASE[refBase];
				
				if (parallelPileup.getPooledPileup1().getStrand() == STRAND.REVERSE) {
					refBase 	= BaseConfig.VALID_COMPLEMENTED[refBaseI];
					refBaseI 	= BaseConfig.BYTE_BASE2INT_BASE[refBase];	
				}
				
				int [] tmpVariantBasesIs = new int[allelesIs.length];
				Arrays.fill(tmpVariantBasesIs, -1);
				int i = 0;
				for (int j = 0; j < allelesIs.length; ++j) {
					if (allelesIs[j] != refBaseI) {
						tmpVariantBasesIs[i] = allelesIs[j];
						++i;
					}
				}
				int[] variantBasesIs = Arrays.copyOfRange(tmpVariantBasesIs, 0, i);
				final Pileup[] pileups2 = DefaultPileup.flat(parallelPileup.getPileups1(), variantBasesIs, refBaseI);
				parallelPileup.setPileups2(pileups2);
				
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
	
}
