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

public final class NumericalStatistic implements StatisticCalculator {

	protected final BaseConfig baseConfig;
	protected final StatisticParameters parameters; 
	
	protected final Phred2Prob phred2Prob;

	protected ChiSquareDist dist = new ChiSquareDist(6);
	
	public NumericalStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.baseConfig	= baseConfig;
		this.parameters = parameters;
		
		phred2Prob 		= Phred2Prob.getInstance(baseConfig.getBases().length);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new NumericalStatistic(baseConfig, parameters);
	}

	protected double[][] getPileup2Probs(final int bases[], final Pileup[] pileups) {
		final double[][] probs = new double[pileups.length][bases.length];

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			// sum the probabilities giving alpha 
			double[] probMean = phred2Prob.colMean(bases, pileups[pileupI]);
			probs[pileupI] = probMean;
		}

		return probs;
	}
	
	protected int estimateMeanCoverage(Pileup[] pileups) {
		if(pileups.length == 1) { 
			return pileups[0].getCoverage();
		}

		int coverage = 0;
		for(Pileup pileup : pileups) {
			coverage += pileup.getCoverage();
		}
		return (int)Math.round((double)coverage / (double)pileups.length);
	}
	
	protected double[] initialEstimate(final int bases[], Pileup pileups[]) {
		final double[] alphas = new double[bases.length];
		Arrays.fill(alphas, 0.0);

		int meanCoverage = estimateMeanCoverage(pileups);
		final double[][] probs = getPileup2Probs(bases, pileups);
		final double[] mean = MathUtil.mean(probs);
		for (int i = 0; i < alphas.length; ++i) {
			alphas[i] = mean[i] * (double)meanCoverage;
		}

		return alphas;
	}
	
	protected double[] getWeights(final int[] bases, final Pileup[] pileups) {
		final double[] weights = new double[bases.length];
		final int n = pileups.length;

		for (int i = 0; i < n; ++i) {
			final Pileup pileup = pileups[i];
			weights[i] = (double)pileup.getCoverage() / (double)n;
		}
		
		return weights;
	}
	
	protected double[] estimateAlpha(final int bases[], final Pileup[] pileups) {
		double[] alpha = initialEstimate(bases, pileups);
		//double[] weights = getWeights(bases, pileups);

		return alpha;
	}
	
	protected double getDensity(final int[] bases, final Pileup[] pileups) {
		double density = 0.0;

		final int pileupN = pileups.length;
		// weights by coverage
		final double[] weights = new double[pileupN] ;
		int totalCoverage = 0;
		// prob. vector per pileup
		final double[][] pileupProbVectors = new double[pileupN][bases.length];
		
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			// calculate weights
			final Pileup pileup = pileups[pileupI]; // has to be
			int coverage = pileup.getCoverage();
			weights[pileupI] = (double)coverage;
			totalCoverage += coverage;
						
			// calculate prob. vectors
			double[] probVector = phred2Prob.colSum(bases, pileup);
			pileupProbVectors[pileupI] = probVector;
		}
		
		// init alpha
		final double alpha[] = new double[bases.length];
		Arrays.fill(alpha, 0.0);
		final double mean[] = MathUtil.mean(pileupProbVectors);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI = 0; baseI < bases.length; ++baseI) {
				alpha[baseI] += mean[baseI] * weights[pileupI] / totalCoverage;
			}
		}
		
		// TODO do numerical search
		
		DirichletDist dirichlet = new DirichletDist(alpha);
		for (final Pileup pileup : pileups) {
			double[] prob = phred2Prob.colSum(bases, pileup);
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(prob)));
		}

		return density;
	}
	
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample(s)
		double density1 = getDensity(bases, parallelPileup.getPileupsA());
		
		// second sample(s)
		double density2 = getDensity(bases, parallelPileup.getPileupsB());
		
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
		return parameters.getStat() < value;
	}

	@Override
	public String getDescription() {
		return "Numerical Estimation of parameters";
	}

	@Override
	public String getName() {
		return "num";
	}

}