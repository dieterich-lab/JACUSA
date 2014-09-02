package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.FilterConfig;
import accusa2.pileup.BaseConfig;
import accusa2.util.AnnotatedCoordinate;

public class DirectedPileupBuilderFactory implements PileupBuilderFactory {

	public DirectedPileupBuilderFactory() {
		// Nothing to be done
	}

	@Override
	public DirectedPileupBuilder newInstance(
			final AnnotatedCoordinate coordinate, 
			final SAMFileReader reader, 
			final int windowSize, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig, 
			final SampleParameters parameters) {
		return new DirectedPileupBuilder(coordinate, reader, windowSize, baseConfig, filterConfig, parameters);
	}

	@Override
	public boolean isDirected() {
		return true;
	}
	
}