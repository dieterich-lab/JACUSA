package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public abstract class AbstractAlphaInit {

	private String name;
	private String desc;
	
	public AbstractAlphaInit(final String name, final String desc) {
		this.name	= name;
		this.desc	= desc;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}

	public abstract double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix,
			final double[] pileupCoverages,
			final double[][] pileupProportionMatrix);

}