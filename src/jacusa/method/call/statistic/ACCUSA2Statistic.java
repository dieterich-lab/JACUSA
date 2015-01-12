package jacusa.method.call.statistic;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.BayesEstimateParameters;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.process.phred2prob.Phred2Prob;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;

/**
 * 
 * @author michael
 * 
 * Uses the matching coverage to calculate the test-statistic.
 * Tested if distributions are equal.
 * Same as in ACCUSA2 paper
 */
public class ACCUSA2Statistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BayesEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	public ACCUSA2Statistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;

		int k = baseConfig.getK();

		Phred2Prob phred2Prob = Phred2Prob.getInstance(k);
		estimateParameters = new BayesEstimateParameters(0.0, phred2Prob);
		this.baseConfig = baseConfig;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new ACCUSA2Statistic(baseConfig, parameters);
	}

	public double getStatistic(final ParallelPileup parallelPileup) {
		// use all bases for calculation
		final int baseIs[] = {0, 1, 2, 3};
		// use only observed bases per parallelPileup

		// first sample
		// probability matrix for all pileups in sampleA (bases in column, pileups in rows)
		final double[][] probsA = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileups1());
		final DirichletDist dirichletA = getDirichlet(baseIs, parallelPileup.getPileups1());
		final double densityAA = getDensity(baseIs, probsA, dirichletA);

		// second sample - see above
		final double[][] probsB = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileups2());
		final DirichletDist dirichletB = getDirichlet(baseIs, parallelPileup.getPileups2());
		final double densityBB = getDensity(baseIs, probsB, dirichletB);

		// null model - distributions are the same
		final double densityAB = getDensity(baseIs, probsB, dirichletA);
		final double densityBA = getDensity(baseIs, probsA, dirichletB);

		// calculate statistic z = log 0_Model - log A_Model 
		final double z = (densityAA + densityBB) - (densityAB + densityBA);

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
	public boolean filter(double value) {
		return parameters.getStat() > value;
	}

	@Override
	public String getDescription() {
		return "ACCUSA2 statistic: Z=log10( Dir(alpha_A; phi_A) * Dir(alpha_B; phi_B) ) - log10( Dir(alpha_A; phi_B) * Dir(alpha_B; phi_A) )";
	}

	@Override
	public String getName() {
		return "ACCUSA2";
	}
	
}