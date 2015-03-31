package jacusa.io.format;

import jacusa.JACUSA;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;

import java.util.Calendar;

public class VCF_ResultFormat extends AbstractOutputFormat {

	public static final char CHAR = 'V';

	private String[] pathnames1;
	private String[] pathnames2;

	public VCF_ResultFormat(final String[] pathnames1, final String[] pathnames2) {
		super(CHAR, "VCF output");
		this.pathnames1 = pathnames1;
		this.pathnames2 = pathnames2;
	}
	
	@Override
	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(getCOMMENT());
		sb.append("#fileformat=VCFv4.0");
		sb.append('\n');
		
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		sb.append("fileDate=");
		sb.append(year);
		sb.append(month);
		sb.append(day);
		sb.append('\n');

		sb.append(getCOMMENT());
		sb.append("#source");
		sb.append(JACUSA.NAME);
		sb.append('\n');

		String[] cols = {
				"CHROM",
				"POS",
				"ID",
				"REF",
				"ALT",
				"QUAL",
				"FILTER",
				"INFO",
				"FORMAT"
		};
		
		sb.append(getCOMMENT());
		sb.append(cols[0]);
		for (int i = 1; i < cols.length; ++i) {
			sb.append(getSEP());
			sb.append(cols[i]);
		}
		for (String pathname : pathnames1)  {
			sb.append(getSEP());
			sb.append(pathname);
		}
		for (String pathname : pathnames2)  {
			sb.append(getSEP());
			sb.append(pathname);
		}
		sb.append('\n');

		return sb.toString();
	}

	@Override
	public String convert2String(Result result) {
		final StringBuilder sb = new StringBuilder();
		final ParallelPileup parallelPileup = result.getParellelPileup();
		final String filterInfo = (String)result.getObject("filterInfo");
		
		String[] cols = {
				// contig
				parallelPileup.getPooledPileup().getContig(),
				// position
				Integer.toString(parallelPileup.getPooledPileup().getPosition()),
				// ID
				Character.toString(getEMPTY()),
				// REF
				Character.toString(parallelPileup.getPooledPileup().getRefBase()),
				// ALT
				Character.toString(getEMPTY()),
				// QUAL
				Character.toString(getEMPTY()),
				// FILTER
				"PASS",
				// INFO
				filterInfo,
				// FORMAT
				"DP",
		};

		sb.append(cols[0]);
		for (int i = 1; i < cols.length; ++i) {
			sb.append(getSEP());
			sb.append(cols[i]);
		}

		for (int i = 0; i < parallelPileup.getN(); ++i) {
			sb.append(getSEP());
			if (i < parallelPileup.getN1()) {
				sb.append(parallelPileup.getPileups1()[i].getCoverage());
				
			} else {
				sb.append(parallelPileup.getPileups2()[i].getCoverage());
			}
		}
		sb.append('\n');
		
		return sb.toString();
	}

	public char getCOMMENT() {
		return '#';
	}

	public char getSEP() {
		return '\t';
	}

	public char getSEP2() {
		return ';';
	}

	public char getSEP3() {
		return ':';
	}
	
	public char getEMPTY() {
		return '.';
	}

}