package accusa2.process.pileup2Matrix;

import accusa2.pileup.Pileup;

@Deprecated
public abstract class AbstractPileup2Prob {

	protected String name;
	protected String desc;

	/**
	 * 
	 * @param pileup
	 * @return
	 */
	public abstract double[] calculate(int[] bases, final Pileup pileup);

	/**
	 * 
	 * @return name of conversion.
	 */
	public final String getDescription() {
		return desc;
	}

	/**
	 * 
	 * @return name of conversion.
	 */
	public final String getName() {
		return name;
	}

}
