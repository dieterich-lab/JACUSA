package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.FilterConfig;
import accusa2.pileup.BaseConfig;
import accusa2.util.AnnotatedCoordinate;

public class UndirectedPileupBuilderFactory implements PileupBuilderFactory {

	public UndirectedPileupBuilderFactory() {
		// nothing to be done
	}

	@Override
	public AbstractPileupBuilder newInstance(
			final AnnotatedCoordinate coordinate,
			final SAMFileReader reader, 
			final int windowSize, 
			final BaseConfig baseConfig,
			final FilterConfig filterConfig, 
			final SampleParameters parameters) {
		return new UndirectedPileupBuilder(coordinate, reader, windowSize, baseConfig, filterConfig, parameters);
	}

	@Override
	public boolean isDirected() {
		return false;
	}

}