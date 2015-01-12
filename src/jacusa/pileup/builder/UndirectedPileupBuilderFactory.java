package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class UndirectedPileupBuilderFactory implements PileupBuilderFactory {

	public UndirectedPileupBuilderFactory() {
		// nothing to be done
	}

	@Override
	public AbstractPileupBuilder newInstance(
			final Coordinate coordinate,
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