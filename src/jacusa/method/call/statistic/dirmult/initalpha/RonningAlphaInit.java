package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jacusa.pileup.Pileup;

public class RonningAlphaInit extends AbstractAlphaInit {

	private double minVariance;
	private int maxAlphaNull;
	
	public RonningAlphaInit() {
		this(Math.pow(10, -10), Integer.MAX_VALUE);
	}

	public RonningAlphaInit(double minVariance, int maxAlphaNull) {
		super("Roning", "See Ronning 1989");
		this.minVariance = minVariance;
		this.maxAlphaNull = maxAlphaNull;
	}

	void setMinVariance(double minVariacnce) {
		this.minVariance = minVariacnce;
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages
			) {

		// number of pileups
		int n = pileupMatrix[0].length;

		// calculate pileup proportion matrix
		final double[][] pileupProportionMatrix = new double[pileups.length][n];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] = pileupMatrix[pileupI][baseI] / pileupCoverages[pileupI];
			}
		}

		// calculate mean
		double[] mean = new double[n];
		double min = Double.MAX_VALUE;

		Set<Integer> bases = new HashSet<Integer>(baseIs.length);
		Arrays.fill(mean, 0d);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				if (pileups[pileupI].getCounts().getBaseCount(baseI) > 0) {
					bases.add(baseI);
				}
				mean[baseI] += pileupProportionMatrix[pileupI][baseI];
				min = Math.min(pileupProportionMatrix[pileupI][baseI], min);
			}
		}
		for (int baseI : baseIs) {
			mean[baseI] /= (double)(pileups.length);
		}
		
		// init alpha with min.
		double[] alpha = new double[n];
		Arrays.fill(alpha, 0.0);

		// variance of called bases
		double[] variance = new double[baseIs.length];
		// init
		Arrays.fill(variance, 0.0);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : bases) {
				variance[baseI] += Math.pow(pileupProportionMatrix[pileupI][baseI] - mean[baseI], 2d);
			}
		}

		// Ronning 1989 to set Method Of Moments
		double[] values = new double[baseIs.length];
		for (int i = 0; i < baseIs.length; ++i) {
			int baseI = baseIs[i];
			variance[baseI] /= (double)(pileups.length - 1);
			// fix if variance to small
			if (variance[baseI] > minVariance) {
				// variance[baseI] = minVariance;
				double v = mean[baseI] * (1d - mean[baseI]) / variance[baseI] - 1d;
				if (v > 0) {
					values[i] = v;
				} else {
					values[i] = 0.0;
				}
			}
		}

		Arrays.sort(values);
		// Ronning 1989 to set Method Of Moments
		double alphaNull = 0.0;
		for (int i = 0; i < baseIs.length - 1; ++i) {
			if (values[i] > 0.0) {
				if (alphaNull == 0.0) {
					alphaNull = values[i];
				} else {
					alphaNull *= values[i];
				}
			}
		}

		if (alphaNull > maxAlphaNull) {
			alphaNull = maxAlphaNull;
		}

		alphaNull = Math.pow(alphaNull, 1d / (double)(baseIs.length - 1));
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
		return null;
	}

}