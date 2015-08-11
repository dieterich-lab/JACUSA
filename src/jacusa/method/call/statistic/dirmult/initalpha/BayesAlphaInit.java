package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.Pileup;

public class BayesAlphaInit extends AbstractAlphaInit {

	public BayesAlphaInit() {
		super("bayes", "n + alpha");
	}

	@Override
	public AbstractAlphaInit newInstance(String line) {
		return new BayesAlphaInit();
	}
	
	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup[] pileups,
			final double[][] pileupMatrix) {
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
	public double[] init(
			final int[] baseIs,
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector) {
		return init(baseIs, new Pileup[]{pileup}, new double[][]{pileupVector});
	}

}