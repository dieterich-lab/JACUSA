package jacusa.filter.storage;

import java.util.Arrays;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public abstract class AbstractFilterStorage<T> {

	private char c;

	// count indel, read start/end, splice site as only 1!!!
	// this ensure that a base-call will only be counted once...
	private boolean[] visited;
	private T data;

	public AbstractFilterStorage(char c, int windowSize) {
		this.c = c;
		visited = new boolean[windowSize];
	}

	protected void setData(T data) {
		this.data = data;
	}

	public T getContainer() {
		return data;
	}

	public final char getC() {
		return c;
	}

	protected void processCigar(int genomicWindowStart, SAMRecord record) {
		// init	
		int readPosition 	= 0;
		int genomicPosition = record.getAlignmentStart();
		int windowPosition  = genomicPosition - genomicWindowStart;
		Arrays.fill(visited, false);

		// process CIGAR -> SP, INDELs
		for (final CigarElement cigarElement : record.getCigar().getCigarElements()) {

			switch(cigarElement.getOperator()) {

			/*
			 * handle insertion
			 */
			case I:
				processInsertion(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * handle alignment/sequence match and mismatch
			 */
			case M:
			case EQ:
			case X:
				processAlignmetMatch(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				genomicPosition += cigarElement.getLength();
				windowPosition  = genomicPosition - genomicWindowStart;
				break;

			/*
			 * handle hard clipping 
			 */
			case H:
				processHardClipping(windowPosition, readPosition, genomicPosition, cigarElement, record);
				break;

			/*
			 * handle deletion from the reference and introns
			 */
			case D:
				processDeletion(windowPosition, readPosition, genomicPosition, cigarElement, record);
				genomicPosition += cigarElement.getLength();
				windowPosition  = genomicPosition - genomicWindowStart;
				break;

			case N:
				processSkipped(windowPosition, readPosition, genomicPosition, cigarElement, record);
				genomicPosition += cigarElement.getLength();
				windowPosition  = genomicPosition - genomicWindowStart;
				break;

			/*
			 * soft clipping
			 */
			case S:
				processSoftClipping(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * silent deletion from padded sequence
			 */
			case P:
				processPadding(windowPosition, readPosition, genomicPosition, cigarElement, record);
				break;

			default:
				throw new RuntimeException("Unsupported Cigar Operator: " + cigarElement.getOperator().toString());
			}
		}
	}

	protected boolean[] getVisited() {
		return visited;
	}

	protected void processInsertion(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}
	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}
	protected void processHardClipping(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		System.err.println("Hard Clipping not handled yet!");
	}
	protected void processDeletion(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}
	protected void processSkipped(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}
	protected void processSoftClipping(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}
	protected void processPadding(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		System.err.println("Padding not handled yet!");
	}

	public void clearContainer() {};
	public void processRecord(int genomicWindowStart, SAMRecord record) {};

}