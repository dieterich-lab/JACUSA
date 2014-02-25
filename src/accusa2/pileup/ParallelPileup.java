package accusa2.pileup;

import accusa2.pileup.Pileup.STRAND;

// CHECKED
public final class ParallelPileup {

	private Pileup pileup;

	private Pileup pileup1;
	private Pileup pileup2;

	private Pileup[] pileups1;
	private Pileup[] pileups2;

	public ParallelPileup(final ParallelPileup parallelPileup) {
		this.pileup = new Pileup(parallelPileup.pileup);

		this.pileup1 = new Pileup(parallelPileup.pileup1);
		this.pileup2 = new Pileup(parallelPileup.pileup2);

		pileups1 = parallelPileup.pileups1.clone();
		pileups2 = parallelPileup.pileups2.clone();
	}

	public ParallelPileup(final int n1, final int n2) {
		pileups1 = new Pileup[n1];
		pileups2 = new Pileup[n2];
	}

	public ParallelPileup(final Pileup[] pileups1, final Pileup[] pileups2) {
		setPileups1(pileups1);
		setPileups2(pileups2);
	}

	public Pileup[] getPileups1() {
		return pileups1;
	}

	public Pileup[] getPileups2() {
		return pileups2;
	}

	public void setPileups1(final Pileup[] pileups1) {
		this.pileups1 = pileups1;
		pileup1 = null;
		pileup = null;
	}

	public void setPileups2(final Pileup[] pileups2) {
		this.pileups2 = pileups2;
		pileup2 = null;
		pileup = null;
	}

	public void reset() {
		pileup1 = null;
		pileup2 = null;
		pileup = null;
	}

	public int getN1() {
		return pileups1.length;
	}

	public int getN2() {
		return pileups2.length;
	}

	public String getContig() {
		return getPooledPileup().getContig();
	}

	public int getPosition() {
		return getPooledPileup().getPosition();
	}

	public STRAND getStrand1() {
		return getPooledPileup1().getStrand();
	}

	public STRAND getStrand2() {
		return getPooledPileup2().getStrand();
	}

	public boolean isValid() {
		return getN1() > 0 && getN2() > 0;
	}

	public Pileup getPooledPileup1() {
		if(pileup1 == null) {
			pileup1 = new Pileup(pileups1[0].getContig(), pileups1[0].getPosition(), pileups1[0].getStrand());
			for(int i = 0; i < pileups1.length; ++i) {
				pileup1.addPileup(pileups1[i]);
			}
		}
		return pileup1;
	}

	public Pileup getPooledPileup2() {
		if(pileup2 == null) {
			pileup2 = new Pileup(pileups2[0].getContig(), pileups2[0].getPosition(), pileups2[0].getStrand());
			for(int i = 0; i < pileups2.length; ++i) {
				pileup2.addPileup(pileups2[i]);
			}
		}
		return pileup2;
	}

	public Pileup getPooledPileup() {
		if(pileup == null) {
			pileup = new Pileup();
			pileup.setContig(getPooledPileup1().getContig());
			pileup.setPosition(getPooledPileup1().getPosition());

			pileup.addPileup(getPooledPileup1());
			pileup.addPileup(getPooledPileup2());
		}

		return pileup;
	}

	public boolean isHoHo() {
		return getPooledPileup().getAlleles().length == 1 && 
				getPooledPileup1().getAlleles().length == 1 && getPooledPileup2().getAlleles().length == 1;		
	}
	
	public boolean isHeHe() {
		return getPooledPileup().getAlleles().length == 2 && 
				getPooledPileup1().getAlleles().length == 2 && getPooledPileup2().getAlleles().length == 2;
	}

	public boolean isHoHe() {
		return getPooledPileup().getAlleles().length == 2 && 
				( getPooledPileup1().getAlleles().length == 1 && getPooledPileup2().getAlleles().length == 2 ||
				getPooledPileup1().getAlleles().length == 2 && getPooledPileup2().getAlleles().length == 1 );
	}
	
	/**
	 * 
	 * @return
	 */
	public int[] getVariantBases() {
		int n = 0;
		int[] alleles = getPooledPileup().getAlleles();
		for(int base : alleles) {
			if(getPooledPileup1().getBaseCount(base) == 0 || getPooledPileup2().getBaseCount(base) == 0) {
				++n;
			}
		}

		int[] variantBases = new int[n];
		int j = 0;
		for(int base : alleles) {
			if(getPooledPileup1().getBaseCount(base) == 0 || getPooledPileup2().getBaseCount(base) == 0) {
				variantBases[j] = base;
				++j;
			}
		}

		return variantBases;
	}
	
}
