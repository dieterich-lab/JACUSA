package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.util.AnnotatedCoordinate;

public class UndirectedPileupBuilderFactory implements PileupBuilderFactory {

	public UndirectedPileupBuilderFactory() {
		// nothing to be done
	}

	@Override
	public AbstractPileupBuilder newInstance(
			final AnnotatedCoordinate coordinate,
			final SAMFileReader reader, 
			final SampleParameters sample, 
			final AbstractParameters parameters) {
		return new UndirectedPileupBuilder(coordinate, reader, sample, parameters);
	}

	@Override
	public boolean isDirected() {
		return false;
	}

}