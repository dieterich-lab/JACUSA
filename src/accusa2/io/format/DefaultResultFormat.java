package accusa2.io.format;

import accusa2.cli.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

// CHANGED
public class DefaultResultFormat extends AbstractResultFormat {

	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';

	private Parameters parameters;

	public DefaultResultFormat(Parameters parameters) {
		super('D', "ACCUSA2 default output");
		this.parameters = parameters;
	}

	@Override
	public String getHeader(ParallelPileup parallelPileup) {
		final StringBuilder sb = new StringBuilder();

		sb.append(COMMENT);

		// position (1-based)
		sb.append("contig");
		sb.append(getSEP());
		sb.append("position");
		sb.append(getSEP());

		// (1) first sample  infos
		addSampleHeader(sb, 1, parallelPileup.getNA());
		sb.append(getSEP());
		// (2) second sample  infos
		addSampleHeader(sb, 2, parallelPileup.getNB());

		sb.append(getSEP());
		// unfiltered value
		sb.append("unfiltered");

		// values from filters
		for(final AbstractFilterFactory abstractPileupFilterFactory : parameters.getFilterConfig().getFactories()) {
			sb.append(getSEP());
			sb.append("filtered_");
			sb.append(abstractPileupFilterFactory.getC());
		}

		/*
		// final value used to calculate STAT
		if(parameters.getPileupBuilderFilters().hasFiters()) {
			sb.append(getSEP());
			sb.append("filtered");
		}
		*/

		//add means and vars
		sb.append(getSEP());
		sb.append("meanA");
		sb.append(getSEP());
		sb.append("varA");
		sb.append(getSEP());
		sb.append("meanB");
		sb.append(getSEP());
		sb.append("varB");
		sb.append(getSEP());
		sb.append("meanAB");
		sb.append(getSEP());
		sb.append("varAB");
		
		// stat
		sb.append(getSEP());
		sb.append("stat");

		return sb.toString();
	}
	
	private void addSampleHeader(StringBuilder sb, int sample, int replicates) {
		sb.append("strand");
		sb.append(sample);
		sb.append(getSEP());
	
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

	private StringBuilder convert2StringHelper(final ParallelPileup parallelPileup) {
		final StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());

		// (1) first pileups
		addPileups(sb, parallelPileup.getStrandA(), parallelPileup.getPileupsA());
		// (2) second pileups
		addPileups(sb, parallelPileup.getStrandB(), parallelPileup.getPileupsB());
		
		return sb;
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		final StringBuilder sb = convert2StringHelper(parallelPileup);
		return sb.toString();		
	}
	
	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value) {
		final StringBuilder sb = convert2StringHelper(parallelPileup);

		// add unfiltered value
		sb.append(SEP);
		sb.append(value);

		return sb.toString();
	}
	
	/*
	 * Helper function
	 */
	private void addPileups(StringBuilder sb, STRAND strand, Pileup[] pileups) {
		// strand information
		sb.append(SEP);
		sb.append(strand.character());

		// output sample: Ax,Cx,Gx,Tx
		for (Pileup pileup : pileups) {
			sb.append(SEP);
			int baseI = 0;
			sb.append(pileup.getCounts().getBaseCount()[baseI]);
			++baseI;
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
		return Double.parseDouble(cols[cols.length - 1]);
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
