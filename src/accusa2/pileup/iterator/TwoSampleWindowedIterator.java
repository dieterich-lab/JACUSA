package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.WindowedPileup;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.util.AnnotatedCoordinate;

// TODO implement
public class TwoSampleWindowedIterator extends AbstractTwoSampleIterator {

	private BaseConfig baseConfig;
	
	public TwoSampleWindowedIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final Variant filter,
			final SAMFileReader[] readersA, 
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, filter, readersA, readersB, sampleA, sampleB, parameters);
		this.baseConfig = parameters.getBaseConfig();
	}

	protected boolean hasNextA() {
		return true;
	}

	protected boolean hasNextB() {
		return true;
	}

	protected void advance(Location location) {
		location.genomicPosition++;
	}

	protected boolean hasNext(Location location, final AbstractPileupBuilder[] pileupBuilders) {
		return false; // TODO check what to do
	}

	@Override
	protected WindowedPileup[] getPileups(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int replicates = pileupBuilders.length;
		
		WindowedPileup[] pileups = new WindowedPileup[replicates];
		for(int replicate = 0; replicate < replicates; ++replicate) {
			pileups[replicate] = new WindowedPileup(baseConfig);
		}

		for (; location.genomicPosition < getAnnotatedCoordinate().getEnd(); ++location.genomicPosition) {
			int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(location.genomicPosition);
			for(int replicate = 0; replicate < replicates; ++replicate) {
				pileups[replicate].addPileup(pileupBuilders[replicate].getPileup(windowPosition, location.strand));
			}
		}

		return pileups;
	}

	// FIXME currently no filtering
	@Override
	protected Counts[][] getCounts(Location location, AbstractPileupBuilder[] pileupBuilders) {
		/*
		int n = pileupBuilders.length;
		Counts[][] counts = new Counts[n][filterCount];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuilders[i].getFilteredCounts(windowPosition, strand);
		}
		 */
		return null;
	}

	public boolean hasNext() {
		return false;
	}

	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		// FIXME currently no filtering
		// parallelPileup.setFilterCountsA(getCounts(genomicPositionA, strandA, pileupBuildersA));
		// parallelPileup.setFilterCountsB(getCounts(genomicPositionB, strandB, pileupBuildersB));

		// advance to the next position
		advance();

		return parallelPileup;
	}

	protected void advance() {
		locationA.genomicPosition++;
		locationB.genomicPosition++;
	}

}