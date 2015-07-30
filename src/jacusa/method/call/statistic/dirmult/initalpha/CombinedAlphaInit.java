package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public class CombinedAlphaInit extends AbstractAlphaInit {

	private BayesAlphaInit bayes;
	private RonningAlphaInit ronning;
	
	public CombinedAlphaInit() {
		super("combined", "Bayes + Ronning");
		bayes = new BayesAlphaInit();
		ronning = new RonningAlphaInit();
	}
	
	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup[] pileups,
			final double[][] pileupMatrix, 
			final double[] pileupCoverages) {

		return ronning.init(baseIs, pileups, pileupMatrix, pileupCoverages);

	}

	@Override
	public double[] init(int[] baseIs, 
			Pileup pileup, 
			double[] pileupVector,
			double[] pileupErrorVector, 
			double pileupCoverage) {
		return bayes.init(baseIs, pileup, pileupVector, pileupErrorVector, pileupCoverage);
	}
		
}
