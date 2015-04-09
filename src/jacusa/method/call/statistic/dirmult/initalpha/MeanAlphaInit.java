package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public class MeanAlphaInit extends AbstractAlphaInit {

	public MeanAlphaInit() {
		super("mean", "alpha = mean(pileups)");
	}
	
	@Override
	public double[] init(int[] baseIs, Pileup[] pileups,
			double[][] pileupMatrix, double[] pileupCoverages) {
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

}
