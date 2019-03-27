package jacusa.filter.storage;

import java.util.ArrayList;
import java.util.List;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMRecord;

public class HomopolymerFilterStorage extends AbstractWindowFilterStorage {
	
	private int minLength;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public HomopolymerFilterStorage(
			final char c, 
			final int length, 
			final WindowCoordinates windowCoordinates,
			final SampleParameters sampleParameters,
			final AbstractParameters parameters) {
		super(c, windowCoordinates, sampleParameters, parameters);

		this.minLength = length;
	}

	private void check(final int refHpStart, final int hpLength, final int lastBase, final List<Integer> quals) {
		for (int i = 0; i < hpLength; ++i) {
			final int winPos = getContainer().getWindowCoordinates().convert2WindowPosition(refHpStart + i);
			if (winPos >= 0) {
				windowCache.addHighQualityBaseCall(winPos, lastBase, quals.get(i));
			}
		}
	}
	
	@Override
	public void processRecord(int genomicWindowStart, int[] byte2int, SAMRecord record) {
		for (final AlignmentBlock block : record.getAlignmentBlocks()) {
			final int refPos 	= block.getReferenceStart();
			final int readPos 	= block.getReadStart() - 1;
			final int length	= block.getLength();
			
			int lastBase				= -1;
			int refHpStart				= -1;
			int hpLength				= 0;
			final List<Integer> quals 	= new ArrayList<Integer>();
			
			for (int i = 0; i < length; ++i) {
				final int base = byte2int[record.getReadBases()[readPos + i]];
				if (base < 0) {
					if (hpLength >= minLength) {
						check(refHpStart, hpLength, lastBase, quals);
					}
					lastBase 	= -1;
					refHpStart 	= -1;
					hpLength	= 0;
					quals.clear();
				} else {
					final int qual = record.getBaseQualities()[readPos + i];
					if (base == lastBase){
						hpLength++;
						quals.add(qual);
					} else {
						if (hpLength >= minLength) {
							check(refHpStart, hpLength, lastBase, quals);
						}
						lastBase 	= base;
						refHpStart 	= refPos + i;
						hpLength	= 1;
						quals.clear();
						quals.add(qual);
					}
				}
			}
			if (hpLength >= minLength) {
				check(refHpStart, hpLength, lastBase, quals);
			}
		}
	}

	public int getLength() {
		return minLength;
	}

	public int getDistance() {
		return minLength;
	}
	
}