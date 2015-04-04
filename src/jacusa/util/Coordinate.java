package jacusa.util;

public class Coordinate {

	private String sequenceName;
	private int start;
	private int end;

	public Coordinate() {
		sequenceName = new String();
		start = -1;
		end = -1;
	}

	public Coordinate(Coordinate coordinate) {
		sequenceName = new String(coordinate.sequenceName);
		start 		= coordinate.start;
		end 		= coordinate.end;
	}
	
	public Coordinate(String sequenceName, int start, int end) {
		this.sequenceName = sequenceName;
		this.start = start;
		this.end = end;
	}
	
	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int begin) {
		this.start = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String toString() {
		return sequenceName + "_" + start + "-" + end;
	}
	
}