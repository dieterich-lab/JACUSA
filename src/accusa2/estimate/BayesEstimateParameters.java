package accusa2.estimate;

import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class BayesEstimateParameters extends AbstractEstimateParameters {

	private final double[] alpha;

	public BayesEstimateParameters(final double[] alpha, final Phred2Prob phred2Prob) {
		super("bayes", "Bayes estimate (n + alpha)", phred2Prob);
		this.alpha = alpha;
	}

	@Override
	public double[] estimateAlpha(int[] baseIs, Pileup[] pileups) {
		// use initial alpha to init
		final double[] alphas = alpha.clone();

		for (Pileup pileup : pileups) {
			double[] v = phred2Prob.colSum(baseIs, pileup);
			for(int baseI : baseIs) {
				alphas[baseI] += v[baseI];
			}
		}

		return alphas;
	}

	@Override
	public double[] estimateExpectedValue(int[] baseIs, Pileup[] pileups) {
		double[] expectedValue = new double[baseIs.length];

		int replicates = pileups.length;
		double[][] probs = estimateProbs(baseIs, pileups);
		for (int pileupI = 0; pileupI < replicates; ++pileupI) {
			for (int baseI : baseIs) {
				expectedValue[baseI] += probs[pileupI][baseI];
			}
		}
		if (replicates > 1) {
			for (int baseI : baseIs) {
				expectedValue[baseI] /= (double)replicates;
			}
		}

		return expectedValue;
	}

	@Override
	public double[][] estimateProbs(int[] baseIs, Pileup[] pileups) {
		final double[][] probs = new double[pileups.length][baseIs.length];

		for(int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			// sum the probabilities giving alpha 
			probs[pileupI] = phred2Prob.colMean(baseIs, pileups[pileupI]);
		}

		return probs;
	}
}