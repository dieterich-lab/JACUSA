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

		switch (pileups.length) {
		case 1:
			return bayes.init(baseIs, pileups, pileupMatrix, pileupCoverages);

		default:
			return ronning.init(baseIs, pileups, pileupMatrix, pileupCoverages);
		}

	}

}
