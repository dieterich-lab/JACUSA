package jacusa.io.format;

import jacusa.JACUSA;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;

import java.util.Calendar;

public class VCF_ResultFormat extends AbstractOutputFormat {

	private BaseConfig baseConfig;
	public static final char CHAR = 'V';

	public VCF_ResultFormat(final BaseConfig baseConfig) {
		super(CHAR, "VCF output");
		this.baseConfig = baseConfig;
	}
	
	@Override
	public String getHeader(String[] pathnames1, String[] pathnames2) {
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

		return sb.toString();
	}

	@Override
	public String convert2String(Result result) {
		final StringBuilder sb = new StringBuilder();
		final ParallelPileup parallelPileup = result.getParellelPileup();
		String filterInfo = (String)result.getObject("filterInfo");
		if (filterInfo == null || filterInfo.length() == 0) {
			filterInfo = "*";
		}

		StringBuilder sb2 = new StringBuilder();
		for (int allelI : parallelPileup.getPooledPileup().getAlleles()) {
			if (parallelPileup.getPooledPileup().getRefBase() != baseConfig.getBases()[allelI]) {
				sb2.append(baseConfig.getBases()[allelI]);
			}
		}
		
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
				sb2.toString(),
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

		for (int i = 0; i < parallelPileup.getN1(); ++i) {
			sb.append(getSEP());
			sb.append(parallelPileup.getPileups1()[i].getCoverage());
		}
		for (int i = 0; i < parallelPileup.getN2(); ++i) {
			sb.append(getSEP());
			sb.append(parallelPileup.getPileups2()[i].getCoverage());
		}
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