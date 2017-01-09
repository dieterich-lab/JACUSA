package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.builder.inverted.FRPairedEnd1InvertedPileupBuilder;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class FRPairedEnd1PileupBuilderFactory implements PileupBuilderFactory {

	public FRPairedEnd1PileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public AbstractStrandedPileupBuilder newInstance(
			final Coordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		if (sample.isInvertStrand()) {
			return new FRPairedEnd1InvertedPileupBuilder(coordinate, reader, sample, parameters);
		}
		return new FRPairedEnd1PileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isStranded() {
		return true;
	}

}