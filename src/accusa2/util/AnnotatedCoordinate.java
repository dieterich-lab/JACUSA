package accusa2.util;

public class AnnotatedCoordinate {

	private String sequenceName;
	private int start;
	private int end;

	// ugly code
	// the index of coordinate in a dispatcher
	// necessary to preserve order of coordinates 
	private int coordinateIndex;

	public AnnotatedCoordinate() {
		sequenceName = new String();
		start = -1;
		end = -1;

		coordinateIndex = -1;
	}

	public AnnotatedCoordinate(String sequenceName, int start, int end) {
		this.sequenceName = sequenceName;
		this.start = start;
		this.end = end;
	}

	public AnnotatedCoordinate(String sequenceName, int start, int end, int i) {
		this.sequenceName = sequenceName;
		this.start	= start;
		this.end 	= end;

		this.coordinateIndex 		= i;
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

	public void setCoordinateIndex(int coordinateIndex) {
		this.coordinateIndex = coordinateIndex;
	}

	public int getIndex() {
		return coordinateIndex;
	}

	public String toString() {
		return sequenceName + "_" + start + "-" + end;
	}
	
}
