package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.builder.inverted.FRPairedEnd2InvertedPileupBuilder;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class FRPairedEnd2PileupBuilderFactory implements PileupBuilderFactory {

	public FRPairedEnd2PileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public AbstractStrandedPileupBuilder newInstance(
			final Coordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		if (sample.isInvertStrand()) {
			return new FRPairedEnd2InvertedPileupBuilder(coordinate, reader, sample, parameters);
		}
		
		return new FRPairedEnd2PileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isStranded() {
		return true;
	}

}