package jacusa.pileup.worker;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.FilterConfig;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;
import jacusa.pileup.dispatcher.call.AbstractCallWorkerDispatcher;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public abstract class AbstractCallWorker extends AbstractWorker {

	private final double threshold; 
	private final StatisticCalculator statisticCalculator;
	private final FilterConfig filterConfig;
	
	public AbstractCallWorker(
			final AbstractCallWorkerDispatcher<? extends AbstractCallWorker> workerDispatcher,
			final int threadId,
			final StatisticParameters statisticParameters, 
			final AbstractParameters parameters) {
		super(workerDispatcher, threadId, parameters.getMaxThreads());
		
		this.statisticCalculator = statisticParameters.getStatisticCalculator();
		threshold = statisticParameters.getThreshold();
		this.filterConfig = parameters.getFilterConfig();
	}

	protected StatisticCalculator getStatisticCalculator() {
		return statisticCalculator;
	}

	@Override
	protected Result processParallelPileup(final ParallelPileup parallelPileup, final Location location, final AbstractWindowIterator parallelPileupIterator) {
		// result object
		Result result = new Result();
		result.setParellelPileup(parallelPileup);
		statisticCalculator.addStatistic(result);

		if (result.getStatistic() > threshold) {
			return null;
		}

		if (filterConfig.hasFiters()) {
			// apply each filter
			for (AbstractFilterFactory<?> filterFactory : filterConfig.getFactories()) {
				AbstractStorageFilter<?> storageFilter = filterFactory.createStorageFilter();
				storageFilter.filter(result, location, parallelPileupIterator);
			}
		}

		return result;
	}

}