package jacusa.pileup.worker;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.dispatcher.call.TwoSampleCallWorkerDispatcher;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.iterator.TwoSampleIterator;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.iterator.variant.VariantParallelPileup;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

public class TwoSampleCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readers1;
	private SAMFileReader[] readers2;
	private TwoSampleCallParameters parameters;
	
	private final Variant variant;
	
	public TwoSampleCallWorker(final TwoSampleCallWorkerDispatcher threadDispatcher, TwoSampleCallParameters parameters) {
		super(threadDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);

		this.parameters = parameters;
		readers1 = initReaders(parameters.getSample1().getPathnames());
		readers2 = initReaders(parameters.getSample2().getPathnames());

		variant = new VariantParallelPileup();
		synchronized (workerDispatcher) {
			parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
		}
	}

	@Override
	protected AbstractWindowIterator buildIterator(final Coordinate coordinate) {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		/*
		if (sample1.getPileupBuilderFactory().isDirected() || 
				sample2.getPileupBuilderFactory().isDirected()) {
			return new TwoSampleIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
		}
		*/
		
		return new TwoSampleIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
	}

	@Override
	protected void close() {
		close(readers1);
		close(readers2);
	}

}