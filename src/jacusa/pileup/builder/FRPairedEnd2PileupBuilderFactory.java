package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class FRPairedEnd2PileupBuilderFactory implements PileupBuilderFactory {

	public FRPairedEnd2PileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public SingleEndStrandedPileupBuilder newInstance(
			final Coordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		return new FRPairedEnd2PileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isStranded() {
		return true;
	}

}