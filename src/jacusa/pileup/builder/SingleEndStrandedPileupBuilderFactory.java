package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class SingleEndStrandedPileupBuilderFactory implements PileupBuilderFactory {

	public SingleEndStrandedPileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public SingleEndStrandedPileupBuilder newInstance(
			final Coordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		return new SingleEndStrandedPileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isStranded() {
		return true;
	}

}