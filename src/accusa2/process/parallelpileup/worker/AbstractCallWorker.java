package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import accusa2.ACCUSA;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.filter.FilterConfig;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.filter.feature.AbstractFeatureFilter;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.AbstractWindowIterator;
import accusa2.process.parallelpileup.dispatcher.call.AbstractCallWorkerDispatcher;

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
		ACCUSA.printLog("Started screening contig " + 
				parallelPileupIterator.getAnnotatedCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getAnnotatedCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getAnnotatedCoordinate().getEnd());

		// iterate over parallel pileups
		while (parallelPileupIterator.hasNext()) {
			final ParallelPileup parallelPileup = parallelPileupIterator.next();

			// calculate unfiltered value
			final double unfilteredValue = statisticCalculator.getStatistic(parallelPileup);

			final StringBuilder sb = new StringBuilder();
			sb.append(format.convert2String(parallelPileup, unfilteredValue));

			final int pileupFilterCount = filterConfig.getFactories().size();
			int pileupFilterIndex = 0;
			if (! filterConfig.hasFiters()) { 
				// no filters
			} else { // calculate filters or quit
				// container for value(s)
				double filteredValue = unfilteredValue;

				// apply each filter
				for (AbstractFilterFactory filterFactory : filterConfig.getFactories()) {
					// container for pileups

					ParallelPileup filteredParallelPileups = new DefaultParallelPileup(parallelPileup);
					AbstractFeatureFilter featureFilter = filterFactory.getFilterInstance();

					// apply filter
					if (featureFilter.filter(filteredParallelPileups)) {
						filteredParallelPileups = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
						filteredValue = -1;
						break;
					} else {
						// append dummy result
						sb.append(format.getSEP());
						sb.append("*");
					}

					pileupFilterIndex++;
				}

				// append empty result
				for (;pileupFilterIndex < pileupFilterCount; ++pileupFilterIndex) {
					sb.append(format.getSEP());
					sb.append("-1");
				}

				// append filtered result
				sb.append(format.getSEP());
				sb.append(filteredValue);
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