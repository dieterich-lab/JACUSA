package jacusa.pileup;

import jacusa.pileup.DefaultPileup.STRAND;

public interface Pileup {

	 void addPileup(Pileup pileup);
	 void substractPileup(Pileup pileup);

	 String getContig();
	 int getPosition();
	 STRAND getStrand();
	 char getRefBase();
	 int getCoverage();

	 int[] getAlleles();

	 void setContig(String contig);
	 void setRefBase(char refBase);
	 void setPosition(int position);
	 void setStrand(STRAND strand);

	 Counts getCounts();

	 void invertStrand();
	 Pileup complement();

}