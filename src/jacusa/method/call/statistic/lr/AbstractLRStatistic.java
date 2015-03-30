package jacusa.method.call.statistic.lr;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.coverage.CoverageEstimateParameters;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;

public abstract class AbstractLRStatistic implements StatisticCalculator {

	private String name;
	private String desc;

	protected final StatisticParameters parameters;
	protected final CoverageEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	// 2*k - 2 : k = dimension of modeled prob. vector
	protected final ChiSquareDist dist = new ChiSquareDist(6);
	
	public AbstractLRStatistic(final String name, final String desc, final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.name = name;
		this.desc = desc;
		
		this.parameters = parameters;

		final int k = baseConfig.getK();

		Phred2Prob phred2Prob = Phred2Prob.getInstance(k);
		estimateParameters = new CoverageEstimateParameters(0.0, phred2Prob);
		this.baseConfig = baseConfig;
	}

	protected abstract int getCoverage1(final ParallelPileup parallelPileup);
	protected abstract int getCoverage2(final ParallelPileup parallelPileup);
	protected abstract int getCoverageP(final ParallelPileup parallelPileup);
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		//final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		int coverage1 = getCoverage1(parallelPileup);
		int coverage2 = getCoverage2(parallelPileup);
		int coverageP = getCoverageP(parallelPileup);

		final double[][] probs1 = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileups1());
		final double[] alpha1 = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileups1(), coverage1);
		final DirichletDist dirichlet1 = new DirichletDist(alpha1);
		final double density11 = getDensity(dirichlet1, probs1);

		final double[][] probs2 = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileups2());
		final double[] alpha2 = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileups2(), coverage2);
		final DirichletDist dirichlet2 = new DirichletDist(alpha2);
		final double density22 = getDensity(dirichlet2, probs2);

		final Pileup[] pileupsP = parallelPileup.getPileupsP();
		final double[][] probsP = estimateParameters.estimateProbs(baseIs, pileupsP);
		final double[] alphaP = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsP(), coverageP);
		final DirichletDist dirichletP = new DirichletDist(alphaP);
		final double densityP = getDensity(dirichletP, probsP);

		final double z = -2 * (densityP) + 2 * (density11 + density22);

		// only positive values are allowed
		if(z < 0.0 ) {
			return 1.0;
		}
		// todo
		return 1 - dist.cdf(z);
	}

	public static double getDensity(final DirichletDist dirichlet, final double[][] probs) {
		double density = 0.0;

		for(int i = 0; i < probs.length; ++i) {
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}
	
	public boolean filter(double value) {
		return parameters.getThreshold() < value;
	}

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public String getName() {
		return name;
	}

}