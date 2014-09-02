package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.Parameters;
import accusa2.util.AnnotatedCoordinate;

public class UndirectedPileupBuilderFactory implements PileupBuilderFactory {

	public UndirectedPileupBuilderFactory() {
		// nothing to be done
	}

	@Override
	public UndirectedPileupBuilder newInstance(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final int windowSize, final Parameters parameters) {
		return new UndirectedPileupBuilder(coordinate, reader, windowSize, parameters);
	}

	@Override
	public boolean isDirected() {
		return false;
	}
	
}