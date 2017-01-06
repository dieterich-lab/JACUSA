package jacusa.pileup.iterator.location;

import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Location;

public class UDLocationAdvance extends AbstractLocationAdvancer {

	protected UDLocationAdvance(final Location loc1, final Location loc2) {
		super(loc1, loc2);
	}

	@Override
	public void advance() {
		if (loc2.strand == STRAND.FORWARD) {
			loc2.strand = STRAND.REVERSE;
			return;
		} else {
			++loc1.genomicPosition;
			++loc2.genomicPosition;

			loc2.strand = STRAND.FORWARD;
		}
	}

	@Override
	void advanceLocation1() {
		++loc1.genomicPosition;
	}

	@Override
	void advanceLocation2() {
		strandedAdvanceLocation(loc2);
	}

	@Override
	public Location getLocation() {
		return loc2;
	}
	
	@Override
	public void setLocation1(Location loc2) {
		loc1.genomicPosition = loc2.genomicPosition;
	}
	
	@Override
	public void setLocation2(Location loc1) {
		loc2.genomicPosition = loc1.genomicPosition;
		loc2.strand = STRAND.FORWARD;
	}

}