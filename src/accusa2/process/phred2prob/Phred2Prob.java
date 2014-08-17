package accusa2.process.phred2prob;

import accusa2.pileup.Pileup;

public final class Phred2Prob {

	private final double[] phred2errerP;
	private final double[] phred2baseP;
	private final double[] phred2baseErrorP;

	// FIXME
	public static final int MAX_Q = 61; // some machines give phred score of 60 -> Prob of error: 10^-6 ?!
	public static final int MAX_Q2 = 41; // Illumina style

	// TODO add static method
	
	public Phred2Prob() {
		this(Pileup.LENGTH);
	}

	public Phred2Prob(int n) {
		// pre-calculate probabilities
		final int min = 0;
		phred2errerP = new double[MAX_Q2];
		phred2baseP = new double[MAX_Q2];
		phred2baseErrorP = new double[MAX_Q2];

		for(int i = min; i <= MAX_Q2; i++) {
			phred2errerP[i] = Math.pow(10.0, -(double)i / 10.0);
			phred2baseP[i] = 1.0 - phred2errerP[i];
			phred2baseErrorP[i] = phred2errerP[i] / (n - 1); // ignore the called base
		}
	}

	public double convert2errorP(byte qual) {
		qual =  qual > MAX_Q2 ? MAX_Q2 : qual; 
		return phred2errerP[qual];
	}

	public double convert2P(byte qual) {
		qual =  qual > MAX_Q2 ? MAX_Q2 : qual;
		return phred2baseP[qual];
	}
	
	public double convert2perEntityP(byte qual) {
		qual =  qual > MAX_Q2 ? MAX_Q2 : qual;
		return phred2baseErrorP[qual];
	}
	
	public double getErrorP(byte qual) {
		qual =  qual > MAX_Q2 ? MAX_Q2 : qual;
		return phred2errerP[qual];
	}

	/**
	 * Calculate a probability vector P for the pileup. |P| = |bases| 
	 */
	public double[] convert2ProbVector(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = new double[bases.length];

		for(int baseI = 0; baseI < bases.length; ++baseI) {
			for(byte qual = 0 ; qual < Phred2Prob.MAX_Q; ++qual) {
				// number of bases with specific quality 
				final int count = pileup.getQualCount(bases[baseI], qual);
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

}