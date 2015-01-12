package jacusa.io.format.result;

import jacusa.filter.FilterConfig;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.process.phred2prob.Phred2Prob;
import jacusa.util.StringCollapse;

// CHANGED
public class DefaultResultFormat extends AbstractResultFormat {

	public static final char CHAR = 'D';
	
	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';

	private BaseConfig baseConfig;
	private FilterConfig filterConfig;
	public Phred2Prob phred2Prob;

	public DefaultResultFormat(BaseConfig baseConfig, FilterConfig filterConfig) {
		super(CHAR, "ACCUSA2 default output");
		this.baseConfig = baseConfig;
		this.filterConfig = filterConfig;
		
		phred2Prob = Phred2Prob.getInstance(baseConfig.getBaseLength());
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
		addSampleHeader(sb, 'A', parallelPileup.getN1());
		sb.append(getSEP());
		// (2) second sample  infos
		addSampleHeader(sb, 'B', parallelPileup.getN2());

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

		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append("filter_info");
		}
		
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
		addPileups(sb, parallelPileup.getStrand1(), parallelPileup.getPileups1());
		// (2) second pileups
		addPileups(sb, parallelPileup.getStrand2(), parallelPileup.getPileups2());
		
		return sb;
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		final StringBuilder sb = convert2StringHelper(parallelPileup);
		return sb.toString();		
	}
	
	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value, final String filterInfo) {
		final StringBuilder sb = convert2StringHelper(parallelPileup);
	
		// meanA
		sb.append(SEP);
		double[] meanA = phred2Prob.getPileupsMeanProb(baseConfig.getBasesI(), parallelPileup.getPileups1());
		sb.append(StringCollapse.collapse(meanA, ","));
		// varA
		sb.append(SEP);
		if (parallelPileup.getN1() > 1) {
			double[] varianceA = phred2Prob.getPileupsVarianceProb(baseConfig.getBasesI(), meanA, parallelPileup.getPileups1());
			sb.append(StringCollapse.collapse(varianceA, ","));
		} else {
			sb.append(EMPTY);
		}
		
		// meanB
		sb.append(SEP);
		double[] meanB = phred2Prob.getPileupsMeanProb(baseConfig.getBasesI(), parallelPileup.getPileups2());
		sb.append(StringCollapse.collapse(meanB, ","));
		// varB
		sb.append(SEP);
		if (parallelPileup.getN2() > 1) {
			double[] varianceB = phred2Prob.getPileupsVarianceProb(baseConfig.getBasesI(), meanB, parallelPileup.getPileups2());
			sb.append(StringCollapse.collapse(varianceB, ","));
		} else {
			sb.append(EMPTY);
		}
		
		// meanAB
		sb.append(SEP);
		double[] meanP = phred2Prob.getPileupsMeanProb(baseConfig.getBasesI(), parallelPileup.getPileupsP());
		sb.append(StringCollapse.collapse(meanP, ","));
		sb.append(SEP);
		// varAB
		double[] varianceP = phred2Prob.getPileupsVarianceProb(baseConfig.getBasesI(), meanB, parallelPileup.getPileupsP());
		sb.append(StringCollapse.collapse(varianceP, ","));
		
		// add unfiltered value
		sb.append(SEP);
		sb.append(value);

		// add filter info
		sb.append(SEP);
		sb.append(filterInfo);
		
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
		return Double.parseDouble(cols[cols.length - 2]);
	}

	@Override
	public String getFilterInfo(String line) {
		String cols[] = line.split(Character.toString(SEP));
		return cols[cols.length];
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

}