package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.Pileup;

public class RonningAlphaInit extends AbstractAlphaInit {

	final private double minVariance = 0.00001;
	
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
		double alphaNull = Double.MAX_VALUE;
		for (int baseI : baseIs) {
			variance[baseI] /= (double)(pileups.length - 1);
			if (variance[baseI] < minVariance) {
				variance[baseI] = minVariance;
			}
			
			int k = baseIs.length;
			double alphaNullTmp = 1.0;
			for (int baseI2 : baseIs) {
				if (baseI == baseI2) {
					continue;
				}
				alphaNullTmp *= mean[baseI] * (1d - mean[baseI]) / variance[baseI] - 1d;
			}
			if (alphaNullTmp > 0 && k >= 2) {
				alphaNullTmp = Math.pow(alphaNullTmp, 1d / (double)(k - 1));
				alphaNull = Math.min(alphaNull, alphaNullTmp);
			}
		}

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
