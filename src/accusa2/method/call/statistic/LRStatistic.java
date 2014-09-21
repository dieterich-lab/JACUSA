package accusa2.method.call.statistic;

import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.coverage.CoverageEstimateParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

/**
 * 
 * @author michael
 *
 * Calculation of the parameters are based upon DefaultStatistic.
 * Likelihood ratio test to test whether "two dirichlet distributions are similar"
 * Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )
 * Use minimal coverage for the calculation of alphas.
 * -> higher specificity, lower sensitivity
 *
 */

public final class LRStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final CoverageEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	// test what is the best??? 2*k - 2 : k = dimension of modeled prob. vector
	// protected final ChiSquareDist dist = new ChiSquareDist(6);
	
	public LRStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.parameters = parameters;
		int basesN = baseConfig.getBases().length;
		Phred2Prob phred2Prob = Phred2Prob.getInstance(basesN);
		double[] alpha = new double[basesN];
		Arrays.fill(alpha, 0.0);
		estimateParameters = new CoverageEstimateParameters(alpha, phred2Prob);
		this.baseConfig = baseConfig;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new LRStatistic(baseConfig, parameters);
	}

	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		//final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		final int coverageA = estimateParameters.getMeanCoverage(parallelPileup.getPileupsA());
		final int coverageB = estimateParameters.getMeanCoverage(parallelPileup.getPileupsB());
		final int minCoverage = Math.min(coverageA, coverageB);
		
		final double[][] probsA = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsA());
		final double[] alphaA = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsA(), minCoverage);
		final DirichletDist dirichletA = new DirichletDist(alphaA);
		final double densityAA = StatisticUtils.getDensity(dirichletA, probsA);

		final double[][] probsB = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsB());
		final double[] alphaB = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsB(), minCoverage);
		final DirichletDist dirichletB = new DirichletDist(alphaB);
		final double densityBB = StatisticUtils.getDensity(dirichletB, probsB);

		final Pileup[] pileupsP = parallelPileup.getPileupsP();

		final double[][] probsP = estimateParameters.estimateProbs(baseIs, pileupsP);
		final double[] alphaP = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsP(), minCoverage);
		final DirichletDist dirichletP = new DirichletDist(alphaP);
		final double densityP = StatisticUtils.getDensity(dirichletP, probsP);

		final double z = -2 * (densityP) + 2 * (densityAA + densityBB);

		// only positive values are allowed
		if(z < 0.0 ) {
			return 1.0;
		}
		return 1 - ChiSquareDist.cdf(2 * (baseIs.length - 1), 1, z);
	}

	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	// redefined to use natural logarithm
	protected double getDensity(final DirichletDist dirichlet, final double[][] probs) {
		double density = 0.0;

		for(int i = 0; i < probs.length; ++i) {
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

	@Override
	public String getDescription() {
		return "likelihood ratio test (min. coverage for alpha). Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )";
	}

	@Override
	public String getName() {
		return "lr";
	}

}