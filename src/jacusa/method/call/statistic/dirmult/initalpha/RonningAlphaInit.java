package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.Pileup;

public class RonningAlphaInit extends AbstractAlphaInit {

	final private double minVariance = Math.pow(10, -6);

	public RonningAlphaInit() {
		super("Roning", "See Ronning 1989");
	}
	
	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages
			) {

		final double[][] pileupProportionMatrix = new double[pileups.length][baseIs.length];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] = pileupMatrix[pileupI][baseI] / pileupCoverages[pileupI];
			}
		}
		
		// init
		double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, 0d);

		double[] mean = new double[baseIs.length];
		Arrays.fill(mean, 0d);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				mean[baseI] += pileupProportionMatrix[pileupI][baseI];
			}
		}
		for (int baseI : baseIs) {
			mean[baseI] /= (double)(pileups.length);
		}
		
		double[] variance = new double[baseIs.length];
		Arrays.fill(variance, 0d);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				variance[baseI] += Math.pow(pileupProportionMatrix[pileupI][baseI] - mean[baseI], 2d);
			}
		}

		// Ronning 1989 to set Method Of Moments
		double[] tmp = new double[baseIs.length];
		for (int i = 0; i < baseIs.length; ++i) {
			int baseI = baseIs[i];
			if (pileups.length - 1 == 0) {
				variance[baseI] = minVariance;
			} else {
				variance[baseI] /= (double)(pileups.length - 1);
				if (variance[baseI] < minVariance) {
					variance[baseI] = minVariance;
				}
			}

			double v = mean[baseI] * (1d - mean[baseI]) / variance[baseI] - 1d;
			if (v > 0) {
				tmp[i] = Math.log(v);
			} else {
				tmp[i] = 0.0;
			}
		}
		Arrays.sort(tmp);
		double alphaNull = 0.0; 
		for (int i = 0; i < baseIs.length -1; ++i) {
			alphaNull += tmp[i];
		}
		
		alphaNull /= (double)(baseIs.length - 1);
		alphaNull = Math.exp(alphaNull);

		for (int baseI : baseIs) {
			alpha[baseI] = mean[baseI] * alphaNull;
		}

		return alpha;
	}

	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup pileup,
			final double[] pileupVector,
			final double[] pileupErrorVector,
			final double pileupCoverage
			) {
		// TODO
		return null;
	}
	
}
