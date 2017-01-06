package jacusa.pileup.iterator.location;

import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Location;

public class DULocationAdvance extends AbstractLocationAdvancer {

	protected DULocationAdvance(final Location loc1, final Location loc2) {
		super(loc1, loc2);
	}

	@Override
	public void advance() {
		if (loc1.strand == STRAND.FORWARD) {
			loc1.strand = STRAND.REVERSE;
			return;
		} else {
			++loc1.genomicPosition;
			++loc2.genomicPosition;

			loc1.strand = STRAND.FORWARD;
		}
	}

	@Override
	void advanceLocation1() {
		strandedAdvanceLocation(loc1);
	}
	
	@Override
	void advanceLocation2() {
		loc2.genomicPosition++;
	}

	@Override
	public Location getLocation() {
		return loc1;
	}

	@Override
	public void setLocation1(Location loc2) {
		loc1.genomicPosition = loc2.genomicPosition;
		loc1.strand = STRAND.FORWARD;
	}
	
	@Override
	public void setLocation2(Location loc1) {
		loc2.genomicPosition = loc1.genomicPosition;
	}
	
}
