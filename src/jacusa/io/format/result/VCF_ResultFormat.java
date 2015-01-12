package jacusa.io.format.result;

import jacusa.JACUSA;
import jacusa.pileup.ParallelPileup;

import java.util.Calendar;

public class VCF_ResultFormat extends AbstractResultFormat {

	public static final char CHAR = 'V';
	
	public VCF_ResultFormat() {
		super(CHAR, "VCF output");
	}
	
	@Override
	public String getHeader(ParallelPileup parallelPileup) {
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
		for (int i = 0; i < parallelPileup.getN(); ++i) {
			sb.append(getSEP());
			sb.append("sample");
			if (i < parallelPileup.getN1()) {
				sb.append("A");
				
			} else {
				sb.append("B");
			}
			sb.append(i);
		}
		sb.append('\n');

		return sb.toString();
	}

	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value, final String filterInfo) {
		StringBuilder sb = new StringBuilder();
		
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

	@Override
	public String getFilterInfo(String line) {
		String cols[] = line.split(Character.toString(getSEP()));
		return cols[4];
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		return null;
	}

	@Override
	public double extractValue(String line) {
		return 0;
	}

	@Override
	public char getCOMMENT() {
		return '#';
	}

	@Override
	public char getSEP() {
		return '\t';
	}

	@Override
	public char getSEP2() {
		return ';';
	}

	public char getSEP3() {
		return ':';
	}
	
	@Override
	public char getEMPTY() {
		return '.';
	}

}