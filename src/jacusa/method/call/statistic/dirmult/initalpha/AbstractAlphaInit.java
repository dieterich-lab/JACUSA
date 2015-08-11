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
			final double[][] pileupMatrix);

	public abstract double[] init(
			final int[] baseIs, 
			final Pileup pileup,
			final double[] pileupVector,
			final double[] pileupErrorVector);

	public abstract AbstractAlphaInit newInstance(final String line);

	// misc method
	protected double[] getCoverages(final int[] baseIs, final double[][] pileupMatrix) {
		int pileupN = pileupMatrix.length;
		double[] coverages = new double[pileupN];
		for (int pileupI = 0; pileupI < pileupN; pileupI++) {
			double sum = 0.0;
			for (int baseI : baseIs) {
				sum += pileupMatrix[pileupI][baseI];
			}
			coverages[pileupI] = sum;
		}

		return coverages;
	}
	
}