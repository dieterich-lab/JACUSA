package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public class CombinedAlphaInit extends AbstractAlphaInit {

	private AbstractAlphaInit A;
	private AbstractAlphaInit B;
	
	public CombinedAlphaInit(AbstractAlphaInit A, AbstractAlphaInit B) {
		super("combined", "A + B");
		this.A = A;
		this.B = B;
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages) {

		return A.init(baseIs, pileups, pileupMatrix, pileupCoverages);
	}

	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector, 
			final double pileupCoverage) {
		return B.init(baseIs, pileup, pileupVector, pileupErrorVector, pileupCoverage);
	}
		
}
