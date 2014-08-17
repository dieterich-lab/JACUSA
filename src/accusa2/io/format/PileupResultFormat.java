package accusa2.io.format;

import net.sf.samtools.SAMUtils;
import accusa2.cli.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.Pileup.STRAND;
import accusa2.process.phred2prob.Phred2Prob;

public class PileupResultFormat extends PileupFormat {

	private Parameters parameters;

	public PileupResultFormat(Parameters paramters) {
		super('A', "pileup like ACCUSA result format");
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

		for(final AbstractFilterFactory abstractPileupFilterFactory : parameters.getPileupBuilderFilters().getFilterFactories()) {
			sb.append(getSEP());
			sb.append("filtered_");
			sb.append(abstractPileupFilterFactory.getC());
		}

		if(parameters.getPileupBuilderFilters().hasFiters()) {
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
		sb.append(SEP);
		sb.append(value);
		return sb.toString();
	}
	
	@Override
	public double extractValue(String line) {
		String[] cols = line.split(Character.toString(SEP));
		return Double.parseDouble(cols[cols.length - 1]);
	}
	
	@Override
	protected void addPileups(StringBuilder sb, STRAND strand, Pileup[] pileups) {
		sb.append(SEP);
		sb.append(strand.character());
		
		for(Pileup pileup : pileups) {

			sb.append(SEP);
			
			for(int base : pileup.getAlleles()) {
				
				// print bases 
				for(int i = 0; i < pileup.getBaseCount()[base]; ++i) {
					sb.append(Pileup.BASES2[base]);
				}
			}

			sb.append(SEP);

			// print quals
			for(int base : pileup.getAlleles()) {
				for(byte qual = 0; qual < Phred2Prob.MAX_Q; ++qual) {

					int count = pileup.getQualCount(base, qual);
					if(count > 0) {
						// repeat count times
						for(int j = 0; j < count; ++j) {
							sb.append(SAMUtils.phredToFastq(qual));
						}
					}
				}
			}
		}
	}
	
}
