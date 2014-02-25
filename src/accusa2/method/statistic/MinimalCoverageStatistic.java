package accusa2.method.statistic;


import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.pileup.ParallelPileup;
import accusa2.process.pileup2Matrix.AbstractPileup2Matrix;
import accusa2.process.pileup2Matrix.BASQ;

/**
 * 
 * @author michael
 * Similar to default but alphas are estimated with the minimal coverage of both samples.
 * -> higher specificity, but lower sensitivity 
 */
public class MinimalCoverageStatistic implements StatisticCalculator {

	protected AbstractPileup2Matrix pileup2Matrix;
	protected DefaultStatistic defaultStatistic;
	
	public MinimalCoverageStatistic() {
		pileup2Matrix = new BASQ();
		defaultStatistic = new DefaultStatistic(); 
	}

	@Override
	public StatisticCalculator newInstance() {
		return new MinimalCoverageStatistic();
	}

	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		final int coverage1 = defaultStatistic.getCoverage(parallelPileup.getPileups1());
		final int coverage2 = defaultStatistic.getCoverage(parallelPileup.getPileups2());
		final int coverageMin = Math.min(coverage1, coverage2);
		
		final double[][] probs1 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileups1());
		final double[] alpha1 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup1(), coverageMin);
		final DirichletDist dirichlet1 = new DirichletDist(alpha1);
		final double density11 = defaultStatistic.getDensity(dirichlet1, probs1);

		final double[][] probs2 = defaultStatistic.getPileup2Probs(bases, parallelPileup.getPileups2());
		final double[] alpha2 = defaultStatistic.estimateAlpha(bases, parallelPileup.getPooledPileup2(), coverageMin);

		final DirichletDist dirichlet2 = new DirichletDist(alpha2);
		final double density22 = defaultStatistic.getDensity(dirichlet2, probs2);

		final double density12 = defaultStatistic.getDensity(dirichlet1, probs2);
		final double density21 = defaultStatistic.getDensity(dirichlet2, probs1);

		final double z = (density11 + density22) - (density12 + density21);
		return Math.max(0, z);
	}

	@Override
	public String getDescription() {
		return "Similar to default. Use the minimal coverage to estimate alpha(s)";
	}

	@Override
	public String getName() {
		return "coverage";
	}

}