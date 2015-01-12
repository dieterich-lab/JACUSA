package jacusa.io.format.result;

import jacusa.filter.FilterConfig;
import jacusa.io.format.output.PileupFormat;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;

public class PileupResultFormat extends AbstractResultFormat {

	private PileupFormat pileupFormat; 
	
	public PileupResultFormat(BaseConfig baseConfig, FilterConfig fitlerConfig) {
		super('A', "pileup like ACCUSA result format");
		pileupFormat = new PileupFormat(baseConfig);
	}
	
	public String getHeader(ParallelPileup parallelPileup) {
		final StringBuilder sb = new StringBuilder();
		sb.append(getCOMMENT());

		sb.append("contig");
		sb.append(getSEP());

		sb.append("position");
		sb.append(getSEP());

		// (1) first sample infos
		sb.append("strand1");
		sb.append(getSEP());
		sb.append("bases1");
		sb.append(getSEP());
		sb.append("quals1");
		
		sb.append(getSEP());
		
		// (2) second sample infos
		sb.append("strand2");
		sb.append(getSEP());
		sb.append("bases2");
		sb.append(getSEP());
		sb.append("quals2");

		sb.append(getSEP());
		sb.append("stat");
		
		sb.append(getSEP());
		sb.append("filter_info");

		return sb.toString();
	}
	
	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value, final String filterInfo) {
		StringBuilder sb = new StringBuilder(convert2String(parallelPileup));
		// add unfiltered value
		sb.append(pileupFormat.getSEP());
		sb.append(value);
		
		sb.append(pileupFormat.getSEP());
		sb.append(filterInfo);
		
		return sb.toString();
	}

	
	
	@Override
	public double extractValue(String line) {
		String[] cols = line.split(Character.toString(pileupFormat.getSEP()));
		return Double.parseDouble(cols[cols.length - 2]);
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		return pileupFormat.convert2String(parallelPileup);
	}

	@Override
	public String getFilterInfo(String line) {
		String cols[] = line.split(Character.toString(pileupFormat.getSEP()));
		return cols[cols.length];
	}
	
	@Override
	public char getCOMMENT() {
		return pileupFormat.getCOMMENT();
	}
	
	@Override
	public char getEMPTY() {
		return pileupFormat.getEMPTY();
	}
	
	public char getSEP() {
		return pileupFormat.getSEP(); 
	}
	
	public char getSEP2() {
		return pileupFormat.getSEP2(); 
	}
	
}