package accusa2.pileup;

import java.util.ArrayList;
import java.util.List;

import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;

public class WindowedPileup implements Pileup {
	
	private Pileup aggregatedPileup;
	private List<Pileup> pileupsWithinWindow;

	public WindowedPileup(BaseConfig baseConfig) {
		aggregatedPileup 	= new DefaultPileup(baseConfig.getBases().length);
		pileupsWithinWindow = new ArrayList<Pileup>(50); // what is a good value?
	}

	public int getWindowSize() {
		if (pileupsWithinWindow.size() == 0) {
			return 0;
		}

		int n = pileupsWithinWindow.size();
		return pileupsWithinWindow.get(0).getPosition() - pileupsWithinWindow.get(n -1 ).getPosition();
	}
	
	public char[] getRefBases() {
		char[] refBases = new char[getWindowSize()];
		int i = 0;
		for (Pileup pileup : pileupsWithinWindow) {
			refBases[i] = pileup.getRefBase();
			++i;
		}
		return refBases;
	}

	@Override
	public void addPileup(Pileup pileup) {
		int windowPosition = getWindowPosition(pileup);
		pileup.addPileup(pileup);
		if (pileupsWithinWindow.get(windowPosition) == null) {
			pileupsWithinWindow.add(windowPosition, new DefaultPileup(pileup));
		}
		pileupsWithinWindow.get(windowPosition).addPileup(pileup);
	}

	@Override
	public void substractPileup(Pileup pileup) {
		int windowPosition = getWindowPosition(pileup);
		pileup.addPileup(pileup);
		if (pileupsWithinWindow.get(windowPosition) == null) {
			pileupsWithinWindow.add(windowPosition, new DefaultPileup(pileup));
		}
		pileupsWithinWindow.get(windowPosition).substractPileup(pileup);
	}

	@Override
	public String getContig() {
		return aggregatedPileup.getContig();
	}

	@Override
	public int getPosition() {
		return aggregatedPileup.getPosition();
	}

	@Override
	public STRAND getStrand() {
		return aggregatedPileup.getStrand();
	}

	@Override
	public char getRefBase() {
		return aggregatedPileup.getRefBase();
	}

	@Override
	public int getCoverage() {
		return aggregatedPileup.getCoverage();
	}

	@Override
	public int[] getAlleles() {
		return aggregatedPileup.getAlleles();
	}

	@Override
	public int[] getBaseCount() {
		return aggregatedPileup.getBaseCount();
	}

	@Override
	public int[][] getQualCount() {
		return aggregatedPileup.getQualCount();
	}

	@Override
	public void setContig(String contig) {
		aggregatedPileup.setContig(contig);
	}

	@Override
	public void setRefBase(char refBase) {
		aggregatedPileup.setRefBase(refBase);
	}

	@Override
	public void setPosition(int position) {
		aggregatedPileup.setPosition(position);		
	}

	@Override
	public void setStrand(STRAND strand) {
		aggregatedPileup.setStrand(strand);
	}

	@Override
	public Counts getCounts() {
		return aggregatedPileup.getCounts();
	}

	@Override
	public Pileup complement() {
		return aggregatedPileup.complement();
	}

	// assume user is smart and we can expect to have identical contigs
	protected int getWindowPosition(Pileup pileup) {
		return getPosition() - pileup.getPosition();
	}

	public List<Pileup> getPileupsWithinWindow() {
		return pileupsWithinWindow;
	}
	
}