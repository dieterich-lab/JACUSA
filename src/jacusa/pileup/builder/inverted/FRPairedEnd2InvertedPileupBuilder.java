package jacusa.pileup.builder.inverted;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.builder.AbstractStrandedPileupBuilder;
import jacusa.util.Coordinate;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

/**
 * @author Michael Piechotta
 *
 */
public class FRPairedEnd2InvertedPileupBuilder extends AbstractStrandedPileupBuilder {

	public FRPairedEnd2InvertedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);
	}
	
	// invert
	protected void processRecord(SAMRecord record) {
		if (record.getReadPairedFlag()) { // paired end
			if (record.getFirstOfPairFlag() && record.getReadNegativeStrandFlag() || 
					record.getSecondOfPairFlag() && ! record.getReadNegativeStrandFlag() ) {
				//strand = STRAND.REVERSE;
				strand = STRAND.FORWARD;
			} else {
				//strand = STRAND.FORWARD;
				strand = STRAND.REVERSE;
			}
		} else {
			if (record.getReadNegativeStrandFlag()) {
				strand = STRAND.FORWARD;
			} else {
				//strand = STRAND.FORWARD;
				strand = STRAND.REVERSE;
			}
		}
		int i = strand.integer() - 1;

		byte2int = byte2intAr[i]; 
		filterContainer = filterContainers[i];
		windowCache = windowCaches[i];

		super.processRecord(record);
	}

}