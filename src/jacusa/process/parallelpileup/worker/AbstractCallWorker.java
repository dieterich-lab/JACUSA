package jacusa.process.parallelpileup.worker;

import jacusa.JACUSA;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.FilterConfig;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.io.format.result.AbstractResultFormat;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.process.parallelpileup.dispatcher.call.AbstractCallWorkerDispatcher;
import jacusa.util.Location;

import java.io.IOException;

public abstract class AbstractCallWorker extends AbstractWorker {

	private final StatisticCalculator statisticCalculator;
	private FilterConfig filterConfig;
	private AbstractResultFormat format;
	
	public AbstractCallWorker(final AbstractCallWorkerDispatcher<? extends AbstractCallWorker> workerDispatcher, 
			final StatisticCalculator statisticCalculator, 
			final AbstractResultFormat format,
			final AbstractParameters parameters) {
		super(workerDispatcher, parameters.getMaxThreads(), parameters.getOutput(), format);
		this.statisticCalculator = statisticCalculator;
		this.filterConfig = parameters.getFilterConfig();
		this.format = format;
	}

	@Override
	protected void processParallelPileupIterator(final AbstractWindowIterator parallelPileupIterator) {
		// print informative log
		JACUSA.printLog("Started screening contig " + 
				parallelPileupIterator.getCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getCoordinate().getEnd());

		// iterate over parallel pileups
		while (parallelPileupIterator.hasNext()) {
			final Location location = parallelPileupIterator.next();
			final ParallelPileup parallelPileup = parallelPileupIterator.getParallelPileup(); 

			// calculate unfiltered value
			final double unfilteredValue = statisticCalculator.getStatistic(parallelPileup);

			final StringBuilder sb = new StringBuilder();

			if (! filterConfig.hasFiters()) {
				// no filters
				sb.append(format.convert2String(parallelPileup, unfilteredValue, "*"));
			} else { // calculate filters or quit
				String filterInfo = "*"; // FIXME
				// apply each filter
				for (AbstractFilterFactory<?> filterFactory : filterConfig.getFactories()) {
					AbstractStorageFilter<?> storageFilter = filterFactory.createStorageFilter();
					
					if (storageFilter.filter(parallelPileup, location, parallelPileupIterator)) {
						if (filterInfo.equals("*")) {
							filterInfo = filterFactory.getC() + storageFilter.getFilterInfo();
						} else {
							filterInfo += "," + filterFactory.getC() + storageFilter.getFilterInfo();
						}
					}
				}

				// append empty result
				// append filtered result
				sb.append(format.convert2String(parallelPileup, unfilteredValue, filterInfo));
			}

			// considered comparisons
			comparisons++;

			try {
				// write output 
				tmpOutputWriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}