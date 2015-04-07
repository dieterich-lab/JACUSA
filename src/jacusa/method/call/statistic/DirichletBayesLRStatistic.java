package jacusa.method.call.statistic;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.BayesEstimateParameters;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;

@Deprecated
public class DirichletBayesLRStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BayesEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	// test what is the best??? 2*k - 2 : k = dimension of modeled prob. vector
	protected final ChiSquareDist dist = new ChiSquareDist(6);
	
	public DirichletBayesLRStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.parameters = parameters;

		int k = baseConfig.getK();
		Phred2Prob phred2Prob = Phred2Prob.getInstance(k);
		estimateParameters = new BayesEstimateParameters(1.0, phred2Prob);
		this.baseConfig = baseConfig;
	}

	@Override
	public void addStatistic(Result result) {
		final double statistic = getStatistic(result.getParellelPileup());
		if (! filter(statistic)) {
			result.setStatistic(statistic);
		}
	}
	
	public double getStatistic(final ParallelPileup parallelPileup) {
		// use all bases for calculation
		final int baseIs[] = baseConfig.getBasesI();
		// use only observed bases per parallelPileup
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample
		// probability matrix for all pileups in sampleA (bases in column, pileups in rows)
		final double[][] probs1 = estimateParameters.probabilityMatrix(baseIs, parallelPileup.getPileups1());
		final DirichletDist dirichlet1 = getDirichlet(baseIs, parallelPileup.getPileups1());
		final double density1 = getDensity(baseIs, probs1, dirichlet1);

		// second sample - see above
		final double[][] probs2 = estimateParameters.probabilityMatrix(baseIs, parallelPileup.getPileups2());
		final DirichletDist dirichlet2 = getDirichlet(baseIs, parallelPileup.getPileups2());
		final double density2 = getDensity(baseIs, probs2, dirichlet2);

		// null model - distributions are the same
		final double[][] probsP = estimateParameters.probabilityMatrix(baseIs, parallelPileup.getPileupsP());
		final DirichletDist dirichletP = getDirichlet(baseIs, parallelPileup.getPileupsP());
		final double densityP = getDensity(baseIs, probsP, dirichletP);

		// calculate statistic z = log 0_Model - log A_Model 
		final double z = -2 * (densityP - (density1 + density2));

		// use only positive numbers
		return 1 - dist.cdf(z);
	}

	/**
	 * Calculate the density for probs given dirichlet.
	 * @param dirichlet
	 * @param probs
	 * @return
	 */
	protected double getDensity(final int[] baseIs, final double[][] probs, final DirichletDist dirichlet) {
		double density = 0.0;

		// log10 prod = sum log10
		for(int i = 0; i < probs.length; ++i) {
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

	protected DirichletDist getDirichlet(final int[] baseIs, final Pileup[] pileups) {
		final double[] alpha = estimateParameters.estimateAlpha(baseIs, pileups);
		return new DirichletDist(alpha);
	}

	@Override
	public String getDescription() {
		return "Dirichlet - Bayes Likelihood Ratio";
	}

	@Override
	public String getName() {
		return "DirBayesLR";
	}

	@Override
	public boolean filter(double value) {
		return parameters.getThreshold() < value;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletBayesLRStatistic(baseConfig, parameters);
	}

	@Override
	public boolean processCLI(String line) {
		return false;
	}
	
}