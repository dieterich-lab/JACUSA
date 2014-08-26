package accusa2.pileup;

import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;

public interface ParallelPileup {

	abstract Pileup[] getPileupsA();
	abstract Pileup[] getPileupsB();
	abstract Pileup[] getPileupsP();

	abstract void setPileupsA(Pileup[] pileupsA);
	abstract void setPileupsB(Pileup[] pileupsB);

	abstract void reset();

	abstract int getNA();
	abstract int getNB();
	abstract int getN();

	abstract String getContig();
	abstract int getPosition();
	abstract STRAND getStrand();

	abstract void setContig(String contig);
	abstract void setPosition(int position);
	abstract void setStrand(STRAND strand);

	abstract STRAND getStrandA();
	abstract STRAND getStrandB();

	abstract boolean isValid();

	abstract Pileup getPooledPileupA();
	abstract Pileup getPooledPileupB();
	abstract Pileup getPooledPileup();

	abstract void setFilterCountsA(Counts[][] counts);
	abstract void setFilterCountsB(Counts[][] counts);

	abstract Counts[][] getFilterCountsA();
	abstract Counts[][] getFilterCountsB();

	abstract int[] getVariantBases();

}