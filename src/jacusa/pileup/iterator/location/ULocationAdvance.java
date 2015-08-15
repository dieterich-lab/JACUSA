package jacusa.pileup.iterator.location;

import jacusa.util.Location;

public class ULocationAdvance extends AbstractLocationAdvancer {

	protected ULocationAdvance(final Location loc) {
		super(loc, null);
	}

	@Override
	public void advance() {
		++loc1.genomicPosition;
	}
	
	
	@Override
	void advanceLocation1() {
		++loc1.genomicPosition;
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
	}
	
	@Override
	public void setLocation2(Location loc1) {}
	
}
