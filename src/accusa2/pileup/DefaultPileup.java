package accusa2.pileup;

import java.util.Arrays;


import accusa2.cli.Parameters;
import accusa2.process.phred2prob.Phred2Prob;

/**
 * Encapsulates a pileup column.
 * 
 * @author Sebastian Fröhler
 * @author Michael Piechotta
 * 
 * Michael Piechotta: refactored
 */
public final class DefaultPileup implements Pileup {

	// container
	private String contig;
	private int position;
	private STRAND strand;
	private char refBase;

	private Counts counts;

	private int minQualI = Parameters.getInstance().getMinBASQ();

	public DefaultPileup() {
		contig 		= new String();
		position	= -1;
		strand		= STRAND.UNKNOWN;

		counts 		= new Counts();
	}
	
	public DefaultPileup(final String contig, final int position, final STRAND strand) {
		this();
		this.contig = contig;
		this.position = position;
		this.strand = strand;
	}

	public DefaultPileup(final Pileup pileup) {
		contig 		= new String(pileup.getContig());
		position 	= pileup.getPosition();
		strand		= pileup.getStrand();
		refBase 	= pileup.getReferenceBase();

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
		for(int baseI : pileup.getCounts().baseCount) {
			counts.baseCount[baseI] -= pileup.getCounts().baseCount[baseI];
			for(int qualI = minQualI; qualI < pileup.getCounts().getQualCount()[baseI].length; ++qualI) {
				counts.qualCount[baseI][qualI] -= pileup.getCounts().getQualCount()[baseI][qualI];
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
	public char getReferenceBase() {
		return refBase;
	}

	@Override
	public int[] getBaseCount() {
		return counts.baseCount;
	}
	
	@Override
	public int[][] getQualCount() {
		return counts.qualCount;
	}
	
	@Override
	public int getCoverage() {
		int sum = 0;
		for(int count : counts.baseCount) {
			sum += count;
		}
		return sum;
	}

	@Override
	public int[] getAlleles() {
		// make this allele
		int[] alleles = new int[counts.baseCount.length];
		int n = 0;
	
		for(int i = 0; i < counts.baseCount.length; ++i) {
			if(counts.baseCount[i] > 0) {
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
	public void setReferenceBase(final char referenceBase) {
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

	@Override
	public Pileup complement() {
		final Pileup complement = new DefaultPileup(this);

		// invert orientation
		switch (complement.getStrand()) {
		case FORWARD:
		case UNKNOWN:
			complement.setStrand(STRAND.REVERSE);
			break;

		case REVERSE:
			complement.setStrand(STRAND.FORWARD);
			break;
		}

		// invert base and qual count
		complement.getCounts().invertCounts();
		return complement;
	}
	
	public enum STRAND {
		FORWARD(BaseConfig.STRAND_FORWARD_CHAR),REVERSE(BaseConfig.STRAND_REVERSE_CHAR),UNKNOWN(BaseConfig.STRAND_UNKNOWN_CHAR);
		final char c;

		private STRAND(char c) {
			this.c = c;
		}

		public char character() {
	        return c;
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

	public class Counts implements Cloneable {

		// container
		private int[] baseCount;
		private int[][] qualCount;

		public Counts() {
			int baseLength = Parameters.getInstance().getBaseConfig().getBases().length;
			baseCount 	= new int[baseLength];
			qualCount	= new int[baseLength][Phred2Prob.MAX_Q];
		}

		public Counts(Counts count) {
			this();
			System.arraycopy(count.baseCount, 0, this.baseCount, 0, count.baseCount.length);

			for(int baseI = minQualI; baseI < count.baseCount.length; ++baseI) {
				System.arraycopy(count.qualCount[baseI], 0, qualCount[baseI], 0, count.qualCount[baseI].length);
			}
		}
		
		public Counts(final int[] baseCount, final int[][] qualCount) {
			this.baseCount = baseCount;
			this.qualCount = qualCount;
		}

		public void addBase(final int base, final byte qual) {
			++baseCount[base];
			++qualCount[base][qual];
		}

		public void removeBase(final int base, final byte qual) {
			--baseCount[base];
			--qualCount[base][qual];
		}

		public int getQualCount(final int base, final byte qual) {
			return qualCount[base][qual];
		}
		
		public int getBaseCount(final int base) {
			return baseCount[base];
		}

		public void addCounts(final Counts counts) {
			for(int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
				baseCount[baseI] += counts.baseCount[baseI];
			}
			
			for(int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
				for (int qualI = minQualI; qualI < counts.qualCount[baseI].length; ++qualI) {
					qualCount[baseI][qualI] += counts.qualCount[baseI][qualI];
				}
			}
		}

		public void substract(final int baseI, final Counts counts) {
			baseCount[baseI] -= counts.baseCount[baseI];

			for(int qualI = minQualI; qualI < counts.qualCount[baseI].length; ++qualI) {
				qualCount[baseI][qualI] -= counts.qualCount[baseI][qualI];
			}
		}

		public void substract(final Counts counts) {
			for(int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
				baseCount[baseI] -= counts.baseCount[baseI];
			}
			
			for(int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
				for(int qualI = minQualI; qualI < counts.qualCount[baseI].length; ++qualI) {
					qualCount[baseI][qualI] -= counts.qualCount[baseI][qualI];
				}
			}
		}

		public int[] getBaseCount() {
			return baseCount;
		}
		
		public int[][] getQualCount() {
			return qualCount;
		}

		public void setBaseCount(int[] baseCount) {
			this.baseCount = baseCount;
		}

		public void setQualCount(int [][] qualCount) {
			this.qualCount = qualCount;
		}

		public void invertCounts() {
			int[] tmpBaseCount = new int[baseCount.length];
			int[][] tmpQualCount = new int[baseCount.length][Phred2Prob.MAX_Q];

			for(int baseI = 0; baseI < baseCount.length; ++baseI) {
				// int complementaryBase = Bases.COMPLEMENT[base];
				int complementaryBaseI = baseCount.length - baseI;  

				// invert base count
				tmpBaseCount[complementaryBaseI] = baseCount[baseI];
				// invert qualCount
				tmpQualCount[complementaryBaseI] = qualCount[baseI];
			}

			baseCount = tmpBaseCount;
			qualCount = tmpQualCount;
		}

		@Override
		public Object clone() {
			return new Counts(this);
		}

	}

}