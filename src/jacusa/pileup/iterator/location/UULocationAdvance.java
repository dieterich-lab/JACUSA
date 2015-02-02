package jacusa.pileup.iterator.location;

import jacusa.util.Location;

public class UULocationAdvance extends AbstractLocationAdvancer {

	protected UULocationAdvance(final Location loc1, final Location loc2) {
		super(loc1, loc2);
	}

	@Override
	public void advance() {
		++loc1.genomicPosition;
		++loc2.genomicPosition;
	}
	
	
	@Override
	void advanceLocation1() {
		++loc1.genomicPosition;
	}
	
	@Override
	void advanceLocation2() {
		++loc2.genomicPosition;
	}
	
	@Override
	public Location getLocation() {
		return loc1;
	}

	@Override
	public void setLocation1(Location loc2) {
		loc1.genomicPosition = loc2.genomicPosition;
	}
	
	@Override
	public void setLocation2(Location loc1) {
		loc2.genomicPosition = loc1.genomicPosition;
	}
	
}
