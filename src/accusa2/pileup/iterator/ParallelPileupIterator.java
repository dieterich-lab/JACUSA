package accusa2.pileup.iterator;


import java.util.Iterator;

import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;


/**
 * Specifies an interface for iterating through two assemblies in a position-wise, head-to-head manner.
 * 
 * An array of size two is returned iff, for a specific position, both assemblies provide assembled reads.
 * 
 * 25.02.2011 Michael Piechotta:
 * 	Added Closeable to list of inherited Interfaces
 * 
 * @author Sebastian Fröhler
 * @author Michael Piechotta
 */
public interface ParallelPileupIterator extends Iterator<ParallelPileup> {

	/**
	 * Returns the next pileup columns.
	 * 
	 * @return the next pileup columns
	 */
	public ParallelPileup next();

	/**
	 * Returns true iff more pileup columns are available to be returned.
	 * 
	 * @return true iff more pileup columns are available to be returned
	 */
	public boolean hasNext();
	
	public AnnotatedCoordinate getAnnotatedCoordinate();
}
