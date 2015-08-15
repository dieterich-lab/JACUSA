package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;

public class RonningAlphaInit extends AbstractAlphaInit {

	private double minVariance;
	private int maxAlphaNull;
	
	public RonningAlphaInit() {
		// this(Math.pow(10, -6), Integer.MAX_VALUE);
		this(Math.pow(10, -6), 100);
	}

	public RonningAlphaInit(double minVariance, int maxAlphaNull) {
		super("Ronning", "See Ronning 1989");
		this.minVariance = minVariance;
		this.maxAlphaNull = maxAlphaNull;
	}

	@Override
	public AbstractAlphaInit newInstance(String line) {
		return new RonningAlphaInit(minVariance, maxAlphaNull);
	}
	
	void setMinVariance(double minVariacnce) {
		this.minVariance = minVariacnce;
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix) {

		// number of pileups
		int baseLength = BaseConfig.VALID.length;
		double[] pileupCoverages = getCoverages(baseIs, pileupMatrix);
		
		// calculate pileup proportion matrix
		final double[][] pileupProportionMatrix = new double[pileups.length][baseLength];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] = pileupMatrix[pileupI][baseI] / pileupCoverages[pileupI];
			}
		}

		// calculate mean
		double[] mean = new double[baseLength];
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
		double[] alpha = new double[baseLength];
		Arrays.fill(alpha, 0.0);

		// variance of called bases
		double[] variance = new double[baseLength];
		// init
		Arrays.fill(variance, 0.0);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : bases) {
				variance[baseI] += Math.pow(pileupProportionMatrix[pileupI][baseI] - mean[baseI], 2d);
			}
		}

		// Ronning 1989 to set Method Of Moments
		double[] values = new double[baseLength];
		for (int i = 0; i < baseIs.length; ++i) {
			int baseI = baseIs[i];
			variance[baseI] /= (double)(pileups.length - 1);
			// fix if variance to small
			/*
			if (variance[baseI] > minVariance) {
				// variance[baseI] = minVariance;
				double v = mean[baseI] * (1d - mean[baseI]) / variance[baseI] - 1d;
				if (v > 0) {
					values[i] = v;
				}
			}*/

			if (variance[baseI] < minVariance) {
				variance[baseI] = minVariance;
			}
			double v = mean[baseI] * (1d - mean[baseI]) / variance[baseI] - 1d;
			if (v > 0) {
				values[i] = v;
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
		// of alphaNull is not defined 
		if (alphaNull == 0.0) {
			alphaNull = 10d;
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
			final double[] pileupErrorVector) {
		return init(baseIs, new Pileup[]{pileup}, new double[][]{pileupVector});
	}

}