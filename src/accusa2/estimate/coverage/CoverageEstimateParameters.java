package accusa2.estimate.coverage;

import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class CoverageEstimateParameters extends AbstractCoverageEstimateParameters {

	private final double[] alpha;

	public CoverageEstimateParameters(double[] alpha, Phred2Prob phred2Prob) {
		super("coverage", "Coverage bases", phred2Prob);
		this.alpha = alpha;
	}
	
	@Override
	public double[] estimateAlpha(int[] baseIs, Pileup[] pileups) {
		int coverage = 0;
		for (Pileup pileup : pileups) {
			coverage += pileup.getCoverage();
		}
		return estimateAlpha(baseIs, pileups, coverage);
	}

	public double[] estimateAlpha(int[] baseIs, Pileup[] pileups, int coverage) {
		final double[] alphas = new double[baseIs.length];
		System.arraycopy(alpha, 0, alphas, 0, baseIs.length);
		
		for (Pileup pileup : pileups) {
			double[] probVector = phred2Prob.colMean(baseIs, pileup);
			// INFO: coverage and pileup.getCoverage() may be different.
			// e.g.: when replicates are available...
			// (double)coverage * 
			for(int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphas[baseI] = probVector[baseI] * (double)coverage;
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

	public int getMeanCoverage(Pileup[] pileups) {
		if (pileups.length == 1) { 
			return pileups[0].getCoverage();
		}

		int coverage = 0;
		for (Pileup pileup : pileups) {
			coverage += pileup.getCoverage();
		}
		return (int)Math.round((double)coverage / (double)pileups.length);
	}
	
}