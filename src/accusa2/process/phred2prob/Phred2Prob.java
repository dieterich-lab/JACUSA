package accusa2.process.phred2prob;

import accusa2.pileup.BaseConfig;
import accusa2.pileup.Pileup;

public final class Phred2Prob {

	private final double[] phred2errerP;
	private final double[] phred2baseP;
	private final double[] phred2baseErrorP;

	// phred capped at 41
	public static final int MAX_Q = 41; // some machines give phred score of 60 -> Prob of error: 10^-6 ?!
	private static Phred2Prob[] singles = new Phred2Prob[BaseConfig.VALID.length];

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

	/**
	 * Calculate a probability vector P for the pileup. |P| = |bases| 
	 */
	public double[] convert2ProbVector(final int[] bases, final Pileup pileup) {
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

	public static Phred2Prob getInstance() {
		return getInstance(BaseConfig.VALID.length);
	}
	
	public static Phred2Prob getInstance(int n) {
		if (singles[n] == null) {
			singles[n] = new Phred2Prob(n);
		}

		return singles[n];
	}
	
}