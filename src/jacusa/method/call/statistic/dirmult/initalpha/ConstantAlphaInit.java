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
	public AbstractAlphaInit newInstance(String line) {
		double constant = 0.1;
		for (String v : line.split(Character.toString(','))) {
			String[] kv = v.split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}
			if (key.equals("value")) {
				constant = Double.parseDouble(value);
			}
		}
		if (constant == -1d) {
			throw new IllegalArgumentException(line + "\nConstant has to be > 0");
		}
		return new ConstantAlphaInit(constant);
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix) {
		final double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, constant);
		return alpha;
	}

	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector) {
		final double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, constant);
		return alpha;
	}
	
}
