package accusa2.pileup;

import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;

public final class DefaultParallelPileup implements ParallelPileup {
	
	private String contig;
	private int position;
	private STRAND strand;
	
	private Pileup pileupA;
	private Pileup pileupB;
	private Pileup pileupP;
	
	private Pileup[] pileupsA;
	private Pileup[] pileupsB;
	private Pileup[] pileupsP;

	private Counts[][] filterCountsA;
	private Counts[][] filterCountsB;

	public DefaultParallelPileup() {
		contig = new String();
		position = -1;
		strand = STRAND.UNKNOWN;
	}

	public DefaultParallelPileup(final ParallelPileup parallelPileup) {
		this.contig 	= parallelPileup.getContig();
		this.position 	= parallelPileup.getPosition();
		this.strand 	= parallelPileup.getStrand();
		
		this.pileupA = new DefaultPileup(parallelPileup.getPooledPileupA());
		this.pileupB = new DefaultPileup(parallelPileup.getPooledPileupB());
		this.pileupP = new DefaultPileup(parallelPileup.getPooledPileup());

		int nA = parallelPileup.getPileupsA().length;
		pileupsA = new Pileup[nA];
		System.arraycopy(parallelPileup.getPileupsA(), 0, pileupsA, 0, nA);
		int nB = parallelPileup.getPileupsB().length;
		pileupsB = new Pileup[nB];
		System.arraycopy(parallelPileup.getPileupsB(), 0, pileupsB, 0, nB);

		pileupsP = new Pileup[nA + nB];
		System.arraycopy(pileupsA, 0, pileupsP, 0, nA);
		System.arraycopy(pileupsB, 0, pileupsP, nA, nB);

		if (parallelPileup.getFilterCountsA() != null && parallelPileup.getFilterCountsA().length > 0) {
			filterCountsA = new Counts[nA][parallelPileup.getFilterCountsA().length];
			copyFilterCounts(nA, parallelPileup.getFilterCountsA(), filterCountsA);
		}
		if (parallelPileup.getFilterCountsB() != null && parallelPileup.getFilterCountsB().length > 0) {
			filterCountsB = new Counts[nB][parallelPileup.getFilterCountsB().length];
			copyFilterCounts(nB, parallelPileup.getFilterCountsB(), filterCountsB);
		}
	}

	private void copyFilterCounts(int n, Counts[][] filterCountsSrc, Counts[][] filterCountsDest) {
		for (int pileupI = 0; pileupI < n; ++pileupI) {
			for (int filterCountsI = 0; filterCountsI < filterCountsSrc[pileupI].length; ++filterCountsI) {
				filterCountsDest[pileupI][filterCountsI] = (Counts)filterCountsSrc[pileupI][filterCountsI].clone();
			}
		}
	}
	
	public DefaultParallelPileup(final int n1, final int n2) {
		this();
		pileupsA = new Pileup[n1];
		pileupsB = new Pileup[n2];
		pileupsP = new Pileup[n1 + n2];
	}

	public DefaultParallelPileup(final Pileup[] pileups1, final Pileup[] pileups2) {
		pileupsP = new Pileup[pileups1.length + pileups2.length];
		
		this.pileupsA = new Pileup[pileups1.length];
		System.arraycopy(pileups1, 0, this.pileupsA, 0, pileups1.length);
		this.pileupsB = new Pileup[pileups2.length];
		System.arraycopy(pileups2, 0, this.pileupsB, pileups1.length, pileups2.length);

		System.arraycopy(pileups1, 0, pileupsP, 0, pileups1.length);
		System.arraycopy(pileups2,0, pileupsP, pileups1.length, pileups2.length);
		
		pileupB = null;
		pileupA = null;
		pileupP = null;
	}

	@Override
	public Pileup[] getPileupsA() {
		return pileupsA;
	}

	@Override
	public Pileup[] getPileupsB() {
		return pileupsB;
	}

	@Override
	public Pileup[] getPileupsP() {
		if (pileupsP == null) {
			pileupsP = new Pileup[getNA() + getNB()];

			if (getNA() > 0) {
				System.arraycopy(pileupA, 0, pileupsA, 0, getNA());
			}
			
			if (getNB() > 0) {
				System.arraycopy(pileupB, 0, pileupsA, getNA(), getNB());
			}
		}

		return pileupsP;
	}

	@Override
	public void setPileupsA(final Pileup[] pileupsA) {
		this.pileupsA = pileupsA;
		
		pileupA = null;
		pileupP = null;
		pileupsP = null;
	}

