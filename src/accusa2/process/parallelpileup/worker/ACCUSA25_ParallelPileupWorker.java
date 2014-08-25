package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import accusa2.ACCUSA2;
import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractParallelPileupFilter;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.ParallelPileupIterator;
import accusa2.pileup.iterator.VariantParallelPileupIterator;
import accusa2.process.parallelpileup.dispatcher.ACCUSA25_ParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class ACCUSA25_ParallelPileupWorker extends AbstractParallelPileupWorker {

	protected final StatisticCalculator statisticCalculator;
	
	public ACCUSA25_ParallelPileupWorker(
			final ACCUSA25_ParallelPileupWorkerDispatcher threadDispatcher, 
			final Parameters parameters) {
		super(threadDispatcher, parameters);

		statisticCalculator = parameters.getStatisticCalculator().newInstance();
	}

	@Override
	protected void processParallelPileupIterator(final ParallelPileupIterator parallelPileupIterator) {
		ACCUSA2.printLog("Started screening contig " + 
				parallelPileupIterator.getAnnotatedCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getAnnotatedCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getAnnotatedCoordinate().getEnd());

		while (parallelPileupIterator.hasNext()) {
			final ParallelPileup parallelPileup = parallelPileupIterator.next();


			if(!isValidParallelPileup(parallelPileup)) {
				continue;
			}

			// calculate unfiltered value
			final double unfilteredValue = statisticCalculator.getStatistic(parallelPileup);
			
			if(!isValidValue(unfilteredValue)) {
				continue;
			}

			final StringBuilder sb = new StringBuilder();
			sb.append(resultFormat.convert2String(parallelPileup, unfilteredValue));

			final int pileupFilterCount = parameters.getFilterConfig().getFactories().size();
			int pileupFilterIndex = 0;
			if(!parameters.getFilterConfig().hasFiters()) { // no filters

			} else { // calculate filters or quit
				// container for value(s)
				double filteredValue = unfilteredValue;

				// apply each filter
				for(AbstractFilterFactory filterFactory : parameters.getFilterConfig().getFactories()) {
					// container for pileups

					ParallelPileup filteredParallelPileups = new DefaultParallelPileup(parallelPileup);
					AbstractParallelPileupFilter filter = filterFactory.getFilterInstance();

					// apply filter
					if(filter.filter(filteredParallelPileups)) {

						// quit filtering
						//if(filter.quitFiltering()) {
							// reset
							filteredParallelPileups = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
							filteredValue = -1;
							break;
						//}

						/*
						// change parallel pileup
						filteredParallelPileups = filter.getFilteredParallelPileup();

						// check coverage restrains
						if(!isValidParallelPileup(filteredParallelPileups)) {
							filteredValue = -1;
							break;
						}

						// calculate value for filterePileups
						filteredValue = statisticCalculator.getStatistic(filteredParallelPileups);

						// append calculated result
						sb.append(resultFormat.getSEP());
						sb.append(filteredValue);
						*/
					} else {
						// append dummy result
						sb.append(resultFormat.getSEP());
						sb.append("*");
					}

					pileupFilterIndex++;
				}

				// append empty result
				for(;pileupFilterIndex < pileupFilterCount; ++pileupFilterIndex) {
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

	/**
	 * 
	 * @param parallelPileup
	 * @return
	 */
	protected final boolean isValidParallelPileup(final ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileupA().getCoverage() >= parameters.getMinCoverage() && 
				parallelPileup.getPooledPileupB().getCoverage() >= parameters.getMinCoverage() && 
				parallelPileup.getPooledPileup().getAlleles().length < 3;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	protected final boolean isValidValue(final double value) {
		if(parameters.getDebug()) {
			return value < 1.0;
		} else {
			return value <= 1.0;
		}
	}

	@Override
	protected ParallelPileupIterator buildParallelPileupIterator(final AnnotatedCoordinate coordinate, final Parameters parameters) {
		return new VariantParallelPileupIterator(coordinate, readers1, readers2, parameters);
	}

}