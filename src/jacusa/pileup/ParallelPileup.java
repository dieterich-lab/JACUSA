package jacusa.pileup;

import jacusa.pileup.DefaultPileup.STRAND;

public interface ParallelPileup {

	abstract Pileup[] getPileups1();
	abstract Pileup[] getPileups2();
	abstract Pileup[] getPileupsP();

	abstract void setPileups1(Pileup[] pileups1);
	abstract void setPileups2(Pileup[] pileups2);

	abstract void reset();

	abstract int getN1();
	abstract int getN2();
	abstract int getN();

	abstract String getContig();
	abstract int getStart();
	abstract int getEnd();
	abstract STRAND getStrand();

	abstract void setContig(String contig);
	abstract void setStart(int start);
	abstract void setEnd(int end);
	abstract void setStrand(STRAND strand);

	abstract STRAND getStrand1();
	abstract STRAND getStrand2();

	abstract boolean isValid();

	abstract Pileup getPooledPileup1();
	abstract Pileup getPooledPileup2();
	abstract Pileup getPooledPileup();

	abstract int[] getVariantBaseIs();

	String prettyPrint();
}