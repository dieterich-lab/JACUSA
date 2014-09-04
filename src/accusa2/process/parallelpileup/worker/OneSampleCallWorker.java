package accusa2.process.parallelpileup.worker;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.OneSampleCallParameters;
import accusa2.pileup.iterator.AbstractWindowIterator;
import accusa2.pileup.iterator.OneSampleStrandedIterator;
import accusa2.pileup.iterator.OneSampleUnstrandedIterator;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.pileup.iterator.variant.VariantParallelPileup;
import accusa2.process.parallelpileup.dispatcher.call.OneSampleCallWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class OneSampleCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readersA;
	private final OneSampleCallParameters parameters;
	
	private final Variant variant;

	public OneSampleCallWorker(OneSampleCallWorkerDispatcher threadDispatcher, OneSampleCallParameters parameters) {
		super(threadDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);

		this.parameters = parameters;
		readersA = initReaders(parameters.getSampleA().getPathnames());

		variant = new VariantParallelPileup();
		
		parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
	}

	@Override
	protected AbstractWindowIterator buildIterator(final AnnotatedCoordinate coordinate) {
		if (parameters.getSampleA().getPileupBuilderFactory().isDirected()) {
			return new OneSampleStrandedIterator(coordinate, variant, readersA, parameters.getSampleA(), parameters);
		}

		return new OneSampleUnstrandedIterator(coordinate, variant, readersA, parameters.getSampleA(), parameters);
	}

	@Override
	protected void close() {
		close(readersA);
	}

}