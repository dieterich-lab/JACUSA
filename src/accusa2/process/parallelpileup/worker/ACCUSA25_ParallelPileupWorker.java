package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import accusa2.ACCUSA2;
import accusa2.cli.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.filter.process.AbstractParallelPileupFilter;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.ParallelPileupIterator;
import accusa2.pileup.iterator.VariantParallelPileupIterator;
import accusa2.process.parallelpileup.dispatcher.ACCUSA25_ParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class ACCUSA25_ParallelPileupWorker extends AbstractParallelPileupWorker {

	protected final StatisticCalculator statisticCalculator;
	
	public ACCUSA25_ParallelPileupWorker(
			final ACCUSA25_ParallelPileupWorkerDispatcher threadDispatcher, 
			final AnnotatedCoordinate coordinate, final Parameters parameters) {
		super(threadDispatcher, coordinate, parameters);

		statisticCalculator = parameters.getStatisticCalculator().newInstance();
	}

	protected void processParallelPileupIterator(final ParallelPileupIterator parallelPileupIterator) {
		ACCUSA2.printLog("Started screening contig " + 
				parallelPileupIterator.getAnnotatedCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getAnnotatedCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getAnnotatedCoordinate().getEnd());

		while (parallelPileupIterator.hasNext()) {
			final ParallelPileup parallelPileup = parallelPileupIterator.next();

			// calculate unfiltered value
			final double unfilteredValue = statisticCalculator.getStatistic(parallelPileup);

			if(!isValidValue(unfilteredValue) || !isValidParallelPileup(parallelPileup)) {
				continue;
			}

			final StringBuilder sb = new StringBuilder();
			sb.append(resultFormat.convert2String(parallelPileup, unfilteredValue));

			final int pileupFilterCount = parameters.getPileupBuilderFilters().getFilterFactories().size();
			int pileupFilterIndex = 0;
			if(!parameters.getPileupBuilderFilters().hasFiters()) { // no filters

			} else { // calculate filters or quit
				// container for pileups
				ParallelPileup filteredParallelPileups = new ParallelPileup(parallelPileup);

				// container for value(s)
				double filteredValue = unfilteredValue;

				// apply each filter
				for(AbstractFilterFactory filterFactory : parameters.getPileupBuilderFilters().getFilterFactories()) {
					AbstractParallelPileupFilter filter = filterFactory.getParallelPileupFilterInstance();

					// apply filter
					if(filter.filter(filteredParallelPileups)) {

						// quit filtering
						if(filter.quitFiltering()) {
							// reset
							filteredParallelPileups = new ParallelPileup(parallelPileup.getN1(), parallelPileup.getN2());
							filteredValue = -1;
							break;
						}

						// change parallel pileup
						filteredParallelPileups = filter.getFilteredParallelPileup();

						// check coverage restrains
						if(!isValidParallelPileup(filteredParallelPileups)) {
							filteredValue = -1;
							break;
						}

						// calculate value for filterePileups
						filteredValue = statisticCalculator.getStatistic(filteredParallelPileups);
						
						/* negative values are not permitted as result
						if(!isValidValue(filteredValue)) {
							filteredValue = -1;
							break;
						}*/

						// append calculated result
						sb.append(resultFormat.getSEP());
						sb.append(filteredValue);
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
				return;
			}
		}
		
		if(parameters.getMaxThreads() > 1) {
			try {
				tmpOutputWriter.write(resultFormat.getCOMMENT() + String.valueOf(getNextThreadId()));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	protected final boolean isValidParallelPileup(final ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup1().getCoverage() >= parameters.getMinCoverage() && 
				parallelPileup.getPooledPileup2().getCoverage() >= parameters.getMinCoverage() && 
				parallelPileup.getPooledPileup().getAlleles().length < 3;
	}

	protected final boolean isValidValue(final double value) {
		return value < 1.0; // TODO what about -1?
	}

	@Override
	protected ParallelPileupIterator buildParallelPileupIterator_Helper(final AnnotatedCoordinate coordinate,
			final Parameters parameters) {
		return new VariantParallelPileupIterator(coordinate, readers1, readers2, parameters);
	}

}


