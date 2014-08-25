package accusa2.pileup;

import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;

public interface Pileup {

	 abstract void addPileup(Pileup pileup);
	 abstract void substractPileup(Pileup pileup);

	 abstract String getContig();
	 abstract int getPosition();
	 abstract STRAND getStrand();
	 abstract char getReferenceBase();
	 abstract int getCoverage();

	 abstract int[] getAlleles();

	 abstract int[] getBaseCount();
	 abstract int[][] getQualCount();

	 abstract void setContig(String contig);
	 abstract void setReferenceBase(char referenceBase);
	 abstract void setPosition(int position);
	 abstract void setStrand(STRAND strand);

	 abstract Counts getCounts();

	 abstract Pileup complement();
}