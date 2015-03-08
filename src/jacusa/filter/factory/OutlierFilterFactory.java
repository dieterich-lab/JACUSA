package jacusa.filter.factory;

import java.util.Arrays;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
//import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;
// FINISIH
public class OutlierFilterFactory extends AbstractFilterFactory<Void> {

	/*
	private StatisticParameters statisticParameters;	
	private StatisticCalculator statisticCalculator;
	*/
	
	public OutlierFilterFactory(StatisticParameters statisticParameters) {
		super('O', "Outlier filter");
		//this.statisticParameters = statisticParameters;

		//statisticCalculator = statisticParameters.getStatisticCalculator();
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
	}

	public VarianceOutlierFilter createStorageFilter() {
		return new VarianceOutlierFilter(getC());
	}

	@Override
	public DummyFilterFillCache createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}
	
	private class VarianceOutlierFilter extends AbstractStorageFilter<Void>{

		public VarianceOutlierFilter(final char c) {
			super(c);
		}

		@Override
		public boolean filter(ParallelPileup parallelPileup, Location location,	AbstractWindowIterator windowIterator) {
			if (parallelPileup.getN1() == 1 && parallelPileup.getN2() == 1) {
				return false;
			}

			if (parallelPileup.getN1() > 1 && parallelPileup.getN2() > 1) {
				double[] variance1 = variance(parallelPileup.getPooledPileup1(), parallelPileup.getPileups1());
				double[] variance2 = variance(parallelPileup.getPooledPileup2(), parallelPileup.getPileups2());
				double[] pooledVariance = variance(parallelPileup.getPooledPileup(), parallelPileup.getPileupsP());
				return isOutlier(variance1, pooledVariance) || isOutlier(variance2, pooledVariance);
			}

			if (parallelPileup.getN1() > 1) {
				double[] variance1 = variance(parallelPileup.getPooledPileup1(), parallelPileup.getPileups1());
				double[] pooledVariance = variance(parallelPileup.getPooledPileup(), parallelPileup.getPileupsP());
				return isOutlier(variance1, pooledVariance);
			}

			if (parallelPileup.getN2() > 1) {
				double[] variance2 = variance(parallelPileup.getPooledPileup2(), parallelPileup.getPileups2());
				double[] pooledVariance = variance(parallelPileup.getPooledPileup(), parallelPileup.getPileupsP());
				return isOutlier(variance2, pooledVariance);
			}

			return false;
		}

		private boolean isOutlier(double[] var, double[] pooledVar) {
			double varNorm = norm(var);
			double pooledVarNorm = norm(pooledVar);
			return varNorm > pooledVarNorm;
		}

		private double norm(double[] vec) {
			double s = 0.0;
			for (double e : vec) {
				s += Math.pow(e, 2.0);
			}
			return Math.sqrt(s);
		}

		private double[] mean(Pileup pooled, Pileup[] pileups) {
			double[] mean = new double[pooled.getCounts().getBaseCount().length];
			Arrays.fill(mean, 0.0);
			int n = pileups.length;
			
			for (int i = 0; i < pooled.getCounts().getBaseCount().length; ++i) {
				mean[i] = (double)pooled.getCounts().getBaseCount(i) / (double)n;
			}

			return mean;
		}

		private double[] variance(Pileup pooled, Pileup[] pileups) {
			double[] mean = mean(pooled, pileups);
			double[] variance = new double[pooled.getCounts().getBaseCount().length];
			Arrays.fill(mean, 0.0);
			int n = pileups.length;

			for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
				for (int baseI = 0; baseI < pooled.getCounts().getBaseCount().length; ++baseI) {
					variance[baseI] += Math.pow(mean[baseI] - (double)pileups[pileupI].getCounts().getBaseCount(baseI), 2.0);
				}
			}
			for (int baseI = 0; baseI < pooled.getCounts().getBaseCount().length; ++baseI) {
				variance[baseI] /= (double)(n - 1);
			}

			return variance;
		}

	}
}
		
		
		