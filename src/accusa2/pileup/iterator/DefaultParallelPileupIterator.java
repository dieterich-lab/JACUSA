package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public class DefaultParallelPileupIterator extends VariantParallelPileupIterator {

	public DefaultParallelPileupIterator(AnnotatedCoordinate coordinate, SAMFileReader[] readers1, SAMFileReader[] readers2, Parameters parameters) {
		super(coordinate, readers1, readers2, parameters);
	}

	@Override
	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 0;
	}

}
