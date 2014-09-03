package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public abstract class OneSampleIterator extends AbstractParallelPileupWindowIterator {

	protected SampleParameters sample;

	protected int genomicPositionA;
	protected STRAND strandA;
	protected final AbstractPileupBuilder[] pileupBuildersA;	

	// output
	protected ParallelPileup parallelPileup;

	public OneSampleIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final SAMFileReader[] readers,
			final SampleParameters sample, 
			AbstractParameters parameters) {
		super(annotatedCoordinate, parameters);

		this.sample = sample;
		pileupBuildersA = createPileupBuilders(
				sample.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readers,
				sample,
				parameters);
		genomicPositionA = init(strandA, sample.getPileupBuilderFactory().isDirected(), pileupBuildersA);
		strandA = STRAND.UNKNOWN;
		
		parallelPileup = new DefaultParallelPileup(pileupBuildersA.length, 0);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());

		
	}

	protected boolean hasNextA() {
		int newGenomicPosition = hasNext(genomicPositionA, strandA, pileupBuildersA);
		if (newGenomicPosition < 0) {
			return false;
		}

		genomicPositionA = newGenomicPosition;
		return true;
	}
	
}