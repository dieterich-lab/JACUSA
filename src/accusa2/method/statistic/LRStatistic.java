package accusa2.method.statistic;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.Parameters;
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

	protected final Parameters parameters;
	
	protected final Phred2Prob phred2Prob;
	protected final DefaultStatistic defaultStatistic;

	// TODO test what is the best??? 2*k - 2 : k = dimension of modeled prob. vector
	protected final ChiSquareDist dist = new ChiSquareDist(6);

	public LRStatistic(Parameters parameters) {
		this.parameters = parameters;

		phred2Prob = Phred2Prob.getInstance(parameters.getBaseConfig().getBases().length);
		defaultStatistic = new DefaultStatistic(parameters);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new LRStatistic(parameters);
	}

	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		final int coverage1 = defaultStatistic.getMeanCoverage(parallelPileup.getPileupsA());
		final int coverage2 = defaultStatistic.getMeanCoverage(parallelPileup.getPileupsB());
		final int minCoverage = Math.min(coverage1, coverage2);

		final double[][] probs1 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileupsA());
		final double[] alpha1 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileupA(), minCoverage);
		final DirichletDist dirichlet1 = new DirichletDist(alpha1);
		final double density11 = getDensity(dirichlet1, probs1);

		final double[][] probs2 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileupsB());
		final double[] alpha2 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileupB(), minCoverage);
		final DirichletDist dirichlet2 = new DirichletDist(alpha2);
		final double density22 = getDensity(dirichlet2, probs2);

		//final int coverageP = parallelPileup.getPooledPileup().getCoverage();
		final Pileup[] pileupsP = new Pileup[parallelPileup.getNA() + parallelPileup.getNB()];
		System.arraycopy(parallelPileup.getPileupsA(), 0, pileupsP, 0, parallelPileup.getPileupsA().length);
		System.arraycopy(parallelPileup.getPileupsB(), 0, pileupsP, parallelPileup.getPileupsA().length, parallelPileup.getPileupsB().length);

		final double[][] probsP = defaultStatistic.getPileup2Probs(bases, pileupsP);
		final double[] alphaP = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup(), minCoverage);
		final DirichletDist dirichletP = new DirichletDist(alphaP);
		final double densityP = getDensity(dirichletP, probsP);

		// test 0 vs A model
		final double z = -2 * (densityP) + 2 * (density11 + density22);

		// only positive values are allowed
		if(z < 0.0 ) {
			return 1.0;
		}
		return 1 - dist.cdf(z);
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