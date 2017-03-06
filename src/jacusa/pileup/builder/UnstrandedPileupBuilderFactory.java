package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class UnstrandedPileupBuilderFactory implements PileupBuilderFactory {

	public UnstrandedPileupBuilderFactory() {
		// nothing to be done
	}

	@Override
	public AbstractPileupBuilder newInstance(
			final Coordinate coordinate,
			final SAMFileReader reader, 
			final SampleParameters sample, 
			final AbstractParameters parameters) {
		return new UnstrandedPileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isStranded() {
		return false;
	}

}