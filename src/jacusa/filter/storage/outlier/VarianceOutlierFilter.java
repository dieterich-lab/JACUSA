package jacusa.filter.storage.outlier;

import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

import java.util.Arrays;

public class VarianceOutlierFilter extends OutlierStorageFilter {

	public VarianceOutlierFilter(final char c) {
		super(c);
	}

	@Override
	public OutlierStorageFilter createInstance(char c) {
		return new VarianceOutlierFilter(c);
	}

	@Override
	public String getType() {
		return "variance";
	}
	
	@Override
	public void process(String line) {
		// nothing to be done
	}
	
	@Override
	public boolean filter(final Result result, final Location location,	final AbstractWindowIterator windowIterator) {
		final ParallelPileup parallelPileup = result.getParellelPileup();
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
		double[] mean = new double[pooled.getCounts().getBaseLength()];
		Arrays.fill(mean, 0.0);
		int n = pileups.length;
		
		for (int i = 0; i < pooled.getCounts().getBaseLength(); ++i) {
			mean[i] = (double)pooled.getCounts().getBaseCount(i) / (double)n;
		}

		return mean;
	}

	private double[] variance(Pileup pooled, Pileup[] pileups) {
		double[] mean = mean(pooled, pileups);
		double[] variance = new double[pooled.getCounts().getBaseLength()];
		Arrays.fill(mean, 0.0);
		int n = pileups.length;

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI = 0; baseI < pooled.getCounts().getBaseLength(); ++baseI) {
				variance[baseI] += Math.pow(mean[baseI] - (double)pileups[pileupI].getCounts().getBaseCount(baseI), 2.0);
			}
		}
		for (int baseI = 0; baseI < pooled.getCounts().getBaseLength(); ++baseI) {
			variance[baseI] /= (double)(n - 1);
		}

		return variance;
	}

}
