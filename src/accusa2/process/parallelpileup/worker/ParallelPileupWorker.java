package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import accusa2.ACCUSA2;
import accusa2.cli.parameters.Parameters;
import accusa2.filter.AbstractParallelPileupFilter;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.AbstractParallelPileupWindowIterator;
import accusa2.pileup.iterator.StrandedVariantParallelPileupWindowIterator;
import accusa2.pileup.iterator.UnstrandedVariantParallelPileupWindowIterator;
import accusa2.process.parallelpileup.dispatcher.ParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class ParallelPileupWorker extends AbstractParallelPileupWorker {

	protected final StatisticCalculator statisticCalculator;
	
	public ParallelPileupWorker(final ParallelPileupWorkerDispatcher threadDispatcher,final Parameters parameters) {
		super(threadDispatcher, parameters);

		statisticCalculator = parameters.getStatisticCalculator().newInstance();
	}

	@Override
	protected void processParallelPileupIterator(final AbstractParallelPileupWindowIterator parallelPileupIterator) {
		// print informative log
		ACCUSA2.printLog("Started screening contig " + 
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
			sb.append(resultFormat.convert2String(parallelPileup, unfilteredValue));

			final int pileupFilterCount = parameters.getFilterConfig().getFactories().size();
			int pileupFilterIndex = 0;
			if (! parameters.getFilterConfig().hasFiters()) { 
				// no filters
			} else { // calculate filters or quit
				// container for value(s)
				double filteredValue = unfilteredValue;

				// apply each filter
				for (AbstractFilterFactory filterFactory : parameters.getFilterConfig().getFactories()) {
					// container for pileups

					ParallelPileup filteredParallelPileups = new DefaultParallelPileup(parallelPileup);
					AbstractParallelPileupFilter filter = filterFactory.getFilterInstance();

					// apply filter
					if (filter.filter(filteredParallelPileups)) {
						filteredParallelPileups = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
						filteredValue = -1;
						break;
					} else {
						// append dummy result
						sb.append(resultFormat.getSEP());
						sb.append("*");
					}

					pileupFilterIndex++;
				}

				// append empty result
				for (;pileupFilterIndex < pileupFilterCount; ++pileupFilterIndex) {
					sb.append(resultFormat.getSEP());
					sb.append("-1");
				}

				// append filtered result
				sb.append(resultFormat.getSEP());
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

	@Override
	protected AbstractParallelPileupWindowIterator buildParallelPileupIterator(final AnnotatedCoordinate coordinate, final Parameters parameters) {
		if (parameters.getPileupBuilderFactoryA().isDirected() || parameters.getPileupBuilderFactoryB().isDirected()) {
			return new StrandedVariantParallelPileupWindowIterator(coordinate, readersA, readersB, parameters);
		}
		
		return new UnstrandedVariantParallelPileupWindowIterator(coordinate, readersA, readersB, parameters);
	}

}