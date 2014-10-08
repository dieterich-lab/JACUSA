package accusa2.method.call.statistic;

import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.MathUtil;

/**
 * 
 * @author michael
 */

public final class DirichletMOMsStatistic implements StatisticCalculator {

	protected final BaseConfig baseConfig;
	protected final StatisticParameters parameters; 
	protected final Phred2Prob phred2Prob;

	// What comes here?
	protected ChiSquareDist dist = new ChiSquareDist(6);

	public DirichletMOMsStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.baseConfig	= baseConfig;
		this.parameters = parameters;
		
		phred2Prob 		= Phred2Prob.getInstance(baseConfig.getBases().length);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMOMsStatistic(baseConfig, parameters);
	}

	protected double getDensity(final int[] baseIs, final Pileup[] pileups) {
		double density = 0.0;

		final int pileupN = pileups.length;
		// prob. vector per pileup
		final double[][] pileupProbVectors = new double[pileupN][baseIs.length];

		// alpha
		final double alpha[] = new double[baseIs.length];
		Arrays.fill(alpha, 1.0 / (double)baseIs.length);

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			final Pileup pileup = pileups[pileupI];
			
			// calculate prob. vectors
			double[] probVector = phred2Prob.colMean(baseIs, pileup);
			pileupProbVectors[pileupI] = probVector;
		}

		double[] mean = MathUtil.mean(pileupProbVectors);
		double[] variance = MathUtil.variance(mean, pileupProbVectors);
		//correctVariance(baseIs, mean, variance);

		// calculate alphas need to be divided by total coverage
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alpha[baseI] += Math.pow(mean[baseI], 2.0) * (1.0 - mean[baseI]) / variance[baseI];
			}
		}
		//correctAlpha(baseIs, alpha, mean, variance);
		
		// calculate density
		DirichletDist dirichlet = new DirichletDist(alpha);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			double[] probVector = pileupProbVectors[pileupI]; 
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probVector)));
		}

		return density;
	}
	
	private void correctVariance(final int[] baseIs, double[] mean, double[] variance) {
		double minMean = 0.01;
		for (int i = 0; i < baseIs.length; ++i) {
			if (mean[i] < minMean) {
				variance[i] = 0.1;
			}
		}
	}
	
	/*
	private void correctAlpha(final int[] baseIs, double[] alphas, double[] mean, double[] variance) {
		double alpha = 1.0 / (double)baseIs.length;
		double minMean = 0.01;
		for (int i = 0; i < baseIs.length; ++i) {
			if (mean[i] < minMean) {
				alphas[i] = alpha;
			}
		}
	}
	*/

	/* Deprecated
	private void correctVariance(double[] variance) {
		double min = 0.1;
		for (int i = 0; i < variance.length; ++i) {
			variance[i] = Math.max(variance[i], min);
		}
	}
	*/
	
	public double getStatistic(final ParallelPileup parallelPileup) {
		if (true) {
			return 1.0;
		}
		
		final int baseIs[] = {0, 1, 2, 3};
		//final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample(s)
		double densityA = getDensity(baseIs, parallelPileup.getPileupsA());
		
		// second sample(s)
		double densityB = getDensity(baseIs, parallelPileup.getPileupsB());
		
		// pooled sample(s)
		final Pileup[] pileupsP = parallelPileup.getPileupsP();
		double densityP = getDensity(baseIs, pileupsP);

		final double z = -2 * (densityP) + 2 * (densityA + densityB);

		// only positive values are allowed
		if (z < 0.0 ) {
			return 1.0;
		}
		/* TODO remove
		if (z > 10.0) {
			int j = 0;
			j++;
		}
		*/
		
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	@Override
	public String getDescription() {
		return "Dirichlet - Method of Moments estimation";
	}

	@Override
	public String getName() {
		return "DirMom";
	}

}