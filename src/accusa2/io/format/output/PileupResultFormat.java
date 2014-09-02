package accusa2.io.format.output;

import accusa2.cli.parameters.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.pileup.ParallelPileup;

public class PileupResultFormat extends AbstractResultFormat {

	private Parameters parameters;
	private PileupFormat pileupFormat; 
	
	public PileupResultFormat(Parameters paramters) {
		super('A', "pileup like ACCUSA result format");
		pileupFormat = new PileupFormat();
		this.parameters = paramters;
	}
	
	public String getHeader() {
		final StringBuilder sb = new StringBuilder();
		sb.append("#");

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

		sb.append("unfiltered");

		for(final AbstractFilterFactory abstractPileupFilterFactory : parameters.getFilterConfig().getFactories()) {
			sb.append(getSEP());
			sb.append("filtered_");
			sb.append(abstractPileupFilterFactory.getC());
		}

		if(parameters.getFilterConfig().hasFiters()) {
			sb.append(getSEP());
			sb.append("filtered");
		}

		sb.append(getSEP());
		sb.append("stat");
		return sb.toString();
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		StringBuilder sb = new StringBuilder(convert2String(parallelPileup));
		// add unfiltered value
		sb.append(pileupFormat.getSEP());
		sb.append(value);
		return sb.toString();
	}

	
	
	@Override
	public double extractValue(String line) {
		String[] cols = line.split(Character.toString(pileupFormat.getSEP()));
		return Double.parseDouble(cols[cols.length - 1]);
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		return pileupFormat.convert2String(parallelPileup);
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