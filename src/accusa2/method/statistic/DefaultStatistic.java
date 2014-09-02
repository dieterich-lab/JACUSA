package accusa2.method.statistic;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

/**
 * 
 * @author michael
 * 
 * Uses the matching coverage to calculate the test-statistic.
 * Tested if distributions are equal.
 */
public class DefaultStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected final Phred2Prob phred2Prob;

	public DefaultStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		this.baseConfig = baseConfig;
		this.parameters = parameters;
		phred2Prob 		= Phred2Prob.getInstance(baseConfig.getBases().length);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DefaultStatistic(baseConfig, parameters);
	}

	/**
	 * Estimate coverage for pileup(s) of a sample by mean
	 * @param pileups
	 * @return
	 */
	protected int getMeanCoverage(Pileup[] pileups) {
		if(pileups.length == 1) { 
			return pileups[0].getCoverage();
		}

		int coverage = 0;
		for(Pileup pileup : pileups) {
			coverage += pileup.getCoverage();
		}
		return (int)Math.round((double)coverage / (double)pileups.length);
	}

	public double getStatistic(final ParallelPileup parallelPileup) {
		// use all bases for calculation
		final int bases[] = {0, 1, 2, 3};
		// use only observed bases per parallelPileup
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample
		// mean coverage for pileups for sample1
		final int coverage1 = getMeanCoverage(parallelPileup.getPileupsA());
		// probability matrix for all pileups in sample1 (bases in column, pileups in rows)
		final double[][] probs1 = getPileup2Probs(bases, parallelPileup.getPileupsA());
		// estimate alpha for by pooling pileups in sample1 alpha = alphaP * avg.cov / covP  
		final double[] alpha1 = estimateAlpha(bases, parallelPileup.getPooledPileupA(), coverage1);
		final DirichletDist dirichlet1 = new DirichletDist(alpha1);
		final double density11 = getDensity(dirichlet1, probs1);

		// second sample - see above
		final int coverage2 = getMeanCoverage(parallelPileup.getPileupsB());
		final double[][] probs2 = getPileup2Probs(bases, parallelPileup.getPileupsB());
		final double[] alpha2 = estimateAlpha(bases, parallelPileup.getPooledPileupB(), coverage2);
		final DirichletDist dirichlet2 = new DirichletDist(alpha2);
		final double density22 = getDensity(dirichlet2, probs2);

		// null model - distributions are the same
		final double density12 = getDensity(dirichlet1, probs2);
		final double density21 = getDensity(dirichlet2, probs1);

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
	protected double getDensity(final DirichletDist dirichlet, final double[][] probs) {
		double density = 0.0;

		// log10 prod = sum log10
		for(int i = 0; i < probs.length; ++i) {
			density += Math.log10(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() > value;
	}
	
	/**
	 * 
	 * @param bases
	 * @param probs
	 * @param coverage
	 * @return
	protected double[] estimateAlpha(final int[] bases, final double probs[][], final int coverage) {
		double[] prob = new double[bases.length];

		for(int i = 0; i < probs.length; ++i) {
			for(int j = 0; j < bases.length; ++j) {
				prob[j] += probs[i][j];
			}
		}

		for(int i = 0; i < bases.length; ++i) {
			prob[i] = prob[i] / (double)probs.length * (double)coverage;
		}

		return prob;
	}
		 */
	
	/**
	 * Accumulates from each pileup of a sample
	 * @param bases
	 * @param pileups
	 * @return
	protected double[] estimateAlpha(final int bases[], final Pileup[] pileups) {
		final double[] alphas = new double[bases.length];

		for(Pileup pileup : pileups) {
			double[] alpha = pileup2Matrix.calculate(bases, pileup);
			for(int i = 0; i < bases.length; ++i) {
				alphas[i] += alpha[i];
			}
		}
		
		return alphas;
	}
		 */

	/**
	 * Estimates alpha(s) of Dirichlet by alpha_j = alpha_i / avg.cov * cov_i 
	 * @param bases
	 * @param pileup
	 * @param coverage
	 * @return
	 */
	protected double[] estimateAlpha(final int bases[], final Pileup pileup, int coverage) {
		final double[] alphas = new double[bases.length];

		double[] probVector = phred2Prob.colMean(bases, pileup);
		// INFO: coverage and pileup.getCoverage() may be different.
		// e.g.: when replicates are available...
		for(int baseI = 0; baseI < bases.length; ++baseI) {
			alphas[baseI] = (double)coverage * probVector[baseI] / (double)pileup.getCoverage();
		}

		return alphas;
	}
	
	/**
	 * Calculate the probability matrix M based on P_i = alpha_i / cov_i:
	 * M = BASES (A,C,G,T)
	 * pileupI_1
	 * ...
	 * pileupI_i
	 * ...
	 * pileupI_n
	 * 
	 * @param bases
	 * @param pileups
	 * @return
	 */
	protected double[][] getPileup2Probs(final int bases[], final Pileup[] pileups) {
		final double[][] probs = new double[pileups.length][bases.length];

		for(int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			// sum the probabilities giving alpha 
			probs[pileupI] = phred2Prob.colMean(bases, pileups[pileupI]);
		}

		return probs;
	}

	@Override
	public String getDescription() {
		return "Default statistic: Z=log10( Dir(alpha_A; phi_A) * Dir(alpha_B; phi_B) ) - log10( Dir(alpha_A; phi_B) * Dir(alpha_B; phi_A) )";
	}

	@Override
	public String getName() {
		return "default";
	}

	public StatisticParameters getParameters() {
		return parameters;
	}
	
	public BaseConfig getBaseConfig() {
		return baseConfig;
	}
	
}