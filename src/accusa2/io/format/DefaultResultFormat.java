package accusa2.io.format;



import accusa2.cli.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.pileup.Pileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup.STRAND;

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
	public String getHeader() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(COMMENT);

		// position (1-based)
		sb.append("contig");
		sb.append(getSEP());
		sb.append("position");
		sb.append(getSEP());

		// (1) first sample  infos
		sb.append("strand1");
		sb.append(getSEP());
		for(char base : Pileup.BASES2) {
			sb.append("base");
			sb.append(base);
			sb.append("1");
			sb.append(getSEP());
		}

		// (2) second sample  infos
		sb.append("strand2");
		sb.append(getSEP());
		for(char base : Pileup.BASES2) {
			sb.append("base");
			sb.append(base);
			sb.append("2");
			sb.append(getSEP());
		}
		
		// unfiltered value
		sb.append("unfiltered");

		// values from filters
		for(final AbstractFilterFactory abstractPileupFilterFactory : parameters.getPileupBuilderFilters().getFilterFactories()) {
			sb.append(getSEP());
			sb.append("filtered_");
			sb.append(abstractPileupFilterFactory.getC());
		}

		// final value used to calculate STAT
		if(parameters.getPileupBuilderFilters().hasFiters()) {
			sb.append(getSEP());
			sb.append("filtered");
		}

		// stat
		sb.append(getSEP());
		sb.append("stat");

		return sb.toString();
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());

		// (1) first pileups
		addPileups(sb, parallelPileup.getStrand1(), parallelPileup.getPileups1());
		// (2) second pileups
		addPileups(sb, parallelPileup.getStrand2(), parallelPileup.getPileups2());

		return sb.toString();		
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());

		// (1) first pileups
		addPileups(sb, parallelPileup.getStrand1(), parallelPileup.getPileups1());
		// (2) second pileups
		addPileups(sb, parallelPileup.getStrand2(), parallelPileup.getPileups2());

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

		// add each base according to order in PILEUP.BASES2
		// bases first than pileup(s)
		// A_1,A_2 C_1,C_2 
		for(int base = 0; base < Pileup.BASES2.length ; ++base) {
			sb.append(SEP);
			
			// first pileup (and containter for later computation)
			Pileup pileup = pileups[0];
			// add base count
			sb.append(pileup.getBaseCount()[base]);
			
			// add resulting pileups
			for(int i = 1; i < pileups.length; ++i) {
				pileup = pileups[i];
				// add base count
				sb.append(SEP2);
				sb.append(pileup.getBaseCount()[base]);
			}
		}
	}

	/**
	 * Format:
	 * contig position strand1 basesA1 basesC1 basesG1 basesT1 strand2 basesA2 basesC2 basesG2 basesT2
	 */
	@Override
	public ParallelPileup extractParallelPileup(String line) {
		// ignore comment
		if(line.charAt(0) == getCOMMENT()) {
			return null;
		}

		// holds a line as an array 
		String[] cols = line.split(Character.toString(SEP));

		// coordinates
		String contig = cols[0];
		int position = Integer.parseInt(cols[1]);

		// (1) first sample infos
		STRAND strand1 = Pileup.STRAND.getEnum(cols[2]);
		String[] bases1 = {cols[3], cols[4], cols[5], cols[6]};
		// number of replicates
		int n1 = bases1[0].split(Character.toString(SEP2)).length;

		// (2) second sample infos
		STRAND strand2 = Pileup.STRAND.getEnum(cols[7]);
		String[] bases2 = {cols[8], cols[9], cols[10], cols[11]};
		// number of replicates
		int n2 = bases2[0].split(Character.toString(SEP2)).length;

		// container
		ParallelPileup parallelPileup = new ParallelPileup(n1, n2);
		// set first sample(s)
		parallelPileup.setPileups1(extractPileups(contig, position, n1, strand1, bases1));
		// set second sample(s)
		parallelPileup.setPileups2(extractPileups(contig, position, n2, strand2, bases2));
		
		return parallelPileup;
	}
	
	/**
	 * Helper function
	 * @param contig
	 * @param position
	 * @param replicates
	 * @param strand
	 * @param bases
	 * @return
	 */
	private Pileup[] extractPileups(String contig, int position, int replicates, STRAND strand, String[] bases) {
		Pileup[] pileups = new Pileup[replicates];
		// create pileups
		for(int i = 0; i < replicates; ++i) {
			Pileup pileup = new Pileup(contig, position, strand);
			pileups[i] = pileup;
		}

		// parse base count infos
		for(int base = 0; base < Pileup.BASES2.length; ++base) {
			String[] countsStr = bases[base].split(Character.toString(SEP2));
			for(int i = 0; i < replicates; ++i) {
				pileups[i].getBaseCount()[base] = Integer.parseInt(countsStr[i]);
			}
		}

		return pileups;
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
