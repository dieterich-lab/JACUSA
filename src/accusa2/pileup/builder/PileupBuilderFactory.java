package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.util.AnnotatedCoordinate;

public interface PileupBuilderFactory {

	public AbstractPileupBuilder newInstance(
			final AnnotatedCoordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters);

	public abstract boolean isDirected();
	
}