package jacusa.phred2prob;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;

public final class Phred2Prob {

	private final double[] phred2errerP;
	private final double[] phred2baseP;
	private final double[] phred2baseErrorP;

	// phred capped at 41
	public static final int MAX_Q = 41 + 1; // some machines give phred score of 60 -> Prob of error: 10^-6 ?!
	private static Phred2Prob[] singles = new Phred2Prob[BaseConfig.VALID.length + 1];

	private Phred2Prob(int n) {
		// pre-calculate probabilities
		final int min = 0;
		phred2errerP = new double[MAX_Q];
		phred2baseP = new double[MAX_Q];
		phred2baseErrorP = new double[MAX_Q];

		for(int i = min; i < MAX_Q; i++) {
			phred2errerP[i] = Math.pow(10.0, -(double)i / 10.0);
			phred2baseP[i] = 1.0 - phred2errerP[i];
			phred2baseErrorP[i] = phred2errerP[i] / (n - 1); // ignore the called base
		}
	}

	public double convert2errorP(byte qual) {
		qual =  qual > MAX_Q ? MAX_Q : qual; 
		return phred2errerP[qual];
	}

	public double convert2P(byte qual) {
		qual =  qual > MAX_Q ? MAX_Q : qual;
		return phred2baseP[qual];
	}
	
	public double convert2perEntityP(byte qual) {
		qual =  qual > MAX_Q ? MAX_Q : qual;
		return phred2baseErrorP[qual];
	}
	
	public double getErrorP(byte qual) {
		qual =  qual > MAX_Q ? MAX_Q : qual;
		return phred2errerP[qual];
	}

	public double[] colSumCount(final int[] baseIs, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] c = new double[BaseConfig.VALID.length];

		for (int baseI : baseIs) {
			final int count = pileup.getCounts().getBaseCount(baseI);
			c[baseI] = count;
		}
		return c;		
	}

	/**
	 * Calculate a probability vector P for the pileup. |P| = |bases| 
	 */
	public double[] colSumProb(final int[] baseIs, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = new double[BaseConfig.VALID.length];
		Arrays.fill(p, 0.0);

		for (int baseI : baseIs) {
			for (byte qual = 0 ; qual < Phred2Prob.MAX_Q; ++qual) {
				// number of bases with specific quality 
				final int count = pileup.getCounts().getQualCount(baseI, qual);
				if (count > 0) {
					final double baseP = convert2P(qual);
					p[baseI] += (double)count * baseP;

					final double errorP = convert2errorP(qual) / (baseIs.length - 1);
					// distribute error probability
					for (int baseI2 : baseIs) {
						if (baseI2 != baseI) {
							p[baseI2] += (double)count * errorP;
						}
					}
				}
			}
		}
		
		return p;
	}

	public double[] colSumErrorProb(final int[] baseIs, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = new double[BaseConfig.VALID.length];
		Arrays.fill(p, 0.0);

		for (int baseI : baseIs) {
			for (byte qual = 0 ; qual < Phred2Prob.MAX_Q; ++qual) {
				// number of bases with specific quality 
				final int count = pileup.getCounts().getQualCount(baseI, qual);

				if (count > 0) {
					final double errorP = convert2errorP(qual) / (double)(baseIs.length - 1);

					// distribute error probability
					for (int baseI2 : baseIs) {
						if (baseI2 != baseI) {
							p[baseI2] += (double)count * errorP;
						}
					}
				}
			}
		}
		return p;		
	}

	public double[] colMeanErrorProb(final int[] baseIs, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = colSumErrorProb(baseIs, pileup);
		
		for (int baseI : baseIs) {
			p[baseI] /= (double)pileup.getCoverage();
		}
		
		return p;
	}

	public double[] colMeanProb(final int[] baseIs, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = colSumProb(baseIs, pileup);
		double sum = MathUtil.sum(p);

		for(int baseI : baseIs) {
			p[baseI] /= sum;
		}
		
		return p;
	}
	
	public double[] getPileupsMeanProb(int[] baseIs, Pileup[] pileups) {
		double[] totalMean = new double[BaseConfig.VALID.length];
		Arrays.fill(totalMean, 0.0);

		for (Pileup pileup : pileups) {
			double[] pileupMean = colMeanProb(baseIs, pileup);
			for (int baseI : baseIs) {
				totalMean[baseI] += pileupMean[baseI];
			}
		}
		double n = pileups.length;
		for (int baseI : baseIs) {
			totalMean[baseI] /= (double)n;
		}
	
		return totalMean;
	}
	
	public double[] getPileupsVarianceProb(int[] baseIs, double[] totalMean, Pileup[] pileups) {
		double[] totalVariance = new double[BaseConfig.VALID.length];
		Arrays.fill(totalVariance, 0.0);

		for (Pileup pileup : pileups) {
			double[] pileupMean = colMeanProb(baseIs, pileup);
			for (int baseI : baseIs) {
				totalVariance[baseI] +=  Math.pow(totalMean[baseI] - pileupMean[baseI], 2.0); 
			}
		}
		double n = pileups.length;
		for (int baseI : baseIs) {
			totalMean[baseI] /= (double)(n - 1);
		}
	
		return totalVariance;
	}

	public static Phred2Prob getInstance(int baseCount) {
		if (singles[baseCount] == null) {
			singles[baseCount] = new Phred2Prob(baseCount);
		}

		return singles[baseCount];
	}

	public double[] meanPileupError(final int[] baseIs, final Pileup[] pileups) {
		double[] totalError = new double[BaseConfig.VALID.length];
		Arrays.fill(totalError, 0.0);

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];
			double[] pileupError = colSumErrorProb(baseIs, pileup);

			for (int baseI : baseIs) {
				totalError[baseI] += pileupError[baseI] / (double)pileup.getCoverage();
			}
		}
		return totalError;
	}
	
	public double[] pooledPileupErrorProb(final int[] baseIs, final Pileup[] pileups) {
		double[] totalError = new double[BaseConfig.VALID.length];
		Arrays.fill(totalError, 0.0);

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			double[] pileupError = colSumErrorProb(baseIs, pileups[pileupI]);

			for (int baseI : baseIs) {
				totalError[baseI] += pileupError[baseI];
			}
		}
		return totalError;
	}

}