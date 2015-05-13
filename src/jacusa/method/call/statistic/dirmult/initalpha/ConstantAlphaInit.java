package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.Pileup;

public class ConstantAlphaInit extends AbstractAlphaInit {

	private double constant;
	
	public ConstantAlphaInit(final double constant) {
		super("constant", "alpha = (c, c, c, c)");
		this.constant = constant;
	}
	
	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages) {
		final double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, constant);
		return alpha;
	}

	@Override
	public double[] init(int[] baseIs, 
			Pileup pileup, 
			double[] pileupVector,
			double[] pileupErrorVector, 
			double pileupCoverage) {
		final double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, constant);
		return alpha;
	}
	
}
