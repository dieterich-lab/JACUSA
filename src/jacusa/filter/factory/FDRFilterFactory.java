package jacusa.filter.factory;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.StatisticParameters;

import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.sample.PermutateBases;
import jacusa.pileup.sample.PermutateParallelPileup;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

// TODO finish
@Deprecated
public class FDRFilterFactory extends AbstractFilterFactory<Void> {

	private double fdr = 0.1;
	private StatisticParameters parameters;

	public FDRFilterFactory(final StatisticParameters parameters) {
		super('F', "Filter by FDR");
		this.parameters = parameters;
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		// TODO
	}

	@Override
	public AbstractFilterStorage<Void> createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}

	@Override
	public FDRFilter createStorageFilter() {
		return new FDRFilter(getC());
	}

	private class FDRFilter extends AbstractStorageFilter<Void> {

		private int n;
		private PermutateParallelPileup ppp;
		private StatisticCalculator calculator;
		
		public FDRFilter(final char c) {
			super(c);
			n = 100;
			ppp = new PermutateBases();
			calculator = parameters.getStatisticCalculator();
		}

		@Override
		public boolean filter(final ParallelPileup parallelPileup, final Location location,	final AbstractWindowIterator windowIterator) {
			double observedStat = calculator.getStatistic(parallelPileup); 
			System.out.println(parallelPileup.prettyPrint());

			int c = 0;
			for (int i = 0; i < n; ++i) {
				ParallelPileup permutated = ppp.permutate(parallelPileup);
				permutated.getPooledPileup();
				double permutatedStat = calculator.getStatistic(permutated);
				System.out.println(permutated.prettyPrint());
				if (permutatedStat <= observedStat) {
					++c;
				}
			}

			double eFdr = (double)c / (double)n;

			setFilterInfo(":FDR=" + Double.toString(eFdr));

			return eFdr <= fdr;
		}

	}

}