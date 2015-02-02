package jacusa.pileup;

import java.util.Arrays;

/**
 * Encapsulates a pileup column.
 * 
 * @author Sebastian Fröhler
 * @author Michael Piechotta
 * 
 * Michael Piechotta: refactored
 */
public class DefaultPileup implements Pileup {

	// container
	private String contig;
	private int position;
	private STRAND strand;
	private char refBase;

	private Counts counts;

	public DefaultPileup(final int baseLength) {
		contig 		= new String();
		position	= -1;
		strand		= STRAND.UNKNOWN;
		refBase		= 'N';

		counts 		= new Counts(baseLength, 0);
	}

	public DefaultPileup(final String contig, final int position, final STRAND strand, final int baseLength) {
		this(baseLength);
		this.contig = contig;
		this.position = position;
		this.strand = strand;
	}

	public DefaultPileup(final Pileup pileup) {
		contig 		= new String(pileup.getContig());
		position 	= pileup.getPosition();
		strand		= pileup.getStrand();
		refBase 	= pileup.getRefBase();

		counts		= (Counts)pileup.getCounts().clone();
	}

	@Override
	public Counts getCounts() {
		return counts;
	}

	@Override
	public void addPileup(final Pileup pileup) {
		counts.addCounts(pileup.getCounts());
	}

	@Override
	public void substractPileup(final Pileup pileup) {
		for (int baseI : pileup.getCounts().getBaseCount()) {
			counts.getBaseCount()[baseI] -= pileup.getCounts().getBaseCount()[baseI];
			for (int qualI = counts.getMinQualI(); qualI < pileup.getCounts().getQualCount()[baseI].length; ++qualI) {
				counts.getQualCount()[baseI][qualI] -= pileup.getCounts().getQualCount()[baseI][qualI];
			}
		}
	}

	@Override
	public String getContig() {
		return contig;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public STRAND getStrand() {
		return strand;
	}
	
	@Override
	public char getRefBase() {
		return refBase;
	}
	
	@Override
	public int getCoverage() {
		int sum = 0;
		for (int count : counts.getBaseCount()) {
			sum += count;
		}
		return sum;
	}

	@Override
	public int[] getAlleles() {
		// make this allele
		int[] alleles = new int[counts.getBaseCount().length];
		int n = 0;
	
		for (int i = 0; i < counts.getBaseCount().length; ++i) {
			if (counts.getBaseCount()[i] > 0) {
				alleles[n] = i;
				++n;
			}
		}
		return Arrays.copyOf(alleles, n);
	}

	public void setContig(final String contig) {
		this.contig = contig;
	}

	@Override
	public void setRefBase(final char referenceBase) {
		this.refBase = referenceBase;
	}

	@Override
	public void setPosition(final int position) {
		this.position = position;
	}

	@Override
	public void setStrand(final STRAND strand) {
		this.strand = strand;
	}

	public void invertStrand() {
		switch (getStrand()) {
		case FORWARD:
			setStrand(STRAND.REVERSE);
			break;

		case REVERSE:
			setStrand(STRAND.FORWARD);
			break;
			
		case UNKNOWN:
			return;
		}
	}

	@Override
	public Pileup invertBaseCount() {
		final Pileup complement = new DefaultPileup(this);

		// invert base and qual count
		complement.getCounts().invertCounts();
		return complement;
	}
	
	public enum STRAND {
		FORWARD(BaseConfig.STRAND_FORWARD_CHAR),REVERSE(BaseConfig.STRAND_REVERSE_CHAR),UNKNOWN(BaseConfig.STRAND_UNKNOWN_CHAR);
		
		final char c;
		final int i;
		
		private STRAND(char c) {
			this.c = c;
			
			switch(c) {

			case BaseConfig.STRAND_FORWARD_CHAR:
				i = 2;
				break;

			case BaseConfig.STRAND_REVERSE_CHAR:
				i = 1;
				break;

			default:
				i = 0;
				break;
			}
		}

		public final char character() {
	        return c;
	    }

		public final int integer() {
			return i;
		}
		
		public static STRAND getEnum(String s) {
			switch(s.charAt(0)) {

			case BaseConfig.STRAND_FORWARD_CHAR:
				return STRAND.FORWARD;

			case BaseConfig.STRAND_REVERSE_CHAR:
				return STRAND.REVERSE;				

			default:
				return STRAND.UNKNOWN;
			}
		}
	}

}