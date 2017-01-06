package jacusa.pileup.worker;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSamplePileupParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;
import jacusa.pileup.dispatcher.pileup.MpileupWorkerDispatcher;
import jacusa.pileup.iterator.AbstractTwoSampleIterator;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.iterator.TwoSampleIterator;
import jacusa.pileup.iterator.variant.AllParallelPileup;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;

import net.sf.samtools.SAMFileReader;

public class MpileupWorker extends AbstractWorker {

	private final TwoSamplePileupParameters parameters;
	private final SAMFileReader[] readers1;
	private final SAMFileReader[] readers2;

	private final Variant variant;
	
	public MpileupWorker(
			MpileupWorkerDispatcher workerDispatcher,
			int threadId,
			TwoSamplePileupParameters parameters) {
		super(
				workerDispatcher, 
				threadId,
				parameters.getMaxThreads()
		);
		this.parameters = parameters;

		readers1 = initReaders(parameters.getSample1().getPathnames());
		readers2 = initReaders(parameters.getSample2().getPathnames());
		
		variant = new AllParallelPileup();
	}

	@Override
	protected Result processParallelPileup(ParallelPileup parallelPileup, final Location location, final AbstractWindowIterator parallelPileupIterator) {
		Result result = new Result();
		result.setParellelPileup(parallelPileup);

		if (parameters.getFilterConfig().hasFiters()) {
			// apply each filter
			for (AbstractFilterFactory<?> filterFactory : parameters.getFilterConfig().getFactories()) {
				AbstractStorageFilter<?> storageFilter = filterFactory.createStorageFilter();
				storageFilter.applyFilter(result, location, parallelPileupIterator);
			}
		}

		return result;
	}

	@Override
	protected AbstractTwoSampleIterator buildIterator(Coordinate coordinate) {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		if (sample1.getPileupBuilderFactory().isStranded() || 
				sample2.getPileupBuilderFactory().isStranded()) {
			return new TwoSampleIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
		}
		
		return new TwoSampleIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
	}

	@Override
	protected void close() {
		close(readers1);
		close(readers2);
	}

}