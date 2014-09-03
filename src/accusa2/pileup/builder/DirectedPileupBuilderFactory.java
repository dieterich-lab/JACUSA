package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.util.AnnotatedCoordinate;

public class DirectedPileupBuilderFactory implements PileupBuilderFactory {

	public DirectedPileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public DirectedPileupBuilder newInstance(
			final AnnotatedCoordinate coordinate, 
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