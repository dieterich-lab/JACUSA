package jacusa.pileup.iterator.location;

import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Location;

public class SLocationAdvance extends AbstractLocationAdvancer {

	protected SLocationAdvance(final Location loc) {
		super(loc, null);
	}

	@Override
	public void advance() {
		if (loc1.strand == STRAND.FORWARD) {
			loc1.strand = STRAND.REVERSE;
			return;
		} else {
			loc1.strand = STRAND.FORWARD;

			++loc1.genomicPosition;
		}
	}

	public boolean isValidStrand() {
		return true;
	}
	
	@Override
	void advanceLocation1() {
		strandedAdvanceLocation(loc1);
	}

	@Override
	void advanceLocation2() {}

	@Override
	public Location getLocation() {
		return loc1;
	}

	@Override
	public void setLocation1(Location loc2) {
		loc1.genomicPosition = loc2.genomicPosition;
		loc2.strand = STRAND.FORWARD;
		loc1.strand = loc2.strand;
	}
	
	@Override
	public void setLocation2(Location loc1) {}

}
