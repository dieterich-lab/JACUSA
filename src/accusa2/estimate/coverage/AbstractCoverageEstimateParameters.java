package accusa2.estimate.coverage;

import accusa2.estimate.AbstractEstimateParameters;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public abstract class AbstractCoverageEstimateParameters extends AbstractEstimateParameters {

	public AbstractCoverageEstimateParameters(final String name, final String desc, final Phred2Prob phred2Prob) {
		super(name, desc, phred2Prob);
	}

	public abstract double[] estimateAlpha(int[] baseIs, Pileup[] pileups, int coverage);

}