package jacusa.io.format;

import jacusa.JACUSA;
import jacusa.filter.FilterConfig;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;

import java.util.Calendar;

public class VCF_ResultFormat extends AbstractOutputFormat {

	private BaseConfig baseConfig;
	private FilterConfig filterConfig;
	public static final char CHAR = 'V';

	public VCF_ResultFormat(final BaseConfig baseConfig, final FilterConfig filterConfig) {
		super(CHAR, "VCF Output format. Option -P will be ignored (VCF is unstranded)");
		this.baseConfig = baseConfig;
		this.filterConfig = filterConfig;
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
		sb.append("##fileDate=");
		sb.append(year);
		if (month < 10)
			sb.append(0);	
		sb.append(month);
		if (day < 10)
			sb.append(0);
		sb.append(day);
		sb.append('\n');

		sb.append("##source=");
		sb.append(JACUSA.NAME + "-" + JACUSA.VERSION);
		sb.append('\n');

		// add filter descriptions to header
		for (final AbstractFilterFactory<?> filter : filterConfig.getFactories()) {
			sb.append("##FILTER=<ID=");
			sb.append(filter.getC());
			sb.append(",Description=");
			sb.append("\"" + filter.getDesc() + "\"");
			sb.append('\n');
		}
		sb.append("##FORMAT=<ID=DP,Number=1,Type=Integer,Description=\"Read Depth\">\n");
		sb.append("##FORMAT=<ID=BC,Number=4,Type=Integer,Description=\"Base counts A,C,G,T\">\n");
		
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
		String filterInfo = result.getFilterInfo().combine();
		if (filterInfo == null || filterInfo.length() == 0) {
			filterInfo = "PASS";
		}

		StringBuilder sb2 = new StringBuilder();
		boolean first = true;
		for (int allelI : parallelPileup.getPooledPileup().getAlleles()) {
			if (parallelPileup.getPooledPileup().getRefBase() != baseConfig.getBases()[allelI]) {
				if (! first) {
					sb2.append(',');
				} else {
					first = false;
				}
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
				filterInfo,
				// INFO
				"*",
				// FORMAT
				"DP" + getSEP3() + "BC",
		};

		sb.append(cols[0]);
		for (int i = 1; i < cols.length; ++i) {
			sb.append(getSEP());
			sb.append(cols[i]);
		}

		addParallelPileup(sb, parallelPileup.getPileups1());
		addParallelPileup(sb, parallelPileup.getPileups2());
		
		return sb.toString();
	}

	private void addParallelPileup(final StringBuilder sb, final Pileup pileups[]) {
		for (int i = 0; i < pileups.length; ++i) {
			// add DP
			sb.append(getSEP());
			sb.append(pileups[i].getCoverage());
			
			sb.append(getSEP3());
			
			// add BC - base counts
			int j = 0;
			char b = BaseConfig.VALID[j];
			int baseI = baseConfig.getBaseI((byte)b);
			int count = 0;
			if (baseI >= 0) {
				count = pileups[i].getCounts().getBaseCount(baseI);
			}
			sb.append(count);
			++j;
			for (; j < BaseConfig.VALID.length; ++j) {
				b = BaseConfig.VALID[j];
				baseI = baseConfig.getBaseI((byte)b);
				count = 0;
				if (baseI >= 0) {
					count = pileups[i].getCounts().getBaseCount(baseI);
				}
				sb.append(',');
				sb.append(count);
			}
		}
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