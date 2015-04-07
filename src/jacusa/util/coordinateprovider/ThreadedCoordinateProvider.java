package jacusa.util.coordinateprovider;

import jacusa.util.Coordinate;

import java.io.IOException;

public class ThreadedCoordinateProvider implements CoordinateProvider {

	private CoordinateProvider cp;
	private int windowSize;

	private Coordinate buffer;
	private Coordinate current;
	
	public ThreadedCoordinateProvider(final CoordinateProvider cp, final int windowSize) {
		this.cp = cp;
		this.windowSize = windowSize;
	}

	public ThreadedCoordinateProvider(final CoordinateProvider cp) {
		this(cp, 100000);
	}

	@Override
	public boolean hasNext() {
		if (current != null) {
			return true;
		}

		if (buffer == null && cp.hasNext()) {
			buffer = cp.next();
		}
		if (buffer != null) {
			current = advance(buffer);
			if (current == null) {
				buffer = null;
				return hasNext();
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public Coordinate next() {
		Coordinate tmp = null;
		if (hasNext()) {
			tmp = new Coordinate(current);
			current = null;
		}
	
		return tmp;
	}

	@Override
	public void remove() {
		cp.remove();
	}

	@Override
	public void close() throws IOException {
		cp.close();
	}

	private Coordinate advance(final Coordinate coordinate) {
		final Coordinate tmp = new Coordinate(coordinate);
		if (coordinate.getStart() > coordinate.getEnd()) {
			return null; 
		}
		final int start = tmp.getStart();
		final int end = Math.min(start + windowSize - 1, coordinate.getEnd());
		tmp.setStart(start);
		tmp.setEnd(end);

		coordinate.setStart(end + 1);
		return tmp;
	}

}