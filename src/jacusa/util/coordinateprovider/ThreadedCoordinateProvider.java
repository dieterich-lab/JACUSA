package jacusa.util.coordinateprovider;

import jacusa.util.Coordinate;

import java.io.File;
import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class ThreadedCoordinateProvider implements CoordinateProvider {

	private CoordinateProvider cp;
	private int windowSize;

	private Coordinate buffer;
	private Coordinate current;

	private SAMFileReader[] readers;
	
	public ThreadedCoordinateProvider(final CoordinateProvider cp, String[] pathnames1, String[] pathnames2, final int windowSize) {
		this.cp = cp;
		this.windowSize = windowSize;
		
		readers = new SAMFileReader[pathnames1.length + pathnames2.length];
		System.arraycopy(initReaders(pathnames1), 0, readers, 0, pathnames1.length);
		System.arraycopy(initReaders(pathnames2), 0, readers, pathnames1.length, pathnames2.length);
	}

	public ThreadedCoordinateProvider(final CoordinateProvider cp, String[] pathnames1, String[] pathnames2) {
		this(cp, pathnames1, pathnames2, 100000);
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

		int start = tmp.getStart();
		for (SAMFileReader reader : readers) {
			SAMRecordIterator iterator = reader.query(coordinate.getSequenceName(), start, coordinate.getEnd(), false);

			boolean found = false;
			while (iterator.hasNext()) {
				SAMRecord record = iterator.next();
				if (! record.getReadUnmappedFlag() && ! record.getNotPrimaryAlignmentFlag()) {
					start = Math.max(start, record.getAlignmentStart());
					found = true;
					break;
				}
			}
			iterator.close();
			if (! found) {
				return null;
			}
		}

		final int end = Math.min(start + windowSize - 1, coordinate.getEnd());
		tmp.setStart(start);
		tmp.setEnd(end);

		coordinate.setStart(end + 1);
		return tmp;
	}
	
	/**
	 * 
	 * @param pathnames
	 * @return
	 */
	protected SAMFileReader[] initReaders(String[] pathnames) {
		SAMFileReader[] readers = new SAMFileReader[pathnames.length];
		for(int i = 0; i < pathnames.length; ++i) {
			readers[i] = initReader(pathnames[i]);
		}
		return readers;
	}

	/**
	 * 
	 * @param pathname
	 * @return
	 */
	protected SAMFileReader initReader(final String pathname) {
		SAMFileReader reader = new SAMFileReader(new File(pathname));
		// be silent
		reader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
		// disable memory mapping
		reader.enableIndexCaching(true);
		reader.enableIndexMemoryMapping(false);
		return reader;
	}

	protected void close(SAMFileReader[] readers) {
		for (SAMFileReader reader : readers) {
			if (reader != null) {
				reader.close();
			}
		}
	}

}