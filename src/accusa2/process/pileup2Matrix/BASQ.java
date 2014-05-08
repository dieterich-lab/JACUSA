package accusa2.process.pileup2Matrix;

import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class BASQ extends AbstractPileup2Matrix {

	protected Phred2Prob phred2Prob;
	
	public BASQ() {
		name = "BASQ";
		desc = "BASQ -> p(A, C, G, T)";

		phred2Prob = new Phred2Prob();
	}

	/**
	 * Calculate a probability vector P for the pileup. |P| = |bases| 
	 */
	public double[] calculate(final int[] bases, final Pileup pileup) {
		// container for accumulated probabilities 
		final double[] p = new double[bases.length];

		for(int baseI = 0; baseI < bases.length; ++baseI) {
			for(byte qual = 0 ; qual < Phred2Prob.MAX_Q; ++qual) {
				// number of bases with specific quality 
				final int count = pileup.getQualCount(bases[baseI], qual);
				if(count > 0) {
					final double baseP = phred2Prob.convert2P(qual);
					p[baseI] += count * baseP;

					final double errorP = phred2Prob.convert2errorP(qual) / (bases.length - 1);
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
