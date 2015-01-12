/**
 * 
 */
package jacusa.util.coordinateprovider;

import jacusa.util.Coordinate;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.sf.samtools.SAMSequenceRecord;

/**
 * @author mpiechotta
 *
 */
public class SAMCoordinateProvider implements CoordinateProvider {

	private Iterator<SAMSequenceRecord> it;

	/**
	 * 
	 */
	public SAMCoordinateProvider(List<SAMSequenceRecord> records) {
		it = records.iterator();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Coordinate next() {
		if(hasNext()) {
			SAMSequenceRecord record = it.next();
			return new Coordinate(record.getSequenceName(), 1, record.getSequenceLength());
		}
		return null;
	}

	@Override
	public void remove() {	
		// not needed
	}

	@Override
	public void close() throws IOException {
		// not needed
	}

}