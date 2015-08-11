package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public class DefaultCombinedAlphaInit extends AbstractAlphaInit {

	private AbstractAlphaInit A;
	private AbstractAlphaInit B;
	
	public DefaultCombinedAlphaInit(String name, AbstractAlphaInit A, AbstractAlphaInit B) {
		super(name, A.getName() + " + " + B.getName());
		this.A = A;
		this.B = B;
	}

	@Override
	public AbstractAlphaInit newInstance(String line) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		StringBuilder sb = sb1;
		for (String s : line.split(",")) {
			if (s.startsWith("initAlpha2")) {
				sb = sb2;
			}
			sb.append(s);
		}

		return new DefaultCombinedAlphaInit(getName(), A.newInstance(sb1.toString()), B.newInstance(sb2.toString()));
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix) {
		return A.init(baseIs, pileups, pileupMatrix);
	}

	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector) {
		return B.init(baseIs, pileup, pileupVector, pileupErrorVector);
	}
		
}
