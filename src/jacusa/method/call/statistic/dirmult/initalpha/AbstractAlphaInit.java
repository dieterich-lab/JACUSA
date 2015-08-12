package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

/**
 * 
 * @author Michael Piechotta
 */
public abstract class AbstractAlphaInit {

	private String name;
	private String desc;
	
	public AbstractAlphaInit(final String name, final String desc) {
		this.name	= name;
		this.desc	= desc;
	}
	
	/**
	 * Return the short name.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Return a short description.
	 * 
	 * @return
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Calculate initial estimates for alpha when > 1 replicates are available.
	 * 
	 * @param baseIs
	 * @param pileups
	 * @param pileupMatrix
	 * @return
	 */
	public abstract double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix);

	/**
	 * Calculate initial estimates for alpha when NO replicates are available.
	 * 
	 * @param baseIs
	 * @param pileup
	 * @param pileupVector
	 * @param pileupErrorVector
	 * @return
	 */
	public abstract double[] init(
			final int[] baseIs, 
			final Pileup pileup,
			final double[] pileupVector,
			final double[] pileupErrorVector);

	/**
	 * Create a new instance.
	 * 
	 * @param line
	 * @return
	 */
	public abstract AbstractAlphaInit newInstance(final String line);

	/**
	 * Calculate the coverage per pileup/replicate taking pseudocounts into account
	 * 
	 * Helper method.
	 * 
	 * @param baseIs
	 * @param pileupMatrix
	 * @return
	 */
	protected double[] getCoverages(final int[] baseIs, final double[][] pileupMatrix) {
		int pileupN = pileupMatrix.length;
		double[] coverages = new double[pileupN];
		for (int pileupI = 0; pileupI < pileupN; pileupI++) {
			double rowSum = 0.0;
			for (int baseI : baseIs) {
				rowSum += pileupMatrix[pileupI][baseI];
			}
			coverages[pileupI] = rowSum;
		}

		return coverages;
	}
	
}