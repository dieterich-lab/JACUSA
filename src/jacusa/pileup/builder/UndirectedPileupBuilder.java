/**
 * 
 */
package jacusa.pileup.builder;


import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Coordinate;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

/**
 * @author michael
 *
 */
public class UndirectedPileupBuilder extends AbstractPileupBuilder {

	protected WindowCache windowCache;
	protected FilterContainer filterContainer;
	private int[] byte2int;

	protected STRAND strand;
	
	public UndirectedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);
		windowCache 	= new WindowCache(windowCoordinates, baseConfig.getBaseLength());
		filterContainer	= parameters.getFilterConfig().createFilterContainer(windowCoordinates, sampleParameters);

		final BaseConfig baseConfig = parameters.getBaseConfig();
		byte2int = baseConfig.getByte2Int();

		strand 			= STRAND.UNKNOWN;
	}

	public FilterContainer getFilterContainer(int windowPosition, STRAND strand) {
		return filterContainer;
	}

	public Pileup getPileup(int windowPosition, STRAND strand) {
		Pileup pileup = new DefaultPileup(
				windowCoordinates.getContig(), 
				windowCoordinates.getGenomicPosition(windowPosition), 
				strand, baseConfig.getBaseLength());

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(windowCache.getBaseCount(windowPosition));
		pileup.getCounts().setQualCount(windowCache.getQualCount(windowPosition));

		if (STRAND.REVERSE == strand) {
			pileup = pileup.complement();
		}

		return pileup;
	}

	@Override
	public void clearCache() {
		windowCache.clear();
		filterContainer.clear();
	}
	
	@Override
	protected void add2WindowCache(int windowPosition, int baseI, int qual, STRAND strand) {
		windowCache.add(windowPosition, baseI, qual);
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, STRAND.UNKNOWN) >= sampleParameters.getMinCoverage();
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		return windowCache.getCoverage(windowPosition);
	}

	protected void processRecord(SAMRecord record) {
		// init	
		int readPosition 	= 0;
		int genomicPosition = record.getAlignmentStart();
		int windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
		
		for (AbstractFilterStorage<?> filter : filterContainer.getPR()) {
			filter.processRecord(windowCoordinates.getGenomicWindowStart(), record);
		}
		
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
				windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
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
				windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
				break;

			case N:
				processSkipped(windowPosition, readPosition, genomicPosition, cigarElement, record);
				genomicPosition += cigarElement.getLength();
				windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
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

	@Override
	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
			filter.processAlignmentBlock(windowPosition, readPosition, genomicPosition, cigarElement, record);
		}

		for (int offset = 0; offset < cigarElement.getLength(); ++offset) {
			final int baseI = byte2int[record.getReadBases()[readPosition + offset]];

			int qual = record.getBaseQualities()[readPosition + offset];
			// all probs are reset
			qual = Math.min(Phred2Prob.MAX_Q - 1, qual);

			if (baseI >= 0 && qual >= sampleParameters.getMinBASQ()) {
				// speedup: if windowPosition == -1 the remaining part of the read will be outside of the windowCache
				// ignore the overhanging part of the read until it overlaps with the window cache
				windowPosition = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition + offset);

				if (windowPosition == -1) {
					// TODO parse some filters
					return;
				}
				if (windowPosition == -2) { // speedup jump to covered position
					offset += windowCoordinates.getGenomicWindowStart() - (genomicPosition + offset) - 1; // this should be negative 
				}
				if (windowPosition >= 0) {
					add2WindowCache(windowPosition, baseI, qual, strand);
					for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
						filter.processAlignmentMatch(windowPosition, readPosition + offset, genomicPosition + offset, record, baseI, qual);
					}
				}
			}
		}
	}

	@Override
	protected void processInsertion(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.I)) {
			filter.processInsertion(windowPosition, readPosition, genomicPosition, cigarElement, record);
		}
	}

	@Override
	protected void processDeletion(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.D)) {
			filter.processDeletion(windowPosition, readPosition, genomicPosition, cigarElement, record);
		}
	}

	@Override
	protected void processSkipped(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.N)) {
			filter.processSkipped(windowPosition, readPosition, genomicPosition, cigarElement, record);
		}
	}

}