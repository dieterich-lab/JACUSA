package accusa2.pileup;



import java.util.Arrays;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Map;
//import java.util.Set;


import accusa2.process.phred2prob.Phred2Prob;

/**
 * Encapsulates a pileup column.
 * 
 * @author Sebastian Fröhler
 * @author Michael Piechotta
 * 
 * Michael Piechotta: refactored
 */
public final class Pileup {

	public static final char STRAND_FORWARD_CHAR = '+';
	public static final char STRAND_REVERSE_CHAR = '-';
	public static final char STRAND_UNKNOWN_CHAR = '.';
	
	public static final char[] BASES = {'A' ,'C', 'G', 'T', 'N'};
	public static final int[] COMPLEMENT = {3, 2, 1, 0, 4};

	public static final char[] BASES2 = {'A' ,'C', 'G', 'T'};

	public static final Map<Character, Integer> BASE2INT = new HashMap<Character, Integer>(BASES.length);
	// init base to integer
	static {
		for(int i = 0; i < BASES.length; ++i) {
			BASE2INT.put(BASES[i], i);
			BASE2INT.put(Character.toLowerCase(BASES[i]), i);
		}
	}
	public static final int LENGTH = BASES.length - 1; // ignore N

	public static final Map<Character, Character> BASES2_COMPLEMENT = new HashMap<Character, Character>();
	static {
		for(char c : BASES2) {
			BASES2_COMPLEMENT.put(c, BASES2[COMPLEMENT[BASE2INT.get(c)]]);
		}
	}

	private Pileup[] filteredPileups;

	// container
	private String contig;
	private int position;
	private STRAND strand;
	private char refBase;

	private int[][] qualCount;

	private int[] baseCount;

	public Pileup() {
		contig 	= new String();
		position= -1;
		strand	= STRAND.UNKNOWN;

		int n 	= Phred2Prob.MAX_Q;
		qualCount	= new int[BASES2.length][n];

		baseCount 	= new int[BASES2.length];

		filteredPileups = new Pileup[0];
	}

	public Pileup(String contig, int position, STRAND strand) {
		this();
		this.contig = contig;
		this.position = position;
		this.strand = strand;

		filteredPileups = new Pileup[0];
	}

	public void addBase(int base, byte qual) {
		++qualCount[base][qual];
		++baseCount[base];
		//quals.add(qual);
	}

	public void removeBase(final int base, final byte qual) {
		--qualCount[base][qual];
		--baseCount[base];
	}

	public int getQualCount(final int base, final byte qual) {
		return qualCount[base][qual];
	}
	
	public int getBaseCount(final int base) {
		return baseCount[base];
	}

	public Pileup(final Pileup pileup) {
		filteredPileups = new Pileup[pileup.filteredPileups.length];
		for(int i = 0; i < pileup.filteredPileups.length; ++i) {
			filteredPileups[i] = new Pileup(pileup.filteredPileups[i]); 
		}

		contig 		= new String(pileup.getContig());
		position 	= pileup.getPosition();
		strand		= pileup.getStrand();
		refBase 	= pileup.getReferenceBase();

		baseCount		= pileup.baseCount.clone();
		qualCount		= pileup.qualCount.clone();
		for(int i = 0; i < pileup.qualCount.length; ++i) {
			qualCount[i] = pileup.qualCount[i].clone();
		}
	}

	public void addPileup(final Pileup pileup) {
		for(int i = 0; i < pileup.baseCount.length; ++i) {
			baseCount[i] += pileup.baseCount[i];
		}
		for(int i = 0; i < qualCount.length; ++i) {
			for(int j = 0; j < Phred2Prob.MAX_Q; ++j) {
				qualCount[i][j] += pileup.qualCount[i][j];
			}
		}
	}

	public void substractPileup(final int base, final Pileup pileup) {
		baseCount[base] -= pileup.baseCount[base];
		for(int j = 0; j < Phred2Prob.MAX_Q; ++j) {
			qualCount[base][j] -= pileup.qualCount[base][j];
		}
	}
	
	public void substractPileup(final Pileup pileup) {
		for(int i = 0; i < pileup.baseCount.length; ++i) {
			baseCount[i] -= pileup.baseCount[i];
		}
		for(int i = 0; i < qualCount.length; ++i) {
			for(int j = 0; j < Phred2Prob.MAX_Q; ++j) {
				qualCount[i][j] -= pileup.qualCount[i][j];
			}
		}
	}

	public void setFilteredPileups(final Pileup[] filteredPileups) {
		this.filteredPileups = filteredPileups;
	}
	
	public String getContig() {
		return contig;
	}

	public int getPosition() {
		return position;
	}

	public STRAND getStrand() {
		return strand;
	}
	
	public char getReferenceBase() {
		return refBase;
	}

	public int[] getBaseCount() {
		return baseCount;
	}
	
	public int[][] getQualCount() {
		return qualCount;
	}
	
	public int getCoverage() {
		int sum = 0;
		for(int i = 0; i < baseCount.length; ++i) {
			sum += baseCount[i];
		}
		return sum;
	}

	public int[] getAlleles() {
		int[] alleles = new int[baseCount.length];
		int n = 0;
		for(int i = 0; i < baseCount.length; ++i) {
			if(baseCount[i] > 0) {
				alleles[n] = i;
				++n;
			}
		}
		return Arrays.copyOf(alleles, n);
	}

	public Pileup[] getFilteredPileups() {
		return filteredPileups;
	}
	
	public void setContig(final String contig) {
		this.contig = contig;
	}

	public void setReferenceBase(final char referenceBase) {
		this.refBase = referenceBase;
	}

	public void setPosition(final int position) {
		this.position = position;
	}

	public void setStrand(final STRAND strand) {
		this.strand = strand;
	}

	public void setBaseCount(int[] baseCount) {
		this.baseCount = baseCount;
	}

	public void setQualCount(int [][] qualCount) {
		this.qualCount = qualCount;
	}

	// TEST
	public Pileup complement() {
		final Pileup complement = new Pileup(this);

		// invert orientation
		switch (complement.strand) {
		case FORWARD:
		case UNKNOWN:
			complement.strand = STRAND.REVERSE;
			break;

		case REVERSE:
			complement.strand = STRAND.FORWARD;
			break;
		}

		// invert base and qual count
		complement.invert();
		// adjust counts for filteredPileups
		for(Pileup filteredPileup : complement.filteredPileups) {
			filteredPileup.invert();
		}

		return complement;
	}

	// TEST
	private void invert() {
		int[] tmpBaseCount = new int[baseCount.length];
		int[][] tmpQualCount = new int[baseCount.length][Phred2Prob.MAX_Q];

		for(int base = 0; base < baseCount.length; ++base) {
			int complementaryBase = COMPLEMENT[base];

			// invert base count
			tmpBaseCount[complementaryBase] = baseCount[base];
			// invert qualCount
			tmpQualCount[complementaryBase] = qualCount[base];
		}

		baseCount = tmpBaseCount;
		qualCount = tmpQualCount;
	}

	public enum STRAND {
		FORWARD(STRAND_FORWARD_CHAR),REVERSE(STRAND_REVERSE_CHAR),UNKNOWN(STRAND_UNKNOWN_CHAR);
		final char c;

		private STRAND(char c) {
			this.c = c;
		}

		public char character() {
	        return c;
	    }

		public static STRAND getEnum(String s) {
			switch(s.charAt(0)) {

			case STRAND_FORWARD_CHAR:
				return STRAND.FORWARD;

			case STRAND_REVERSE_CHAR:
				return STRAND.REVERSE;				

			default:
				return STRAND.UNKNOWN;
			}
		}

	}

}
