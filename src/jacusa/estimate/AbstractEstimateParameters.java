package jacusa.estimate;

import jacusa.pileup.Pileup;
import jacusa.process.phred2prob.Phred2Prob;

public abstract class AbstractEstimateParameters {

	private final String name;
	private final String desc;
	protected final Phred2Prob phred2Prob;

	public AbstractEstimateParameters(final String name, final String desc, final Phred2Prob phred2Prob) {
		this.name = name;
		this.desc = desc;
		this.phred2Prob = phred2Prob;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public Phred2Prob getPhred2Prob() {
		return phred2Prob;
	}

	public abstract double[] estimateAlpha(int[] baseIs, Pileup[] pileups);
	public abstract double[] estimateExpectedProb(int[] baseIs, Pileup[] pileups);
	public abstract double[][] estimateProbs(int[] baseIs, Pileup[] pileups);
	// abstract double[] estimateVariance(int[] baseIs, Pileup[] pileups);

}