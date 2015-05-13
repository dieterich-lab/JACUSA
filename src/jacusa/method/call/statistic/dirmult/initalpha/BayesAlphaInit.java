package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.Pileup;

public class BayesAlphaInit extends AbstractAlphaInit {

	public BayesAlphaInit() {
		super("bayes", "n + alpha");
	}
	
	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages) {
		final double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, 0d);

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				alpha[baseI] += pileupMatrix[pileupI][baseI];
			}
		}
		for (int baseI : baseIs) {
			alpha[baseI] = alpha[baseI] / (double)pileups.length;
		}

		return alpha;
	}

	@Override
	public double[] init(int[] baseIs, 
			Pileup pileup, 
			double[] pileupVector,
			double[] pileupErrorVector, 
			double pileupCoverage) {
		return init(baseIs, new Pileup[]{pileup}, new double[][]{pileupVector}, new double[]{pileupCoverage});
	}

}