package jacusa.pileup.iterator.location;

import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Location;

public abstract class AbstractLocationAdvancer {

	protected Location loc1;
	protected Location loc2;
	
	public AbstractLocationAdvancer(final Location loc1, final Location loc2) {
		this.loc1 = loc1;
		this.loc2 = loc2;
	}
	
	public static AbstractLocationAdvancer getInstance(boolean isStranded1, Location loc1) {
		if (isStranded1) {
			return new SLocationAdvance(loc1);
		} else if (! isStranded1) {
			return new ULocationAdvance(loc1);
		}
		
		return null;
	}
	
	public static AbstractLocationAdvancer getInstance(boolean isStranded1, Location loc1, boolean isStranded2, Location loc2) { 
		// create the correct LocationAdvancer
		if (isStranded1 && isStranded2) {
			return new SSLocationAdvance(loc1, loc2);
		} else if (! isStranded1 && ! isStranded2) {
			return new UULocationAdvance(loc1, loc2);
		} else if (isStranded1 && ! isStranded2) {
			return new SULocationAdvance(loc1, loc2);
		} else if (! isStranded1 && isStranded2) {
			return new USLocationAdvance(loc1, loc2);
		}
		
		return null;
	}
	
	public Location getLocation1() {
		return loc1;
	}
	
	public Location getLocation2() {
		return loc2;
	}
	
	public abstract void advance();
	public void advanceLocation(Location loc) {
		if (loc == loc1) {
			advanceLocation1();
		} else if (loc == loc2) {
			advanceLocation2();
		} else {
			throw new RuntimeException();
		}
	}
	abstract void advanceLocation1();
	abstract void advanceLocation2();
	
	public abstract void setLocation1(Location loc2);
	public abstract void setLocation2(Location loc1);

	public abstract Location getLocation();

	public boolean isValidStrand() {
		return true;
	}
	
	protected void strandedAdvanceLocation(Location location) {
		switch (location.strand) {
		case FORWARD:
			location.strand = STRAND.REVERSE;
			break;

		case REVERSE:
			++location.genomicPosition;
			location.strand = STRAND.FORWARD;
			break;

		case UNKNOWN:
		default:
			System.out.println("Ups");
			break;
		}
	}
	
}
