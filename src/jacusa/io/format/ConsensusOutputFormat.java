package jacusa.io.format;

import jacusa.filter.FilterConfig;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.util.Util;

public class ConsensusOutputFormat extends AbstractOutputFormat {

	public static final char CHAR = 'C';
	
	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';
	
	protected FilterConfig filterConfig;
	protected BaseConfig baseConfig;
	public Phred2Prob phred2Prob;
	private boolean showReferenceBase;
	
	public ConsensusOutputFormat(
			final char c,
			final String desc,
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig,
			final boolean showReferenceBase) {
		super(c, desc);
		
		this.baseConfig = baseConfig;
		this.filterConfig = filterConfig;

		phred2Prob = Phred2Prob.getInstance(baseConfig.getBaseLength());
		this.showReferenceBase = showReferenceBase;
	}

	public ConsensusOutputFormat(
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig,
			final boolean showReferenceBase) {
		this(CHAR, "Consensus", baseConfig, filterConfig, showReferenceBase);
	}

	@Override
	public String getHeader(String[] pathnames1, String[] pathnames2) {
		final StringBuilder sb = new StringBuilder();

		sb.append(COMMENT);

		// position (0-based)
		sb.append("contig");
		sb.append(getSEP());
		sb.append("start");
		sb.append(getSEP());
		sb.append("end");
		sb.append(getSEP());

		sb.append("name");
		sb.append(getSEP());

		// stat	
		sb.append("stat");
		sb.append(getSEP());
		
		sb.append("strand");
		sb.append(getSEP());
		
		sb.append("consensus");

		sb.append(getSEP());
		sb.append("info");
		
		// add filtering info
		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append("filter_info");
		}

		if (showReferenceBase) {
			sb.append(getSEP());
			sb.append("refBase");
		}
		
		return sb.toString();
	}
	
	protected void addSampleHeader(StringBuilder sb, char sample, int replicates) {
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

	@Override
	public String convert2String(Result result) {
		final ParallelPileup parallelPileup = result.getParellelPileup();
		final double statistic = result.getStatistic();
		final StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getStart() - 1);
		sb.append(SEP);
		sb.append(parallelPileup.getEnd());
		
		sb.append(SEP);
		sb.append("variant");
		
		sb.append(SEP);
		if (Double.isNaN(statistic)) {
			sb.append("NA");
		} else {
			sb.append(Util.format(statistic));
		}

		sb.append(SEP);
		sb.append(parallelPileup.getStrand().character());

		addPileups(sb, parallelPileup.getPooledPileup());

		sb.append(getSEP());
		sb.append(result.getResultInfo().combine());
		
		// add filtering info
		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append(result.getFilterInfo().combine());
		}
		
		if (showReferenceBase) {
			sb.append(getSEP());
			sb.append(parallelPileup.getPooledPileup().getRefBase());
		}

		return sb.toString();		
	}
	
	/*
	 * Helper function
	 */
	protected void addPileups(StringBuilder sb, Pileup pileup) {
		// output sample: Ax,Cx,Gx,Tx

		int maxBaseI = -1;
		int count = Integer.MIN_VALUE;
		for (int baseI : pileup.getAlleles()) {
			if (pileup.getCounts().getBaseCount(baseI) > count) {
				maxBaseI = baseI;
				count = pileup.getCounts().getBaseCount(baseI);
			}
		}
		sb.append(SEP);
		sb.append(baseConfig.getBases()[maxBaseI]);
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