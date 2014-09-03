package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleStrandedIterator extends AbstractTwoSampleIterator {

	public TwoSampleStrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final SAMFileReader[] readersA,
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			AbstractParameters parameters) {
		super(annotatedCoordinate, readersA, readersB, sampleA, sampleB, parameters);
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ParallelPileup next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void advance() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int advance(int genomicPosition, STRAND strand) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}