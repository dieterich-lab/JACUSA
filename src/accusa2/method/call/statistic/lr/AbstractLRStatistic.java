package accusa2.method.call.statistic.lr;

import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.coverage.CoverageEstimateParameters;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.method.call.statistic.StatisticUtils;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public abstract class AbstractLRStatistic implements StatisticCalculator {

	private String name;
	private String desc;

	protected final StatisticParameters parameters;
	protected final CoverageEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	// test what is the best??? 2*k - 2 : k = dimension of modeled prob. vector
	// protected final ChiSquareDist dist = new ChiSquareDist(6);
	
	public AbstractLRStatistic(String name, String desc, BaseConfig baseConfig, StatisticParameters parameters) {
		this.parameters = parameters;

		int k = baseConfig.getK();
		double[] alpha = new double[k];
		Arrays.fill(alpha, 0.0);

		Phred2Prob phred2Prob = Phred2Prob.getInstance(k);
		estimateParameters = new CoverageEstimateParameters(alpha, phred2Prob);
		this.baseConfig = baseConfig;
	}

	protected abstract int getCoverageA(final ParallelPileup parallelPileup);
	protected abstract int getCoverageB(final ParallelPileup parallelPileup);
	protected abstract int getCoverageP(final ParallelPileup parallelPileup);
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		//final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		int coverageA = getCoverageA(parallelPileup);
		int coverageB = getCoverageB(parallelPileup);
		int coverageP = getCoverageP(parallelPileup);

		final double[][] probsA = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsA());
		final double[] alphaA = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsA(), coverageA);
		final DirichletDist dirichletA = new DirichletDist(alphaA);
		final double densityAA = StatisticUtils.getDensity(dirichletA, probsA);

		final double[][] probsB = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsB());
		final double[] alphaB = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsB(), coverageB);
		final DirichletDist dirichletB = new DirichletDist(alphaB);
		final double densityBB = StatisticUtils.getDensity(dirichletB, probsB);

		final Pileup[] pileupsP = parallelPileup.getPileupsP();
		final double[][] probsP = estimateParameters.estimateProbs(baseIs, pileupsP);
		final double[] alphaP = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsP(), coverageP);
		final DirichletDist dirichletP = new DirichletDist(alphaP);
		final double densityP = StatisticUtils.getDensity(dirichletP, probsP);

		final double z = -2 * (densityP) + 2 * (densityAA + densityBB);

		// only positive values are allowed
		if(z < 0.0 ) {
			return 1.0;
		}
		// todo
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
		return desc;
	}

	@Override
	public String getName() {
		return name;
	}

}