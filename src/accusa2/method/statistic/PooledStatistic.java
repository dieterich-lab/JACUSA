package accusa2.method.statistic;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.pileup2Matrix.AbstractPileup2Matrix;
import accusa2.process.pileup2Matrix.BASQ;

/**
 * 
 * @author michael
 * 
 * Z = log10( Dir(alpha_1,phi_1) Dir(alpha_2,phi_2) ) - log10( Dir(alpha_P,phi_1) Dir(alpha_P,phi_2))
 * Test whether distributions are "similar".
 * Calculation of coverage and estimation of alpha(s) is based upon DefaultStatistic 
 * INFO: minimal coverage can be used to estimate alpha(s) - currently the effective coverage is used
 */
public final class PooledStatistic implements StatisticCalculator {

	protected final AbstractPileup2Matrix pileup2Matrix;
	protected final DefaultStatistic defaultStatistic;
	
	public PooledStatistic() {
		pileup2Matrix = new BASQ();
		defaultStatistic = new DefaultStatistic();
		
	}

	@Override
	public StatisticCalculator newInstance() {
		return new PooledStatistic();
	}
	
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		final int coverage1 = defaultStatistic.getCoverage(parallelPileup.getPileups1());
		final int coverage2 = defaultStatistic.getCoverage(parallelPileup.getPileups2());

		final double[][] probs1 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileups1());
		final double[] alpha1 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup1(), coverage1);
		final DirichletDist dirichlet1 = new DirichletDist(alpha1);
		final double density11 = defaultStatistic.getDensity(dirichlet1, probs1);

		final double[][] probs2 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileups2());
		final double[] alpha2 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup2(), coverage2);
		final DirichletDist dirichlet2 = new DirichletDist(alpha2);
		final double density22 = defaultStatistic.getDensity(dirichlet2, probs2);

		final int coverageP = parallelPileup.getPooledPileup().getCoverage();
		// merge pileups from sample 1,2 in pileupP
		final Pileup[] pileupsP = new Pileup[parallelPileup.getN1() + parallelPileup.getN2()];
		System.arraycopy(parallelPileup.getPileups1(), 0, pileupsP, 0, parallelPileup.getPileups1().length);
		System.arraycopy(parallelPileup.getPileups2(), 0, pileupsP, parallelPileup.getPileups1().length, parallelPileup.getPileups2().length);

		// calculation for pooled sample
		final double[][] probsP = defaultStatistic.getPileup2Probs(bases, pileupsP);
		final double[] alphaP = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup(), coverageP);
		final DirichletDist dirichletP = new DirichletDist(alphaP);
		final double densityP = defaultStatistic.getDensity(dirichletP, probsP);

		final double z = (density11 + density22) - (densityP);

		// only positive values are allowed
		return Math.max(0, z);
	}
	
	@Override
	public String getDescription() {
		return "Pooled statistic. z =";
	}

	@Override
	public String getName() {
		return "pooled";
	}

}