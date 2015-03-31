package jacusa.method.call.statistic;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.util.MathUtil;

import java.util.Arrays;


import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;

/**
 * 
 * @author michael
 */
@Deprecated
public final class WeightedMethodOfMomentsStatistic implements StatisticCalculator {

	protected final BaseConfig baseConfig;
	protected final StatisticParameters parameters; 
	
	protected final Phred2Prob phred2Prob;
	
	protected ChiSquareDist dist = new ChiSquareDist(6);
	
	public WeightedMethodOfMomentsStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.baseConfig	= baseConfig;
		this.parameters = parameters;
		
		phred2Prob 		= Phred2Prob.getInstance(baseConfig.getBases().length);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new WeightedMethodOfMomentsStatistic(baseConfig, parameters);
	}

	protected double getDensity(final int[] bases, final Pileup[] pileups) {
		double density = 0.0;
		
		final int pileupN = pileups.length;
		// weights by coverage
		final double[] weights = new double[pileupN] ;
		int totalCoverage = 0;
		// prob. vector per pileup
		final double[][] pileupProbVectors = new double[pileupN][bases.length];
		// alpha
		final double alpha[] = new double[bases.length];
		Arrays.fill(alpha, 1.0/bases.length);
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			// calculate weights
			final Pileup pileup = pileups[pileupI]; // has to be
			int coverage = pileup.getCoverage();
			weights[pileupI] = (double)coverage;
			totalCoverage += coverage;
			
			// calculate prob. vectors
			double[] probVector = phred2Prob.colMeanProb(bases, pileup);
			pileupProbVectors[pileupI] = probVector;
		}
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			weights[pileupI] /= (double)totalCoverage;
		}

		double[] weightedMean = MathUtil.weightedMean(weights, pileupProbVectors);
		double[] weightedVariance = MathUtil.weightedVariance(weights, weightedMean, pileupProbVectors);

		// calculate alphas need to be divided by total coverage
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI = 0; baseI < bases.length; ++baseI) {
				alpha[baseI] += Math.pow(weightedMean[baseI], 2.0) * (1 - weightedMean[baseI]) / weightedVariance[baseI];
			}
		}

		// calculate density
		DirichletDist dirichlet = new DirichletDist(alpha);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			double[] probVector = pileupProbVectors[pileupI]; 
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probVector)));
		}

		return density;
	}
	
	@Override
	public void addStatistic(Result result) {
		final double statistic = getStatistic(result.getParellelPileup());
		if (! filter(statistic)) {
			result.setStatistic(statistic);
		}
	}
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = result.getPooledPileup().getAlleles();

		// first sample(s)
		double density1 = getDensity(bases, parallelPileup.getPileups1());
		
		// second sample(s)
		double density2 = getDensity(bases, parallelPileup.getPileups2());
		
		// pooled sample(s)
		final Pileup[] pileupsP = parallelPileup.getPileupsP();
		double densityP = getDensity(bases, pileupsP);

		final double z = -2 * (densityP) + 2 * (density1 + density2);

		// only positive values are allowed
		if(z < 0.0 ) {
			return 1.0;
		}
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getThreshold() < value;
	}

	@Override
	public String getDescription() {
		return "Weighted Method of Moments estimation";
	}

	@Override
	public String getName() {
		return "wmom";
	}

	@Override
	public void processCLI(String line) {
		// nothing to be done
	}
	
}