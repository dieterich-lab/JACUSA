package jacusa.estimate.coverage;

import jacusa.estimate.AbstractEstimateParameters;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.Pileup;

public abstract class AbstractCoverageEstimateParameters extends AbstractEstimateParameters {

	public AbstractCoverageEstimateParameters(final String name, final String desc, final Phred2Prob phred2Prob) {
		super(name, desc, phred2Prob);
	}

	public abstract double[] estimateAlpha(int[] baseIs, Pileup[] pileups, int coverage);

}