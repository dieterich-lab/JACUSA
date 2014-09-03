package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public abstract class TwoSampleIterator extends AbstractParallelPileupWindowIterator {

	// sample A
	protected SampleParameters sampleA;
	protected int genomicPositionA;
	protected STRAND strandA;
	protected final AbstractPileupBuilder[] pileupBuildersA;	

	// sample B
	protected SampleParameters sampleB;
	protected int genomicPositionB;
	protected STRAND strandB;
	protected final AbstractPileupBuilder[] pileupBuildersB;
	
	// output
	protected ParallelPileup parallelPileup;
	
	public TwoSampleIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final SAMFileReader[] readersA,
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			AbstractParameters parameters) {
		super(annotatedCoordinate, parameters);

		this.sampleA = sampleA;
		pileupBuildersA = createPileupBuilders(
				sampleA.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readersA,
				sampleA,
				parameters);
		genomicPositionA = init(strandA, sampleA.getPileupBuilderFactory().isDirected(), pileupBuildersA);
		strandA = STRAND.UNKNOWN;
		
		this.sampleB = sampleB;
		pileupBuildersB = createPileupBuilders(
				sampleB.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readersB,
				sampleB,
				parameters);
		genomicPositionB = init(strandB, sampleB.getPileupBuilderFactory().isDirected(), pileupBuildersB);
		strandB = STRAND.UNKNOWN;
		
		parallelPileup = new DefaultParallelPileup(pileupBuildersA.length, pileupBuildersB.length);
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
	
	protected boolean hasNextB() {
		int newGenomicPosition = hasNext(genomicPositionB, strandB, pileupBuildersB);
		if (newGenomicPosition < 0) {
			return false;
		}

		genomicPositionB = newGenomicPosition;
		return true;
	}
	
}