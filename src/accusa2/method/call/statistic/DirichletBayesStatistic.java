package accusa2.method.call.statistic;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.BayesEstimateParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class DirichletBayesStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BayesEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	// test what is the best??? 2*k - 2 : k = dimension of modeled prob. vector
	// protected final ChiSquareDist dist = new ChiSquareDist(6);
	
	public DirichletBayesStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.parameters = parameters;

		int k = baseConfig.getK();
		Phred2Prob phred2Prob = Phred2Prob.getInstance(k);
		estimateParameters = new BayesEstimateParameters(1.0, phred2Prob);
		this.baseConfig = baseConfig;
	}

	public double getStatistic(final ParallelPileup parallelPileup) {
		// use all bases for calculation
		final int baseIs[] = {0, 1, 2, 3};
		// use only observed bases per parallelPileup
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample
		// probability matrix for all pileups in sampleA (bases in column, pileups in rows)
		final double[][] probsA = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsA());
		final DirichletDist dirichletA = getDirichlet(baseIs, parallelPileup.getPileupsA());
		final double densityAA = getDensity(baseIs, probsA, dirichletA);

		// second sample - see above
		final double[][] probsB = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsB());
		final DirichletDist dirichletB = getDirichlet(baseIs, parallelPileup.getPileupsB());
		final double densityBB = getDensity(baseIs, probsB, dirichletB);

		// null model - distributions are the same
		final double densityAB = getDensity(baseIs, probsB, dirichletA);
		final double densityBA = getDensity(baseIs, probsA, dirichletB);

		// calculate statistic z = log 0_Model - log A_Model 
		final double z = (densityAA + densityBB) - (densityAB + densityBA);

		/* TODO remove 
		if (z > 30) {
			int j = 0;
			++j;
		}
		*/
		
		// use only positive numbers
		return Math.max(0, z);
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
			density += Math.log10(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

	protected DirichletDist getDirichlet(final int[] baseIs, final Pileup[] pileups) {
		final double[] alpha = estimateParameters.estimateAlpha(baseIs, pileups);
		return new DirichletDist(alpha);
	}

	@Override
	public String getDescription() {
		return "Dirichlet Bayes";
	}

	@Override
	public String getName() {
		return "DirBayes";
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < 0;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletBayesStatistic(baseConfig, parameters);
	}

}