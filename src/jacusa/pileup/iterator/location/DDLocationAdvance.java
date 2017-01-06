package jacusa.pileup.iterator.location;

import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Location;

public class DDLocationAdvance extends AbstractLocationAdvancer {

	protected DDLocationAdvance(final Location loc1, final Location loc2) {
		super(loc1, loc2);
	}

	@Override
	public void advance() {
		if (loc1.strand == STRAND.FORWARD) {
			loc1.strand = STRAND.REVERSE;
			loc2.strand = STRAND.REVERSE;
			return;
		} else {
			loc1.strand = STRAND.FORWARD;
			loc2.strand = STRAND.FORWARD;

			++loc1.genomicPosition;
			++loc2.genomicPosition;
		}
	}

	public boolean isValidStrand() {
		return loc1.strand == loc2.strand;
	}
	
	@Override
	void advanceLocation1() {
		strandedAdvanceLocation(loc1);
	}

	@Override
	void advanceLocation2() {
		strandedAdvanceLocation(loc2);
	}

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
	public void setLocation2(Location loc1) {
		loc2.genomicPosition = loc1.genomicPosition;
		loc1.strand = STRAND.FORWARD;
		loc2.strand = loc1.strand;
	}

}
