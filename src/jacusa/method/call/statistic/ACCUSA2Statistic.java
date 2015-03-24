package jacusa.method.call.statistic;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.BayesEstimateParameters;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
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

		final int k = baseConfig.getK();

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
		final int baseIs[] = baseConfig.getBasesI();
		// use only observed bases per parallelPileup

		// first sample
		// probability matrix for all pileups in sampleA (bases in column, pileups in rows)
		final double[][] probs1 = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileups1());
		final DirichletDist dirichlet1 = getDirichlet(baseIs, parallelPileup.getPileups1());
		final double density11 = getDensity(baseIs, probs1, dirichlet1);

		// second sample - see above
		final double[][] probs2 = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileups2());
		final DirichletDist dirichlet2 = getDirichlet(baseIs, parallelPileup.getPileups2());
		final double density22 = getDensity(baseIs, probs2, dirichlet2);

		// null model - distributions are the same
		final double density12 = getDensity(baseIs, probs2, dirichlet1);
		final double density21 = getDensity(baseIs, probs1, dirichlet2);

		// calculate statistic z = log 0_Model - log A_Model 
		final double z = (density11 + density22) - (density12 + density21);

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
		return parameters.getMaxStat() > value;
	}

	@Override
	public String getDescription() {
		return "ACCUSA2 statistic: Z=log10( Dir(alpha_A; phi_A) * Dir(alpha_B; phi_B) ) - log10( Dir(alpha_A; phi_B) * Dir(alpha_B; phi_A) )";
	}

	@Override
	public String getName() {
		return "ACCUSA2";
	}

	@Override
	public void processCLI(String line) {
		// nothing to be done
	}
	
}