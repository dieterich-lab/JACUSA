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
public class FRPairedEnd1PileupBuilder extends AbstractStrandedPileupBuilder {

	public FRPairedEnd1PileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);
	}
	
	protected void processRecord(SAMRecord record) {
		/*
		 * 
		 * Taken from: https://www.biostars.org/p/64250/
	     * fr-firststrand:dUTP, NSR, NNSR Same as above except we enforce the rule that the right-most end of the fragment (in transcript coordinates) is the first sequenced (or only sequenced for single-end reads). Equivalently, it is assumed that only the strand generated during first strand synthesis is sequenced.
	     *  
		 */
		if (record.getFirstOfPairFlag() && record.getReadNegativeStrandFlag() || 
				record.getSecondOfPairFlag() && ! record.getReadNegativeStrandFlag()) {
			strand = STRAND.FORWARD;
		} else {
			strand = STRAND.REVERSE;
		}
		int i = strand.integer() - 1;
		// makes sure that for reads on the reverse strand the complement is stored in pileup and filters
		byte2int = byte2intAr[i]; 
		filterContainer = filterContainers[i];
		windowCache = windowCaches[i];

		super.processRecord(record);
	}

}