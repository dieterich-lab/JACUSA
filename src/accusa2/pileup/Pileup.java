package accusa2.pileup;

import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;

public interface Pileup {

	 void addPileup(Pileup pileup);
	 void substractPileup(Pileup pileup);

	 String getContig();
	 int getPosition();
	 STRAND getStrand();
	 char getRefBase();
	 int getCoverage();

	 int[] getAlleles();

	 int[] getBaseCount();
	 int[][] getQualCount();

	 void setContig(String contig);
	 void setRefBase(char refBase);
	 void setPosition(int position);
	 void setStrand(STRAND strand);

	 Counts getCounts();

	 Pileup complement();

}