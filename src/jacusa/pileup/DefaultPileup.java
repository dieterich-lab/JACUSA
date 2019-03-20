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

		counts 		= new Counts(baseLength);
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

	public void setCounts(Counts counts) {
		this.counts = counts;
	}
	
	@Override
	public void addPileup(final Pileup pileup) {
		counts.addCounts(pileup.getCounts());
	}

	@Override
	public void substractPileup(final Pileup pileup) {
		counts.substract(pileup.getCounts());
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
		return counts.getCoverage();
	}

	@Override
	public int[] getAlleles() {
		// make this allele
		int[] alleles = new int[counts.getBaseLength()];
		int n = 0;
	
		for (int baseI = 0; baseI < counts.getBaseLength(); ++baseI) {
			if (counts.getBaseCount(baseI) > 0) {
				alleles[n] = baseI;
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

	public static Pileup[] flat(Pileup[] pileups, int[] variantBaseIs, int commonBaseI) {
		Pileup[] ret = new Pileup[pileups.length];
		for (int i = 0; i < pileups.length; ++i) {
			ret[i] = new DefaultPileup(pileups[i]);

			for (int variantBaseI : variantBaseIs) {
				ret[i].getCounts().add(commonBaseI, variantBaseI, pileups[i].getCounts());
				ret[i].getCounts().substract(variantBaseI, variantBaseI, pileups[i].getCounts());
			}
			
		}
		return ret;
	}
	
	public static Pileup[] empty(Pileup[] pileups) {
		Pileup[] ret = new Pileup[pileups.length];
		for (int i = 0; i < pileups.length; ++i) {
			final Pileup tmpPileup = pileups[i];
			ret[i] = new DefaultPileup(
					tmpPileup.getContig(),
					tmpPileup.getPosition(),
					tmpPileup.getStrand(),
					tmpPileup.getCounts().getBaseLength());
		}
		return ret;
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