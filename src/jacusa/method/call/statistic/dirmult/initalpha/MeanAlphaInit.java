package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;

public class MeanAlphaInit extends AbstractAlphaInit {

	public MeanAlphaInit() {
		super("mean", "alpha = mean * n * p * q");
	}

	@Override
	public AbstractAlphaInit newInstance(String line) {
		return new MeanAlphaInit();
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix) {
		final double[] alpha = new double[BaseConfig.VALID.length];
		final double[] mean = new double[BaseConfig.VALID.length];

		double total = 0.0;
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				mean[baseI] += pileupMatrix[pileupI][baseI];
				total += pileupMatrix[pileupI][baseI];
			}
		}

		for (int baseI : baseIs) {
			mean[baseI] /= total;
			alpha[baseI] = mean[baseI];
		}

		return alpha;
	}

	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector) {
		return init(baseIs, new Pileup[]{pileup}, new double[][]{pileupVector});
	}
	
}
