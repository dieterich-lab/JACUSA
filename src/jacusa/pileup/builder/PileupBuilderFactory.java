package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public interface PileupBuilderFactory {

	public AbstractPileupBuilder newInstance(
			final Coordinate coordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters);

	public abstract boolean isStranded();
	
}