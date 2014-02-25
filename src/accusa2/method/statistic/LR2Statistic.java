package accusa2.method.statistic;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.pileup2Matrix.AbstractPileup2Matrix;
import accusa2.process.pileup2Matrix.BASQ;

/**
 * 
 * @author michael
 * Calculation of the parameters are based upon DefaultStatistic.
 * Likelihood ratio test to test whether "two dirichlet distributions are similar"
 * Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )
 * Use effective coverage for the calculation of alphas.
 * -> lower specificity, higher sensitivity
 */

public final class LR2Statistic implements StatisticCalculator {

	protected final AbstractPileup2Matrix pileup2Matrix;
	protected final DefaultStatistic defaultStatistic;

	// TODO test what is the best??? 2*k - 2 : k = dimension of modeled prob. vector
	protected ChiSquareDist dist = new ChiSquareDist(6);
	
	public LR2Statistic() {
		pileup2Matrix = new BASQ();
		defaultStatistic = new DefaultStatistic();
	}

	@Override
	public StatisticCalculator newInstance() {
		return new LR2Statistic();
	}
	
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		final int coverage1 = defaultStatistic.getCoverage(parallelPileup.getPileups1());
		final int coverage2 = defaultStatistic.getCoverage(parallelPileup.getPileups2());
		
		final double[][] probs1 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileups1());
		final double[] alpha1 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup1(), coverage1);
		final DirichletDist dirichlet1 = new DirichletDist(alpha1);
		final double density11 = getDensity(dirichlet1, probs1);

		final double[][] probs2 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileups2());
		final double[] alpha2 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup2(), coverage2);
		final DirichletDist dirichlet2 = new DirichletDist(alpha2);
		final double density22 = getDensity(dirichlet2, probs2);

		final int coverageP = parallelPileup.getPooledPileup().getCoverage();
		final Pileup[] pileupsP = new Pileup[parallelPileup.getN1() + parallelPileup.getN2()];
		System.arraycopy(parallelPileup.getPileups1(), 0, pileupsP, 0, parallelPileup.getPileups1().length);
		System.arraycopy(parallelPileup.getPileups2(), 0, pileupsP, parallelPileup.getPileups1().length, parallelPileup.getPileups2().length);

		final double[][] probsP = defaultStatistic.getPileup2Probs(bases, pileupsP);
		final double[] alphaP = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup(), coverageP);
		final DirichletDist dirichletP = new DirichletDist(alphaP);
		final double densityP = getDensity(dirichletP, probsP);

		final double z = -2 * (densityP) + 2 * (density11 + density22);

		// only positive values are allowed
		if(z < 0 ) {
			return 1.0;
		}
		return 1 - dist.cdf(z);
	}

	/*
	 * TODO
	 * redundant code: see LRStatistic, DefaultStatistic(log10)
	 */
	protected double getDensity(final DirichletDist dirichlet, final double[][] probs) {
		double density = 0.0;

		for(int i = 0; i < probs.length; ++i) {
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

	@Override
	public String getDescription() {
		return "likelihood ratio test (effective coverage for alpha). Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )";
	}

	@Override
	public String getName() {
		return "lr2";
	}

}