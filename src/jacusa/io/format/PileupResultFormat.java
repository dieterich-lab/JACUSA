package jacusa.io.format;

import jacusa.filter.FilterConfig;
import jacusa.io.format.PileupFormat;
import jacusa.pileup.BaseConfig;
import jacusa.result.Result;

public class PileupResultFormat extends AbstractOutputFormat {

	private int replicates1;
	private int replicates2;
	
	private PileupFormat pileupFormat; 
	
	public PileupResultFormat(
			final int replicates1, 
			final int replicates2, 
			final BaseConfig baseConfig, 
			final FilterConfig fitlerConfig) {
		super('A', "pileup like ACCUSA result format");
		
		this.replicates1 = replicates1;
		this.replicates2 = replicates2;
		pileupFormat = new PileupFormat(baseConfig);
	}
	
	public String getHeader() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getCOMMENT());

		sb.append("contig");
		sb.append(getSEP());

		sb.append("position");

		// (1) first sample infos
		for (int i = 0; i < replicates1; ++i) {
			sb.append(getSEP());
			sb.append("strand1");
			sb.append(i + 1);
			sb.append(getSEP());
			sb.append("bases1");
			sb.append(i + 1);
			sb.append(getSEP());
			sb.append("quals1");
			sb.append(i + 1);
		}		
		// (2) second sample infos
		for (int i = 0; i < replicates2; ++i) {
			sb.append(getSEP());
			sb.append("strand2");
			sb.append(i + 1);
			sb.append(getSEP());
			sb.append("bases2");
			sb.append(i + 1);
			sb.append(getSEP());
			sb.append("quals2");
			sb.append(i + 1);
		}

		sb.append(getSEP());
		sb.append("stat");
		
		sb.append(getSEP());
		sb.append("filter_info");

		return sb.toString();
	}
	
	@Override
	public String convert2String(final Result result) {
		final StringBuilder sb = new StringBuilder(pileupFormat.convert2String(result));
		// add unfiltered value
		sb.append(pileupFormat.getSEP());
		sb.append(result.getStatistic());
		
		sb.append(pileupFormat.getSEP());
		sb.append(result.getObject("filterInfo"));
		
		return sb.toString();
	}

	public char getCOMMENT() {
		return pileupFormat.getCOMMENT();
	}
	
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