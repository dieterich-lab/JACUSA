package accusa2.process.parallelpileup.worker;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSampleCallParameters;
import accusa2.pileup.iterator.AbstractWindowIterator;
import accusa2.pileup.iterator.TwoSampleStrandedIterator;
import accusa2.pileup.iterator.TwoSampleUnstrandedIterator;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.pileup.iterator.variant.VariantParallelPileup;
import accusa2.process.parallelpileup.dispatcher.call.TwoSampleCallWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readersA;
	private SAMFileReader[] readersB;
	private TwoSampleCallParameters parameters;
	
	private final Variant variant;
	
	public TwoSampleCallWorker(final TwoSampleCallWorkerDispatcher threadDispatcher, TwoSampleCallParameters parameters) {
		super(threadDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);

		this.parameters = parameters;
		readersA = initReaders(parameters.getSampleA().getPathnames());
		readersB = initReaders(parameters.getSampleB().getPathnames());

		variant = new VariantParallelPileup();
		
		parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
	}

	@Override
	protected AbstractWindowIterator buildIterator(final AnnotatedCoordinate coordinate) {
		SampleParameters sampleA = parameters.getSampleA();
		SampleParameters sampleB = parameters.getSampleB();

		if (sampleA.getPileupBuilderFactory().isDirected() || 
				sampleB.getPileupBuilderFactory().isDirected()) {
			return new TwoSampleStrandedIterator(coordinate, variant, readersA, readersB, sampleA, sampleB, parameters);
		}
		
		return new TwoSampleUnstrandedIterator(coordinate, variant, readersA, readersB, sampleA, sampleB, parameters);
	}

	@Override
	protected void close() {
		close(readersA);
		close(readersB);
	}

}