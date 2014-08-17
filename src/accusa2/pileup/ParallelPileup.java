package accusa2.pileup;

import java.util.Arrays;

import accusa2.pileup.Pileup.STRAND;
import accusa2.process.phred2prob.Phred2Prob;

public final class ParallelPileup {

	private Pileup pileup;

	private Pileup pileup1;
	private Pileup pileup2;

	private Pileup[] pileups1;
	private Pileup[] pileups2;

	private Pileup[] pileupsP;

	// stats
	private double[] mean;;
	private double[] mean1;
	private double[] mean2;
	
	private double[] var;
	private double[] var1;
	private double[] var2;
	
	public ParallelPileup(final ParallelPileup parallelPileup) {
		this.pileup = new Pileup(parallelPileup.getPooledPileup());

		this.pileup1 = new Pileup(parallelPileup.getPooledPileup1());
		this.pileup2 = new Pileup(parallelPileup.getPooledPileup2());

		pileups1 = new Pileup[parallelPileup.getPileups1().length];
		System.arraycopy(parallelPileup.getPileups1(), 0, pileups1, 0, parallelPileup.getPileups1().length);
		pileups2 = new Pileup[parallelPileup.getPileups2().length];
		System.arraycopy(parallelPileup.getPileups2(), 0, pileups2, 0, parallelPileup.getPileups2().length);

		System.arraycopy(pileups1, 0, pileupsP, 0, pileups1.length);
		System.arraycopy(pileups2,0, pileupsP, pileups1.length, pileups2.length);
		
		this.mean = parallelPileup.mean;
		this.mean1 = parallelPileup.mean1;
		this.mean2 = parallelPileup.mean2;
		
		this.var = parallelPileup.var;
		this.var1 = parallelPileup.var1;
		this.var2 = parallelPileup.var2;
	}

	public ParallelPileup(final int n1, final int n2) {
		pileups1 = new Pileup[n1];
		pileups2 = new Pileup[n2];
		pileupsP = new Pileup[n1 + n2];
	}

	public ParallelPileup(final Pileup[] pileups1, final Pileup[] pileups2) {
		pileupsP = new Pileup[pileups1.length + pileups2.length];
		
		this.pileups1 = new Pileup[pileups1.length];
		System.arraycopy(pileups1, 0, this.pileups1, 0, pileups1.length);
		this.pileups2 = new Pileup[pileups2.length];
		System.arraycopy(pileups2, 0, this.pileups2, pileups1.length, pileups2.length);

		System.arraycopy(pileups1, 0, pileupsP, 0, pileups1.length);
		System.arraycopy(pileups2,0, pileupsP, pileups1.length, pileups2.length);
		
		pileup2 = null;
		pileup1 = null;
		pileup = null;
		
		mean2 = null;
		mean1 = null;
		mean = null;
	
		var2 = null;
		var1 = null;
		var = null;
	}

	public Pileup[] getPileups1() {
		return pileups1;
	}

	public Pileup[] getPileups2() {
		return pileups2;
	}

	public Pileup[] getPileupP() {
		return pileupsP;
	}
	
	public void setPileups1(final Pileup[] pileups1) {
		if (this.pileups1.length != pileups1.length) {
			Pileup[] tmpPileup = new Pileup[pileups1.length * this.pileups2.length];
			System.arraycopy(pileups2, 0, tmpPileup, pileups1.length, this.pileups2.length);
			pileupsP = tmpPileup;
		}
		this.pileups1 = pileups1;
		System.arraycopy(this.pileups1, 0, pileupsP, 0, this.pileups1.length);
		
		pileup1 = null;
		pileup = null;
		
		mean1 = null;
		mean = null;
		
		var1 = null;
		var = null;
	}

	public void setPileups2(final Pileup[] pileups2) {
		if (this.pileups2.length != pileups2.length) {
			Pileup[] tmpPileup = new Pileup[pileups1.length * this.pileups2.length];
			System.arraycopy(pileups1, 0, tmpPileup, 0, this.pileups1.length);
			pileupsP = tmpPileup;
		}
		this.pileups2 = pileups2;
		System.arraycopy(this.pileups2, 0, pileupsP, this.pileups1.length, this.pileups2.length);
		
		pileup2 = null;
		pileup = null;
		
		mean2 = null;
		mean = null;
		
		var2 = null;
		var = null;
	}

	public void reset() {
		pileup1 = null;
		pileup2 = null;
		pileup = null;
		
		mean = null;
		mean2 = null;
		mean1 = null;
		
		var = null;
		var1 = null;
		var2 = null;
	}

	public int getN1() {
		return pileups1.length;
	}

	public int getN2() {
		return pileups2.length;
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
			mean1 = getMeanHelper(pileups1);
			var1 = getVarianceHelper(mean1, pileups1);
		}
		return pileup1;
	}

	/**
	 * 
	 * @param pileups
	 * @return
	 */
	// TODO check how to estimate mean
	private double[] getMeanHelper(Pileup[] pileups) {
		double[] mean = new double[Pileup.BASES2.length];
		Arrays.fill(mean, 0.0);
		for (Pileup pileup : pileups) {
			double[] prob = Phred2Prob.getInstance().convert2ProbVector(null, pileup);
			for (int baseI = 0; baseI < mean.length; baseI++) {
				mean[baseI] += prob[baseI];
			}
		}
		int n = pileups.length;
		if (n > 1) {
			for (int baseI = 0; baseI < mean.length; baseI++) {
				mean[baseI] /= n;
			}
		}
		return mean;
	}

	private double[] getVarianceHelper(double mean[], Pileup[] pileups) {
		int n = pileups.length;
		if (n == 1 ) {
			return var;
		}
	
		for (Pileup pileup : pileups) {
			double[] prob = Phred2Prob.getInstance().convert2ProbVector(null, pileup);
			for (int baseI = 0; baseI < mean.length; baseI++) {
				var[baseI] += Math.pow(mean[baseI] - prob[baseI], 2.0);
			}
		}

		for (int baseI = 0; baseI < mean.length; baseI++) {
			var[baseI] /= (n - 1);
		}

		return var;
	}
	
	/* Is this needed?
	private double[] getVarianceHelper(Pileup[] pileups) {
		int n = pileups.length;
		if (n == 1 ) {
			double[] var = new double[Pileup.BASES2.length];
			Arrays.fill(var, 0.0);
			return var;
		}
		
		double[] mean = getMeanHelper(pileups);
		return getVarianceHelper(mean, pileups);
	}
	*/
	
	public Pileup getPooledPileup2() {
		if(pileup2 == null) {
			pileup2 = new Pileup(pileups2[0].getContig(), pileups2[0].getPosition(), pileups2[0].getStrand());
			for(int i = 0; i < pileups2.length; ++i) {
				pileup2.addPileup(pileups2[i]);
			}
			mean2 = getMeanHelper(pileups2);
			var2 = getVarianceHelper(mean2, pileups2);
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

			// copy things
			// enusre there is enough space
			if (getN() != getN1() + getN2()) {
				pileupsP = new Pileup[getN1() + getN2()];
			}
			System.arraycopy(pileups1, 0, pileupsP, 0, pileups1.length);
			System.arraycopy(pileups2, 0, pileupsP, pileups1.length, pileups2.length);

			mean = getMeanHelper(pileupsP);
			var = getVarianceHelper(mean, pileupsP);
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
