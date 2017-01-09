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
public class SingleEndStrandedPileupBuilder extends AbstractStrandedPileupBuilder {

	public SingleEndStrandedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);
	}

	protected void processRecord(SAMRecord record) {
		/*
		 * fr-firststrand [...] the first sequenced (or only sequenced for single-end reads) [...]
		 * 
		 * Taken from: https://www.biostars.org/p/64250/
    	 * fr-unstranded: Standard Illumina Reads from the left-most end of the fragment (in transcript coordinates) map to the transcript strand, and the right-most end maps to the opposite strand.
         * fr-firststrand:dUTP, NSR, NNSR Same as above except we enforce the rule that the right-most end of the fragment (in transcript coordinates) is the first sequenced (or only sequenced for single-end reads). Equivalently, it is assumed that only the strand generated during first strand synthesis is sequenced.
         * fr-secondstrand: Ligation, Standard SOLiD Same as above except we enforce the rule that the left-most end of the fragment (in transcript coordinates) is the first sequenced (or only sequenced for single-end reads). Equivalently, it is assumed that only the strand generated during second strand synthesis is sequenced.
         * 
         * Therefore strand is inverted
         * 
		 */
		if (record.getReadNegativeStrandFlag()) {
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