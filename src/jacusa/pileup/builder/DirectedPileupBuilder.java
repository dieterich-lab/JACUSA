package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Coordinate;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

/**
 * @author michael
 *
 */
public class DirectedPileupBuilder extends AbstractPileupBuilder {

	private WindowCache[] windowCaches;
	private FilterContainer[] filterContainers;
	private int[][] byte2intAr;
	
	public DirectedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, STRAND.UNKNOWN, reader, sample, parameters);

		windowCaches	= new WindowCache[2];
		windowCaches[0] = new WindowCache(windowCoordinates, baseConfig.getBaseLength());
		windowCaches[1] = windowCache;

		filterContainers = new FilterContainer[2];
		filterContainers[0] = parameters.getFilterConfig().createFilterContainer(windowCoordinates, sampleParameters);
		filterContainers[1] = filterContainer;
		
		final BaseConfig baseConfig = parameters.getBaseConfig();
		byte2intAr = new int[2][baseConfig.getByte2Int().length];
		byte2intAr[0] = baseConfig.getComplementByte2Int();
		byte2intAr[1] = byte2int;
	}

	@Override
	public void clearCache() {
		for (WindowCache windowCache : windowCaches) {
			windowCache.clear();
		}
		for (FilterContainer filterContainer : filterContainers) {
			filterContainer.clear();
		}
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		int i = strand.integer() - 1;
		return windowCaches[i].getCoverage(windowPosition);
	}

	@Override
	public FilterContainer getFilterContainer(int windowPosition, STRAND strand) {
		int i = strand.integer() - 1;
		return filterContainers[i];
	}
	
	@Override
	public Pileup getPileup(int windowPosition, STRAND strand) {
		final Pileup pileup = new DefaultPileup(
				windowCoordinates.getContig(), 
				windowCoordinates.getGenomicPosition(windowPosition), 
				strand, 
				baseConfig.getBaseLength());

		int i = strand.integer() - 1;
		WindowCache windowCache = windowCaches[i];

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(windowCache.getBaseCount(windowPosition));
		pileup.getCounts().setQualCount(windowCache.getQualCount(windowPosition));

		return pileup;
	}

	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, strand) >= sampleParameters.getMinCoverage();
	}

	@Override
	protected void add2WindowCache(int windowPosition, int baseI, int qual, STRAND strand) {
		int i = strand.integer() - 1;
		windowCaches[i].add(windowPosition, baseI, qual);
	}

	protected void processRecord(SAMRecord record) {
		if (record.getReadNegativeStrandFlag()) {
			strand = STRAND.REVERSE;
		} else {
			strand = STRAND.FORWARD;
		}
		int i = strand.integer() - 1;
		byte2int = byte2intAr[i];
		filterContainer = filterContainers[i];
		windowCache = windowCaches[i];
		
		super.processRecord(record);
	}

}