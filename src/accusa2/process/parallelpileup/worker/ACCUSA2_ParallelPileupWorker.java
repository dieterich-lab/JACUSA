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
import accusa2.pileup.sample.PermutateBasesWithoutReplacement;
import accusa2.pileup.sample.PermutateParallelPileup;
import accusa2.pileup.sample.PermutateParallelPileupWithoutReplacement;
import accusa2.process.parallelpileup.dispatcher.ACCUSA2_ParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;
import accusa2.util.DiscriminantStatisticContainer;

public class ACCUSA2_ParallelPileupWorker extends AbstractParallelPileupWorker {

	protected final StatisticCalculator statisticCalculator;

	PermutateParallelPileup permutateParallelPileup;
	PermutateParallelPileup permutateBases;

	protected int permutations;

	protected DiscriminantStatisticContainer discriminantStatisticContainer;

	public ACCUSA2_ParallelPileupWorker(
			final ACCUSA2_ParallelPileupWorkerDispatcher threadDispatcher, 
			final AnnotatedCoordinate coordinate, final Parameters parameters) {
		super(threadDispatcher, coordinate, parameters);

		statisticCalculator = parameters.getStatisticCalculator().newInstance();

		permutateParallelPileup = new PermutateParallelPileupWithoutReplacement();
		permutateBases = new PermutateBasesWithoutReplacement();

		permutations 		= parameters.getPermutations();

		discriminantStatisticContainer = new DiscriminantStatisticContainer();
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
				processParallelPileup(parallelPileup, unfilteredValue);
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

				// don't process filtered results when one filter said to quit
				boolean correct = true;
				if(pileupFilterIndex != pileupFilterCount) {
					correct = false;
				}

				// append empty result
				for(;pileupFilterIndex < pileupFilterCount; ++pileupFilterIndex) {
					sb.append(resultFormat.getSEP());
					sb.append("-1");
				}

				// append filtered result
				sb.append(resultFormat.getSEP());
				sb.append(filteredValue);

				if(correct) {
					processParallelPileup(filteredParallelPileups, filteredValue);
				}
			}

			// considered comparisons
			comparisons++;
			try {
				// write output 
				tmpOutput.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * construct true positive set on filtered values
	 */
	private final void processParallelPileup(final ParallelPileup parallelPileup, final double value) {
		if(isValidParallelPileup(parallelPileup) && isValidValue(value)) {
			discriminantStatisticContainer.addR_Value(value, parallelPileup);
			resample(value, parallelPileup);
		}
	}

	protected final boolean isValidParallelPileup(final ParallelPileup parallelPileup) {
		//int[] variantsBases = parallelPileup.getVariantBases();

		/*
		int count = 2;
		if(variantsBases.length > 0) {
			int variantBase = variantsBases[0];
			count = parallelPileup.getPooledPileup().getBaseCount(variantBase);
		}
		*/

		return parallelPileup.getPooledPileup1().getCoverage() >= parameters.getMinCoverage() && 
				parallelPileup.getPooledPileup2().getCoverage() >= parameters.getMinCoverage() && 
				parallelPileup.getPooledPileup().getAlleles().length < 3;
	}

	protected final boolean isValidValue(final double value) {
		return value < 1.0;
	}

	@Override
	protected ParallelPileupIterator buildParallelPileupIterator_Helper(final AnnotatedCoordinate coordinate,
			final Parameters parameters) {
		return new VariantParallelPileupIterator(coordinate, readers1, readers2, parameters);
	}

	private void resample(final double observedValue, final ParallelPileup parallelPileup) {
		// permutate
		double sumValue = 0.0;
		
		for(int i = 0; i < permutations; ++i) {
			// container
			// sample from pooled pileup
			ParallelPileup permutatedParallelPileup = permutateParallelPileup.permutate(parallelPileup);
			double sampledValue = statisticCalculator.getStatistic(permutatedParallelPileup);

			ParallelPileup permutatedParallelPileup2 = permutateBases.permutate(parallelPileup);
			double sampleValue2 = statisticCalculator.getStatistic(permutatedParallelPileup2);
			sampledValue = Math.max(sampledValue, sampleValue2);
			
			if(isValidValue(sampledValue)) {
				sumValue += sampledValue;
			} else {
				--i;
			}
		}
		double val = sumValue / (double) permutations;
		discriminantStatisticContainer.addNULL_Value(val, parallelPileup);
	}

	public DiscriminantStatisticContainer getStatisticContainer() {
		return discriminantStatisticContainer;
	}

}


