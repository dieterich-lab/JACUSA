package jacusa.process.parallelpileup.worker;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.iterator.TwoSampleIterator;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.iterator.variant.VariantParallelPileup;
import jacusa.process.parallelpileup.dispatcher.call.TwoSampleCallWorkerDispatcher;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class TwoSampleCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readersA;
	private SAMFileReader[] readersB;
	private TwoSampleCallParameters parameters;
	
	private final Variant variant;
	
	public TwoSampleCallWorker(final TwoSampleCallWorkerDispatcher threadDispatcher, TwoSampleCallParameters parameters) {
		super(threadDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);

		this.parameters = parameters;
		readersA = initReaders(parameters.getSample1().getPathnames());
		readersB = initReaders(parameters.getSample2().getPathnames());

		variant = new VariantParallelPileup();
		synchronized (workerDispatcher) {
			parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
		}
	}

	@Override
	protected AbstractWindowIterator buildIterator(final Coordinate coordinate) {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		if (sample1.getPileupBuilderFactory().isDirected() || 
				sample2.getPileupBuilderFactory().isDirected()) {
			return new TwoSampleIterator(coordinate, variant, readersA, readersB, sample1, sample2, parameters);
		}
		
		return new TwoSampleIterator(coordinate, variant, readersA, readersB, sample1, sample2, parameters);
	}

	@Override
	protected void close() {
		close(readersA);
		close(readersB);
	}

}