package jacusa.method.call.statistic.dirmult.initalpha;

import java.util.Arrays;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;

/**
 * 
 * @author Michael Piechotta
 */
public class WeirAlphaInit extends AbstractAlphaInit {

	public WeirAlphaInit() {
		super("Weir", "Weir");
	}

	@Override
	public AbstractAlphaInit newInstance(String line) {
		return new WeirAlphaInit();
	}

	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix) {
		final double[] alpha = new double[BaseConfig.VALID.length];

		int n = pileupMatrix[0].length;
		double[] pileupCoverages = getCoverages(baseIs, pileupMatrix);
		
		// calculate mean
		double[] mean = new double[n];
		Arrays.fill(mean, 0d);
		double sum = 0.0;
		
		// calculate pileup proportion matrix
		final double[][] pileupProportionMatrix = new double[pileups.length][n];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				pileupProportionMatrix[pileupI][baseI] = pileupMatrix[pileupI][baseI] / pileupCoverages[pileupI];
				mean[baseI] += pileupMatrix[pileupI][baseI];
				sum += pileupMatrix[pileupI][baseI];
			}
		}		

		// calculate mean
		/*
		double[] mean = new double[n];
		Arrays.fill(mean, 0d);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				mean[baseI] += pileupProportionMatrix[pileupI][baseI];
			}
		}
		*/
		for (int baseI : baseIs) {
			mean[baseI] /= sum;
		}

		double MSP = 0.0;
		double MSG = 0.0;
		for (int baseI : baseIs) {
			for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
				MSP += pileupCoverages[pileupI] * Math.pow(pileupProportionMatrix[pileupI][baseI] - mean[baseI], 2);
				MSG += pileupCoverages[pileupI] * pileupProportionMatrix[pileupI][baseI] * (1d - pileupProportionMatrix[pileupI][baseI]);
				
			}
		}
		MSP /= (double)(pileups.length - 1);
		MSG /= (double)(sum - (pileups.length));
				
		double nc = 0.0;
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			nc += Math.pow(pileupCoverages[pileupI], 2);  
		}
		nc = sum - nc / sum; 
		nc /= (double)(pileups.length - 1);

		double phi = (MSP - MSG) / (MSP + (nc - 1d) * MSG);

		// fix when negative
		if (phi < 0.0) {
			phi = 0.001;
		}
		for (int baseI : baseIs) {
			alpha[baseI] = mean[baseI] * (1d - phi) / phi;
		}

		return alpha;
	}

	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector) {
		return init(baseIs, new Pileup[]{pileup}, new double[][]{pileupVector});
	}
	
}
