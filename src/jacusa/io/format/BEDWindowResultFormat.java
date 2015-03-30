package jacusa.io.format;

import jacusa.filter.FilterConfig;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.result.Result;

public class BEDWindowResultFormat extends AbstractOutputFormat {

	public static final char CHAR = 'B';
	
	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';

	private int replicates1;
	private int replicates2;
	
	public Phred2Prob phred2Prob;

	public BEDWindowResultFormat(
			final int replicates1, 
			final int replicates2, 
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig) {
		super(CHAR, "BED like window output");

		this.replicates1 = replicates1;
		this.replicates2 = replicates2;
		
		phred2Prob = Phred2Prob.getInstance(baseConfig.getBaseLength());
	}

	@Override
	public String getHeader() {
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
		addSampleHeader(sb, '1', replicates1);
		sb.append(getSEP());
		// (2) second sample  infos
		addSampleHeader(sb, '2', replicates2);

		/* no filters for window calling
		// add filtering info
		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append("filter_info");
		}
		*/
		return sb.toString();
	}
	
	private void addSampleHeader(StringBuilder sb, char sample, int replicates) {
		sb.append("bases");
		sb.append(sample);
		sb.append(1);
	}

	private StringBuilder convert2StringHelper(final ParallelPileup parallelPileup, final double value) {
		final StringBuilder sb = new StringBuilder();

		// N1 should be == N2
		int n = parallelPileup.getN1();
		String contig = parallelPileup.getContig();
		String id = parallelPileup.getContig() + "_" + (parallelPileup.getStart() - 1) + "_" + parallelPileup.getEnd();

		for (int i = 0; i < n; ++i) {
			int start = parallelPileup.getPileups1()[i].getPosition() - 1;
			int end = start + 1;
			
			// coordinates
			sb.append(contig);
			sb.append(SEP);
			sb.append(start);
			sb.append(SEP);
			sb.append(end);
			
			// "window id"
			sb.append(SEP);
			sb.append(id);

			sb.append(SEP);
			if (Double.isNaN(value)) {
				sb.append("NA");
			} else {
				sb.append(value);
			}

			sb.append(SEP);
			sb.append(parallelPileup.getStrand().character());

			// (1) first pileups
			addPileup(sb, parallelPileup.getPileups1()[i]);
			// (2) second pileups
			addPileup(sb, parallelPileup.getPileups2()[i]);
			if (i + 1 < n) {
				sb.append("\n");
			}
		}

		return sb;
	}

	@Override
	public String convert2String(Result result) {
		final ParallelPileup parallelPileup = result.getParellelPileup();
		final StringBuilder sb = convert2StringHelper(parallelPileup, Double.NaN);
		return sb.toString();		
	}

	/*
	 * Helper function
	 */
	private void addPileup(StringBuilder sb, Pileup pileup) {
		// output sample: Ax,Cx,Gx,Tx
		sb.append(SEP);
		int baseI = 0;
		sb.append(pileup.getCounts().getBaseCount()[baseI]);
		baseI++;
		for (; baseI < pileup.getCounts().getBaseCount().length ; ++baseI) {
			sb.append(SEP2);
			sb.append(pileup.getCounts().getBaseCount()[baseI]);
		}
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