package jacusa.filter.factory;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.StatisticParameters;

import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.sample.PermutateBases;
import jacusa.pileup.sample.PermutateParallelPileup;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class FDRFilterFactory extends AbstractFilterFactory<Void> {

	private double fdr = 0.1;
	private int n = 100;

	private StatisticParameters parameters;

	public FDRFilterFactory(final StatisticParameters parameters) {
		super('F', "Filter by FDR");
		this.parameters = parameters;
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		for (int i = 1; i < s.length; ++i) {
			// key=value
			String[] kv = s[i].split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// set value
			if (key.equals("n")) {
				n = Integer.parseInt(value);
			} else if(key.equals("fdr")) {
				fdr = Double.parseDouble(value);
			} else {
				throw new IllegalArgumentException("Invalid argument " + key + " IN: " + line);
			}
		}
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
		private PermutateParallelPileup ppp;
		private StatisticCalculator calculator;
		
		public FDRFilter(final char c) {
			super(c);
			ppp = new PermutateBases();
			calculator = parameters.getStatisticCalculator();
		}

		@Override
		public boolean filter(final Result result, final Location location,	final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			double observedStat = result.getStatistic(); 

			int c = 0;
			for (int i = 0; i < n; ++i) {
				ParallelPileup permutated = ppp.permutate(parallelPileup);
				// permutated.getPooledPileup();
				double permutatedStat = calculator.getStatistic(permutated);
				if (permutatedStat <= observedStat) {
					++c;
				}
			}

			double eFdr = (double)c / (double)n;

			result.addInfo("FDR=" + Double.toString(eFdr));

			return eFdr <= fdr;
		}

	}

}