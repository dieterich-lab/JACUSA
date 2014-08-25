package accusa2.pileup;

import accusa2.pileup.DefaultPileup.STRAND;

public final class WindowedParallelPileup {

	private DefaultPileup pileup;

	private DefaultPileup pileupA;
	private DefaultPileup pileupB;

	private DefaultPileup[] pileupsA;
	private DefaultPileup[] pileupsB;

	private Pileup[] pileupsP;
	
	public WindowedParallelPileup(final WindowedParallelPileup parallelPileup) {
		this.pileup = new DefaultPileup(parallelPileup.getPooledPileup());

		this.pileupA = new DefaultPileup(parallelPileup.getPooledPileupA());
		this.pileupB = new DefaultPileup(parallelPileup.getPooledPileupB());

		pileupsA = new DefaultPileup[parallelPileup.getPileupsA().length];
		System.arraycopy(parallelPileup.getPileupsA(), 0, pileupsA, 0, parallelPileup.getPileupsA().length);
		pileupsB = new DefaultPileup[parallelPileup.getPileupsB().length];
		System.arraycopy(parallelPileup.getPileupsB(), 0, pileupsB, 0, parallelPileup.getPileupsB().length);

		System.arraycopy(pileupsA, 0, pileupsP, 0, pileupsA.length);
		System.arraycopy(pileupsB,0, pileupsP, pileupsA.length, pileupsB.length);
	}

	public WindowedParallelPileup(final int nA, final int nB) {
		pileupsA = new DefaultPileup[nA];
		pileupsB = new DefaultPileup[nB];
		pileupsP = new Pileup[nA + nB];
	}

	public WindowedParallelPileup(final Pileup[] pileupsA, final Pileup[] pileupsB) {
		pileupsP = new Pileup[pileupsA.length + pileupsB.length];
		
		this.pileupsA = new DefaultPileup[pileupsA.length];
		System.arraycopy(pileupsA, 0, this.pileupsA, 0, pileupsA.length);
		this.pileupsB = new DefaultPileup[pileupsB.length];
		System.arraycopy(pileupsB, 0, this.pileupsB, pileupsA.length, pileupsB.length);

		System.arraycopy(pileupsA, 0, pileupsP, 0, pileupsA.length);
		System.arraycopy(pileupsB,0, pileupsP, pileupsA.length, pileupsB.length);
		
		pileupB = null;
		pileupA = null;
		pileup = null;
	}

	public DefaultPileup[] getPileupsA() {
		return pileupsA;
	}

	public DefaultPileup[] getPileupsB() {
		return pileupsB;
	}

	public Pileup[] getPileupP() {
		return pileupsP;
	}
	
	public void setPileups1(final DefaultPileup[] pileups1) {
		if (this.pileupsA.length != pileups1.length) {
			Pileup[] tmpPileup = new Pileup[pileups1.length * this.pileupsB.length];
			System.arraycopy(pileupsB, 0, tmpPileup, pileups1.length, this.pileupsB.length);
			pileupsP = tmpPileup;
		}
		this.pileupsA = pileups1;
		System.arraycopy(this.pileupsA, 0, pileupsP, 0, this.pileupsA.length);
		
		pileupA = null;
		pileup = null;
	}

	public void setPileups2(final DefaultPileup[] pileups2) {
		if (this.pileupsB.length != pileups2.length) {
			Pileup[] tmpPileup = new Pileup[pileupsA.length * this.pileupsB.length];
			System.arraycopy(pileupsA, 0, tmpPileup, 0, this.pileupsA.length);
			pileupsP = tmpPileup;
		}
		this.pileupsB = pileups2;
		System.arraycopy(this.pileupsB, 0, pileupsP, this.pileupsA.length, this.pileupsB.length);
		
		pileupB = null;
		pileup = null;
	}

	public void reset() {
		pileupA = null;
		pileupB = null;
		pileup = null;
	}

	public int getN1() {
		return pileupsA.length;
	}

	public int getN2() {
		return pileupsB.length;
	}

	public int getN() {
		return pileupsP.length;
	}
	
	public String getContig() {
		return getPooledPileup().getContig();
	}

	public int getPosition() {
		return getPooledPileup().getPosition();
	}

	public STRAND getStrand1() {
		return getPooledPileupA().getStrand();
	}

	public STRAND getStrand2() {
		return getPooledPileupB().getStrand();
	}

	public boolean isValid() {
		return getN1() > 0 && getN2() > 0;
	}

	public DefaultPileup getPooledPileupA() {
		if(pileupA == null) {
			pileupA = new DefaultPileup(pileupsA[0].getContig(), pileupsA[0].getPosition(), pileupsA[0].getStrand());
			for(int i = 0; i < pileupsA.length; ++i) {
				pileupA.addPileup(pileupsA[i]);
			}
		}
		return pileupA;
	}
	
	public DefaultPileup getPooledPileupB() {
		if(pileupB == null) {
			pileupB = new DefaultPileup(pileupsB[0].getContig(), pileupsB[0].getPosition(), pileupsB[0].getStrand());
			for(int i = 0; i < pileupsB.length; ++i) {
				pileupB.addPileup(pileupsB[i]);
			}
		}
		return pileupB;
	}

	public DefaultPileup getPooledPileup() {
		if(pileup == null) {
			pileup = new DefaultPileup();
			pileup.setContig(getPooledPileupA().getContig());
			pileup.setPosition(getPooledPileupA().getPosition());

			pileup.addPileup(getPooledPileupA());
			pileup.addPileup(getPooledPileupB());

			// copy things
			// enusre there is enough space
			if (getN() != getN1() + getN2()) {
				pileupsP = new Pileup[getN1() + getN2()];
			}
			System.arraycopy(pileupsA, 0, pileupsP, 0, pileupsA.length);
			System.arraycopy(pileupsB, 0, pileupsP, pileupsA.length, pileupsB.length);
		}

		return pileup;
	}

	public boolean isHoHo() {
		return getPooledPileup().getAlleles().length == 1 && 
				getPooledPileupA().getAlleles().length == 1 && getPooledPileupB().getAlleles().length == 1;		
	}
	
	public boolean isHeHe() {
		return getPooledPileup().getAlleles().length == 2 && 
				getPooledPileupA().getAlleles().length == 2 && getPooledPileupB().getAlleles().length == 2;
	}

	public boolean isHoHe() {
		return getPooledPileup().getAlleles().length == 2 && 
				( getPooledPileupA().getAlleles().length == 1 && getPooledPileupB().getAlleles().length == 2 ||
				getPooledPileupA().getAlleles().length == 2 && getPooledPileupB().getAlleles().length == 1 );
	}
	
	/**
	 * 
	 * @return
	 */
	public int[] getVariantBases() {
		int n = 0;
		int[] alleles = getPooledPileup().getAlleles();
		for(int base : alleles) {
			if(getPooledPileupA().getCounts().getBaseCount(base) == 0 || getPooledPileupB().getCounts().getBaseCount(base) == 0) {
				++n;
			}
		}

		int[] variantBases = new int[n];
		int j = 0;
		for(int base : alleles) {
			if(getPooledPileupA().getCounts().getBaseCount(base) == 0 || getPooledPileupB().getCounts().getBaseCount(base) == 0) {
				variantBases[j] = base;
				++j;
			}
		}

		return variantBases;
	}
	
}
