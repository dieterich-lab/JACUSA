package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class DirectedPileupBuilderFactory implements PileupBuilderFactory {

	public DirectedPileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public DirectedPileupBuilder newInstance(
			final Coordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		return new DirectedPileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isDirected() {
		return true;
	}

}