package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public class MeanAlphaInit extends AbstractAlphaInit {

	public MeanAlphaInit() {
		super("mean", "alpha = mean * n * p * q");
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages) {
		final double[] alpha = new double[baseIs.length];
		final double[] mean = new double[baseIs.length];

		double[][] pileupProportionMatrix = new double[pileups.length][baseIs.length];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] = pileupMatrix[pileupI][baseI] / pileupCoverages[pileupI];
			}
		}
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] /= pileupCoverages[pileupI];
				mean[baseI] += pileupProportionMatrix[pileupI][baseI];
			}
		}
		for (int baseI : baseIs) {
			mean[baseI] /= (double)(pileups.length);
			alpha[baseI] = mean[baseI];
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
