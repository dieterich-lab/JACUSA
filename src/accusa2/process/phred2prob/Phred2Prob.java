package accusa2.process.phred2prob;

import java.util.Arrays;

import accusa2.pileup.BaseConfig;
import accusa2.pileup.Pileup;

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

	public double[] colSumCount(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] c = new double[bases.length];

		for(int baseI = 0; baseI < bases.length; ++baseI) {
			final int count = pileup.getCounts().getBaseCount(baseI);
			c[baseI] = count;
		}
		return c;		
	}
	
	/**
	 * Calculate a probability vector P for the pileup. |P| = |bases| 
	 */
	public double[] colSum(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = new double[bases.length];

		for(int baseI = 0; baseI < bases.length; ++baseI) {
			for(byte qual = 0 ; qual < Phred2Prob.MAX_Q; ++qual) {
				// number of bases with specific quality 
				final int count = pileup.getCounts().getQualCount(bases[baseI], qual);
				if(count > 0) {
					final double baseP = convert2P(qual);
					p[baseI] += count * baseP;

					final double errorP = convert2errorP(qual) / (bases.length - 1);
					// distribute error probability
					for(int i = 0; i < baseI; ++i) {
						p[i] += count * errorP;
					}
					for(int i = baseI + 1; i < bases.length; ++i) {
						p[i] += count * errorP;
					}
				}
			}
		}
		return p;
	}

	public double[] colErrorSum(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = new double[bases.length];

		for (int baseI = 0; baseI < bases.length; ++baseI) {
			for (byte qual = 0 ; qual < Phred2Prob.MAX_Q; ++qual) {
				// number of bases with specific quality 
				final int count = pileup.getCounts().getQualCount(bases[baseI], qual);

				if (count > 0) {
					final double errorP = convert2errorP(qual) / (double)(bases.length - 1);

					// distribute error probability
					for (int i = 0; i < baseI; ++i) {
						p[i] += count * errorP;
					}
					for (int i = baseI + 1; i < bases.length; ++i) {
						p[i] += count * errorP;
					}
				}
			}
		}
		return p;		
	}
	public double[] colErrorMean(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = colErrorSum(bases, pileup);
		
		for (int baseI = 0; baseI < bases.length; ++baseI) {
			p[baseI] /= (double)pileup.getCoverage();
		}
		
		return p;
	}

	public double[] colMean(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = colSum(bases, pileup);
		
		for(int baseI = 0; baseI < bases.length; ++baseI) {
			p[baseI] /= (double)pileup.getCoverage();
		}
		
		return p;
	}
	
	public double[] getPileupsMean(int[] basesI, Pileup[] pileups) {
		double[] totalMean = new double[basesI.length];
		Arrays.fill(totalMean, 0.0);
	
		for (Pileup pileup : pileups) {
			double[] pileupMean = colMean(basesI, pileup);
			for (int baseI : basesI) {
				totalMean[baseI] += pileupMean[baseI];
			}
		}
		double n = pileups.length;
		for (int baseI : basesI) {
			totalMean[baseI] /= (double)n;
		}
	
		return totalMean;
	}

	public double[] getPileupsVariance(int[] basesI, double[] totalMean, Pileup[] pileups) {
		double[] totalVariance = new double[basesI.length];
		Arrays.fill(totalVariance, 0.0);
	
		for (Pileup pileup : pileups) {
			double[] pileupMean = colMean(basesI, pileup);
			for (int baseI : basesI) {
				totalVariance[baseI] +=  Math.pow(totalMean[baseI] - pileupMean[baseI], 2.0); 
			}
		}
		double n = pileups.length;
		for (int baseI : basesI) {
			totalMean[baseI] /= (double)(n - 1);
		}
	
		return totalVariance;
	}

	public static Phred2Prob getInstance(int n) {
		if (singles[n] == null) {
			singles[n] = new Phred2Prob(n);
		}

		return singles[n];
	}
	
}