	@Override
	public void setPileupsB(final Pileup[] pileupsB) {
		this.pileupsB = pileupsB;
		
		pileupB = null;
		pileupP = null;
		pileupsP = null;
	}
	
	@Override
	public Counts[][] getFilterCountsA() {
		return filterCountsA;
	}

	@Override
	public Counts[][] getFilterCountsB() {
		return filterCountsB;
	}
	
	@Override
	public void reset() {
		pileupA = null;
		pileupB = null;
		pileupP = null;
	}

		@Override
	public int getNA() {
		return pileupsA.length;
	}

	@Override
	public int getNB() {
		return pileupsB.length;
	}

	@Override
	public int getN() {
		return pileupsP.length;
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
	public void setContig(String contig) {
		this.contig = contig;
	}
	
	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void setStrand(STRAND strand) {
		this.strand = strand;
	}

	@Override
	public STRAND getStrandA() {
		return getPooledPileupA().getStrand();
	}

	@Override
	public STRAND getStrandB() {
		return getPooledPileupB().getStrand();
	}

	@Override
	public boolean isValid() {
		return getNA() > 0 && getNB() > 0;
	}

	@Override
	public Pileup getPooledPileupA() {
		if(pileupA == null) {
			pileupA = new DefaultPileup(pileupsA[0].getContig(), pileupsA[0].getPosition(), pileupsA[0].getStrand(), pileupsA[0].getBaseCount().length);
			for(int i = 0; i < pileupsA.length; ++i) {
				pileupA.addPileup(pileupsA[i]);
			}
		}
		return pileupA;
	}
	
	@Override
	public Pileup getPooledPileupB() {
		if(pileupB == null) {
			pileupB = new DefaultPileup(pileupsB[0].getContig(), pileupsB[0].getPosition(), pileupsB[0].getStrand(), pileupsB[0].getBaseCount().length);
			for(int i = 0; i < pileupsB.length; ++i) {
				pileupB.addPileup(pileupsB[i]);
			}
		}
		return pileupB;
	}

	@Override
	public Pileup getPooledPileup() {
		if(pileupP == null) {
			pileupP = new DefaultPileup(getPooledPileupA().getBaseCount().length);
			pileupP.setContig(getPooledPileupA().getContig());
			pileupP.setPosition(getPooledPileupA().getPosition());

			pileupP.addPileup(getPooledPileupA());
			pileupP.addPileup(getPooledPileupB());
		}

		return pileupP;
	}

	@Override
	public int[] getVariantBases() {
		int n = 0;
		int[] alleles = getPooledPileup().getAlleles();
		for (int baseI : alleles) {
			int countA = getPooledPileupA().getCounts().getBaseCount(baseI);
			int countB = getPooledPileupB().getCounts().getBaseCount(baseI);
			int countP = getPooledPileup().getCounts().getBaseCount(baseI);

			if (countA == 0 && countB == countP || countB == 0 && countA == countP) {
				++n;
			}
		}

		int[] variantBases = new int[n];
		int j = 0;
		for (int baseI : alleles) {
			int countA = getPooledPileupA().getCounts().getBaseCount(baseI);
			int countB = getPooledPileupB().getCounts().getBaseCount(baseI);
			int countP = getPooledPileup().getCounts().getBaseCount(baseI);

			if (countA == 0 && countB == countP || countB == 0 && countA == countP) {
				variantBases[j] = baseI;
				++j;
			}
		}

		return variantBases;
	}

	@Override
	public void setFilterCountsA(Counts[][] counts) {
		this.filterCountsA = counts;
	}

	@Override
	public void setFilterCountsB(Counts[][] counts) {
		this.filterCountsB = counts;
	}

	public static boolean isHoHo(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length == 1 && 
				parallelPileup.getPooledPileupA().getAlleles().length == 1 && parallelPileup.getPooledPileupB().getAlleles().length == 1;		
	}
	
	public static boolean isHeHe(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length == 2 && 
				parallelPileup.getPooledPileupA().getAlleles().length == 2 && parallelPileup.getPooledPileupB().getAlleles().length == 2;
	}

	public static boolean isHoHe(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length == 2 && 
				(parallelPileup.getPooledPileupA().getAlleles().length == 1 && parallelPileup.getPooledPileupB().getAlleles().length == 2 ||
						parallelPileup.getPooledPileupA().getAlleles().length == 2 && parallelPileup.getPooledPileupB().getAlleles().length == 1 );
	}
	
}