package accusa2.filter.cache;

import java.util.Arrays;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.builder.WindowCache;

public abstract class AbstractCountFilterCache {

	private char c;
	protected WindowCache cache;
	protected boolean[] visited;
	protected BaseConfig baseConfig;
	
	public AbstractCountFilterCache(char c, AbstractParameters parameters) {
		this.c = c;

		int windowSize = parameters.getWindowSize();
		int baseLength = parameters.getBaseConfig().getBases().length;

		cache = new WindowCache(windowSize, baseLength);
		visited = new boolean[windowSize];
		Arrays.fill(visited, false);
		
		baseConfig = parameters.getBaseConfig();
	}

	protected void fillCache(int windowPosition, int length, int readPosition, SAMRecord record) {
		int offset = 0;

		if (readPosition < 0) {
			offset += Math.abs(readPosition);
			
			windowPosition += offset;
			readPosition += offset;
			length -= offset;
		}

		if (windowPosition < 0) {
			offset += Math.abs(windowPosition);
			
			windowPosition += offset;
			readPosition += offset;
			length -= offset;
		}

		for (int i = 0; i < length && windowPosition + i < cache.getWindowSize() && readPosition + i < record.getReadLength(); ++i) {
			windowPosition += i;
			readPosition += i;

			if (! visited[windowPosition]) {
				int baseI = baseConfig.getBaseI(record.getReadBases()[readPosition]);	

				// corresponds to N
				if (baseI < 0) {
					continue;
				}

				byte qual = record.getBaseQualities()[readPosition];
				cache.add(windowPosition, baseI, qual);
				visited[windowPosition] = true;
			}
		}
	}

	protected void processCigar(int genomicWindowStart, SAMRecord record) {
		// init
		int readPosition 	= 0;
		int genomicPosition = record.getAlignmentStart();
		int windowPosition  = genomicPosition - genomicWindowStart;
		Arrays.fill(visited, false);

		// process CIGAR -> SP, INDELs
		for(final CigarElement cigarElement : record.getCigar().getCigarElements()) {

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
				break;

			case N:
				processSkipped(windowPosition, readPosition, genomicPosition, cigarElement, record);
				genomicPosition += cigarElement.getLength();
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
	
	public final char getC() {
		return c;
	}

	public WindowCache getCache() {
		return cache;
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

	public abstract void processRecord(int genomicWindowStart, SAMRecord record);

}