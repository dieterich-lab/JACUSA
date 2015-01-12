package jacusa.io.format.result;

import jacusa.filter.FilterConfig;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;

public class BEDResultFormat extends AbstractResultFormat {

	public static final char CHAR = 'B';
	
	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';

	private FilterConfig filterConfig;
	public Phred2Prob phred2Prob;

	public BEDResultFormat(BaseConfig baseConfig, FilterConfig filterConfig) {
		super(CHAR, "BED like output");
		this.filterConfig = filterConfig;

		phred2Prob = Phred2Prob.getInstance(baseConfig.getBaseLength());
	}

	@Override
	public String getHeader(ParallelPileup parallelPileup) {
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
		sb.append(getSEP());
		sb.append("stat");
		sb.append(getSEP());
		
		sb.append("strand");
		sb.append(getSEP());
		
		// (1) first sample  infos
		addSampleHeader(sb, '1', parallelPileup.getN1());
		sb.append(getSEP());
		// (2) second sample  infos
		addSampleHeader(sb, '2', parallelPileup.getN2());

		// add filtering info
		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append("filter_info");
		}

		return sb.toString();
	}
	
	private void addSampleHeader(StringBuilder sb, char sample, int replicates) {
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

	private StringBuilder convert2StringHelper(final ParallelPileup parallelPileup, final double value) {
		final StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition() - 1);
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());
		
		sb.append(SEP);
		sb.append("variant");
		
		sb.append(SEP);
		if (Double.isNaN(value)) {
			sb.append("NA");
		} else {
			sb.append(value);
		}

		sb.append(SEP);
		sb.append(parallelPileup.getStrand().character());

		// (1) first pileups
		addPileups(sb, parallelPileup.getPileups1());
		// (2) second pileups
		addPileups(sb, parallelPileup.getPileups2());

		return sb;
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		final StringBuilder sb = convert2StringHelper(parallelPileup, Double.NaN);
		return sb.toString();		
	}
	
	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value, final String filterInfo) {
		final StringBuilder sb = convert2StringHelper(parallelPileup, value);

		sb.append(SEP);
		sb.append(filterInfo);

		return sb.toString();
	}
	
	/*
	 * Helper function
	 */
	private void addPileups(StringBuilder sb, Pileup[] pileups) {
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
		return Double.parseDouble(cols[4]);
	}

	@Override
	public String getFilterInfo(String line) {
		String[] cols = line.split(Character.toString(SEP));
		return cols[cols.length - 1];
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