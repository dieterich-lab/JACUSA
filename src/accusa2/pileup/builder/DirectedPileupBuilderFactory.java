package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.util.AnnotatedCoordinate;

public class DirectedPileupBuilderFactory implements PileupBuilderFactory {

	public DirectedPileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public DirectedPileupBuilder newInstance(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final int windowSize, final Parameters parameters) {
		return new DirectedPileupBuilder(coordinate, reader, windowSize, parameters);
	}

	@Override
	public boolean isDirected() {
		return true;
	}
	
}