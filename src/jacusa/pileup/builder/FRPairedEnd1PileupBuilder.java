package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Coordinate;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

/**
 * @author Michael Piechotta
 *
 */
public class FRPairedEnd1PileupBuilder extends SingleEndStrandedPileupBuilder {

	public FRPairedEnd1PileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);
	}
	
	protected void processRecord(SAMRecord record) {
		if (record.getReadNegativeStrandFlag() && record.getSecondOfPairFlag() || 
				! record.getReadNegativeStrandFlag() && record.getFirstOfPairFlag()) {
			strand = STRAND.REVERSE;
		} else {
			strand = STRAND.FORWARD;
		}
		int i = strand.integer() - 1;
		// makes sure that for reads on the reverse strand the complement is stored in pileup and filters
		byte2int = byte2intAr[i]; 
		filterContainer = filterContainers[i];
		windowCache = windowCaches[i];

		super.processRecord(record);
	}

}