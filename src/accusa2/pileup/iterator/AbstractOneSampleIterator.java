package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractOneSampleIterator extends AbstractWindowIterator {

	protected SampleParameters sample;

	protected Location location;
	protected final AbstractPileupBuilder[] pileupBuilders;	

	// output
	protected ParallelPileup parallelPileup;

	public AbstractOneSampleIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final SAMFileReader[] readers,
			final SampleParameters sample, 
			AbstractParameters parameters) {
		super(annotatedCoordinate, parameters);

		location = new Location(-1, STRAND.UNKNOWN);
		this.sample = sample;
		pileupBuilders = createPileupBuilders(
				sample.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readers,
				sample,
				parameters);
		initLocation(location, sample.getPileupBuilderFactory().isDirected(), pileupBuilders);
		
		parallelPileup = new DefaultParallelPileup(pileupBuilders.length, 0);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());

		
	}

	protected boolean hasNextA() {
		return hasNext(location, pileupBuilders);
	}
	
}