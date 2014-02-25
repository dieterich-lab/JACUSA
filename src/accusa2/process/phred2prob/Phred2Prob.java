package accusa2.process.phred2prob;

import accusa2.pileup.Pileup;

public final class Phred2Prob {

	private final double[] phred2errerP;
	private final double[] phred2baseP;
	private final double[] phred2baseErrorP;

	public static final int MAX_Q = 60;
	
	public Phred2Prob() {
		this(Pileup.LENGTH);
	}

	public Phred2Prob(int n) {
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
		return phred2errerP[qual];
	}

	public double convert2P(byte qual) {
		return phred2baseP[qual];
	}
	
	public double convert2perEntityP(byte qual) {
		return phred2baseErrorP[qual];
	}
	
	public double getErrorP(byte qual) {
		return phred2errerP[qual];
	}

}
