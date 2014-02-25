package accusa2.pileup;

import java.util.List;

import accusa2.cache.Coordinate;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public final class DecodedSamRecord {

	private final int readPostition;
	private final int genomicPostition;
	private final CigarElement cigarElement;
	private final List<Coordinate> indels;
	private final List<Coordinate> skipped;
	private final SAMRecord samRecord;

	public DecodedSamRecord(final int readPosition, final int genomicPosition, final CigarElement cigarElement, final List<Coordinate> indels, final List<Coordinate> skipped, final SAMRecord samRecord) {
		this.readPostition 	= readPosition;
		this.genomicPostition = genomicPosition;
		this.cigarElement	= cigarElement;
		this.indels 		= indels;
		this.skipped 		= skipped;
		this.samRecord 		= samRecord;
	}

	/**
	 * @return the readPostition
	 */
	public int getReadPostition() {
		return readPostition;
	}

	/**
	 * @return the genomicPostition
	 */
	public int getGenomicPostition() {
		return genomicPostition;
	}

	/**
	 * @return the cigarElement
	 */
	public CigarElement getCigarElement() {
		return cigarElement;
	}

	/**
	 * @return the indels
	 */
	public List<Coordinate> getIndels() {
		return indels;
	}

	/**
	 * @return the skipped
	 */
	public List<Coordinate> getSkipped() {
		return skipped;
	}
	
	/**
	 * @return the sAMrecord
	 */
	public SAMRecord getSAMrecord() {
		return samRecord;
	}

}
