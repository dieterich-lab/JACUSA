package accusa2.estimate;

import java.util.Arrays;

import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class MethodOfMomentsEstimateParameters extends AbstractEstimateParameters {

	private final double initialAlphaNull;

	public MethodOfMomentsEstimateParameters(final double initialAlphaNull, final Phred2Prob phred2Prob) {
		super("mom", "Method of Moments", phred2Prob);
		this.initialAlphaNull = initialAlphaNull;
	}

	@Override
	public double[] estimateAlpha(int[] baseIs, Pileup[] pileups) {
		// use initial alpha to init
		final double[] alpha = new double[baseIs.length];
		if (initialAlphaNull > 0.0) {
			Arrays.fill(alpha, initialAlphaNull / (double)baseIs.length);
		} else {
			Arrays.fill(alpha, 0.0);
		}
		
		for (Pileup pileup : pileups) {
			double[] v = phred2Prob.colSum(baseIs, pileup);
			for(int baseI : baseIs) {
				alpha[baseI] += v[baseI];
			}
		}

		return alpha;
	}

	@Override
	public double[] estimateExpectedProb(int[] baseIs, Pileup[] pileups) {
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