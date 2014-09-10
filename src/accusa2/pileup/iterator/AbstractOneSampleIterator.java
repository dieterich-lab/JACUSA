package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractOneSampleIterator extends AbstractWindowIterator {

	protected SampleParameters sample;

	protected Location location;
	protected final AbstractPileupBuilder[] pileupBuilders;	

	// output
	protected ParallelPileup parallelPileup;

	public AbstractOneSampleIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readers,
			final SampleParameters sample, 
			AbstractParameters parameters) {
		super(annotatedCoordinate, filter, parameters);

		this.sample = sample;
		pileupBuilders = createPileupBuilders(
				sample.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readers,
				sample,
				parameters);
		location = initLocation(annotatedCoordinate, sample.getPileupBuilderFactory().isDirected(), pileupBuilders);
		
		parallelPileup = new DefaultParallelPileup(pileupBuilders.length, 0);
	}

	protected boolean hasNextA() {
		return hasNext(location, pileupBuilders);
	}

	protected Pileup[] removeBase(int baseI, Pileup[] pileups) {
		int n = pileups.length;
		Pileup[] ret = new Pileup[n];

		for (int i = 0; i < n; ++i) {
			Pileup pileup = new DefaultPileup(pileups[i]);

			pileup.getCounts().substract(baseI, pileups[i].getCounts());
			ret[i] = pileup;
		}

		return ret;
	}

	protected int getHomomorphBaseI(Pileup pooled) {
		int baseI = -1;
		
		return baseI;
	}

}