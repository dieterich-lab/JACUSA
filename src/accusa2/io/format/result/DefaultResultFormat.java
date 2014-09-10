package accusa2.io.format.result;

import java.util.Arrays;

import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

// CHANGED
public class DefaultResultFormat extends AbstractResultFormat {

	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';

	private BaseConfig baseConfig;
	private Phred2Prob phred2Prob;

	public DefaultResultFormat(BaseConfig baseConfig) {
		super('D', "ACCUSA2 default output");
		this.baseConfig = baseConfig;

		phred2Prob = Phred2Prob.getInstance(baseConfig.getBases().length);
	}

	@Override
	public String getHeader(ParallelPileup parallelPileup) {
		final StringBuilder sb = new StringBuilder();

		sb.append(COMMENT);

		// position (1-based)
		sb.append("contig");
		sb.append(getSEP());
		sb.append("position");
		sb.append(getSEP());

		// (1) first sample  infos
		addSampleHeader(sb, 'A', parallelPileup.getNA());
		sb.append(getSEP());
		// (2) second sample  infos
		addSampleHeader(sb, 'B', parallelPileup.getNB());

		/*
		sb.append(getSEP());
		// unfiltered value
		sb.append("unfiltered");

		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append("filtered");
		}
		*/

		//add means and vars
		sb.append(getSEP());
		sb.append("meanA");
		sb.append(getSEP());
		sb.append("varA");
		sb.append(getSEP());
		sb.append("meanB");
		sb.append(getSEP());
		sb.append("varB");
		sb.append(getSEP());
		sb.append("meanAB");
		sb.append(getSEP());
		sb.append("varAB");
		
		// stat
		sb.append(getSEP());
		sb.append("stat");

		return sb.toString();
	}
	
	private void addSampleHeader(StringBuilder sb, char sample, int replicates) {
		sb.append("strand");
		sb.append(sample);
		sb.append(getSEP());
	
		sb.append("bases");
		sb.append(sample);
		sb.append(1);
		if (replicates == 1) {
			return;
		}
		
		for (int i = 2; i <= replicates; ++i) {
			sb.append(SEP);
			sb.append("bases");
			sb.append(sample);
			sb.append(i);
		}
	}

	private StringBuilder convert2StringHelper(final ParallelPileup parallelPileup) {
		final StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());

		// (1) first pileups
		addPileups(sb, parallelPileup.getStrandA(), parallelPileup.getPileupsA());
		// (2) second pileups
		addPileups(sb, parallelPileup.getStrandB(), parallelPileup.getPileupsB());
		
		return sb;
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		final StringBuilder sb = convert2StringHelper(parallelPileup);
		return sb.toString();		
	}
	
	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value) {
		final StringBuilder sb = convert2StringHelper(parallelPileup);

		// meanA
		sb.append(SEP);
		double[] meanA = getPileupsMean(baseConfig.getBasesI(), parallelPileup.getPileupsA());
		sb.append(collapse(meanA));
		// varA
		sb.append(SEP);
		double[] varianceA = getPileupsVariance(baseConfig.getBasesI(), meanA, parallelPileup.getPileupsA());
		sb.append(collapse(varianceA));
		
		// meanB
		sb.append(SEP);
		double[] meanB = getPileupsMean(baseConfig.getBasesI(), parallelPileup.getPileupsB());
		sb.append(collapse(meanB));
		// varB
		sb.append(SEP);
		double[] varianceB = getPileupsVariance(baseConfig.getBasesI(), meanB, parallelPileup.getPileupsB());
		sb.append(collapse(varianceB));
		
		// meanAB
		sb.append(SEP);
		double[] meanP = getPileupsMean(baseConfig.getBasesI(), parallelPileup.getPileupsP());
		sb.append(collapse(meanP));
		sb.append(SEP);
		// varAB		
		double[] varianceP = getPileupsVariance(baseConfig.getBasesI(), meanB, parallelPileup.getPileupsP());
		sb.append(collapse(varianceP));
		
		// add unfiltered value
		sb.append(SEP);
		sb.append(value);

		return sb.toString();
	}
	
	/*
	 * Helper function
	 */
	private void addPileups(StringBuilder sb, STRAND strand, Pileup[] pileups) {
		// strand information
		sb.append(SEP);
		sb.append(strand.character());

		// output sample: Ax,Cx,Gx,Tx
		for (Pileup pileup : pileups) {
			sb.append(SEP);
			int baseI = 0;
			sb.append(pileup.getCounts().getBaseCount()[baseI]);
			baseI++;
			for (; baseI < pileup.getCounts().getBaseCount().length ; ++baseI) {
				sb.append(SEP2);
				sb.append(pileup.getCounts().getBaseCount()[baseI]);
			}
		}
	}

	/**
	 * Last column holds the final value
	 */
	@Override
	public double extractValue(String line) {
		String[] cols = line.split(Character.toString(SEP));
		return Double.parseDouble(cols[cols.length - 1]);
	}

	public char getCOMMENT() {
		return COMMENT;
	}

	public char getEMPTY() {
		return EMPTY;
	}

	public char getSEP() {
		return SEP;
	}
	
	public char getSEP2() {
		return SEP2;
	}

	/*
	 * TODO move it somewhere
	 */

	private String collapse(double[] values) {
		StringBuilder sb = new StringBuilder();
		sb.append(values[0]);
		for (int i = 1; i < values.length; ++i) {
			sb.append(",");
			sb.append(values[i]); // format output >=0.001 is fine.			
		}
		return sb.toString();
	}

	private double[] getPileupsMean(int[] basesI, Pileup[] pileups) {
		double[] totalMean = new double[basesI.length];
		Arrays.fill(totalMean, 0.0);

		for (Pileup pileup : pileups) {
			double[] pileupMean = phred2Prob.colMean(basesI, pileup);
			for (int baseI : basesI) {
				totalMean[baseI] += pileupMean[baseI];
			}
		}
		double n = pileups.length;
		for (int baseI : basesI) {
			totalMean[baseI] /= n;
		}

		return totalMean;
	}

	private double[] getPileupsVariance(int[] basesI, double[] totalMean, Pileup[] pileups) {
		double[] totalVariance = new double[basesI.length];
		Arrays.fill(totalVariance, 0.0);

		for (Pileup pileup : pileups) {
			double[] pileupMean = phred2Prob.colMean(basesI, pileup);
			for (int baseI : basesI) {
				totalVariance[baseI] +=  Math.pow(totalMean[baseI] - pileupMean[baseI], 2.0); 
			}
		}
		double n = pileups.length;
		for (int baseI : basesI) {
			totalMean[baseI] /= n - 1;
		}

		return totalVariance;
	}
	
}
