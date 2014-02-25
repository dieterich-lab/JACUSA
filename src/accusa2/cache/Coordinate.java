package accusa2.cache;

import net.sf.samtools.CigarElement;

public class Coordinate {

	private int genomicPosition;
	private int readPosition;
	private CigarElement cigarElement;

	public Coordinate() {
		genomicPosition = -1;
		readPosition = -1;
	}

	public Coordinate(int genomicPosition, int readPosition, CigarElement cigarElement) {
		this.genomicPosition = genomicPosition;
		this.readPosition = readPosition;
		this.cigarElement = cigarElement;
	}
	
	/**
	 * @return the genomicPosition
	 */
	public int getGenomicPosition() {
		return genomicPosition;
	}

	/**
	 * @return the readPosition
	 */
	public int getReadPosition() {
		return readPosition;
	}

	/**
	 * @return the cigarElement
	 */
	public CigarElement getCigarElement() {
		return cigarElement;
	}

	/**
	 * @param genomicPosition the genomicPosition to set
	 */
	public void setGenomicPosition(int genomicPosition) {
		this.genomicPosition = genomicPosition;
	}

	/**
	 * @param readPosition the readPosition to set
	 */
	public void setReadPosition(int readPosition) {
		this.readPosition = readPosition;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setCigarElement(CigarElement cigarElement) {
		this.cigarElement = cigarElement;
	}

	public void set(int genomicPosition, int readPosition, CigarElement cigarElement) {
		this.genomicPosition = genomicPosition;
		this.readPosition = readPosition;
		this.cigarElement = cigarElement;
	}

}
