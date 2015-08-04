package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.Pileup;

public class MinAlphaInit extends AbstractAlphaInit {

	public MinAlphaInit() {
		super("mean", "alpha = mean * n * p * q");
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages) {
		final double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, Double.MAX_VALUE);
		
		double[][] pileupProportionMatrix = new double[pileups.length][baseIs.length];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] = pileupMatrix[pileupI][baseI] / pileupCoverages[pileupI];
				alpha[baseI] = Math.min(alpha[baseI], pileupProportionMatrix[pileupI][baseI]);
			}
		}

		return alpha;
	}

	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector,
			final double pileupCoverage) {
		return init(baseIs, new Pileup[]{pileup}, new double[][]{pileupVector}, new double[]{pileupCoverage});
	}
	
}
