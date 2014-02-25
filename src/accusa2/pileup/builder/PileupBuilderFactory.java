package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.util.AnnotatedCoordinate;

public interface PileupBuilderFactory {

	public AbstractPileupBuilder newInstance(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final Parameters parameters);

	public boolean isDirected();
	
}
