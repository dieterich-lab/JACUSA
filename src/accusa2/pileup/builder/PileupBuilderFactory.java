package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.FilterConfig;
import accusa2.pileup.BaseConfig;
import accusa2.util.AnnotatedCoordinate;

public interface PileupBuilderFactory {

	public AbstractPileupBuilder newInstance(
			final AnnotatedCoordinate coordinate, 
			final SAMFileReader reader, 
			final int windowSize, 
			final BaseConfig baseConfig,
			final FilterConfig filterConfig,
			final SampleParameters parameters);

	public abstract boolean isDirected();
	
}