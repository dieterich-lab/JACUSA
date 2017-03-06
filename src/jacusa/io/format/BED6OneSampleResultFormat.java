package jacusa.io.format;

import jacusa.filter.FilterConfig;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;

public class BED6OneSampleResultFormat extends BED6ResultFormat {

	private boolean showReferenceBase;
	
	public BED6OneSampleResultFormat(
			final BaseConfig baseConfig, 
			final FilterConfig filterConfig,
			final boolean showReferenceBase) {
		super('b', "One sample", baseConfig, filterConfig, showReferenceBase);
		this.showReferenceBase = showReferenceBase;
	}
	
	@Override
	public String getHeader(String[] pathnames1, String[] pathnames2) {
		final StringBuilder sb = new StringBuilder();

		sb.append(COMMENT);

		// position (0-based)
		sb.append("contig");
		sb.append(getSEP());
		sb.append("start");
		sb.append(getSEP());
		sb.append("end");
		sb.append(getSEP());

		sb.append("name");
		sb.append(getSEP());

		// stat	
		sb.append("stat");
		sb.append(getSEP());
		
		sb.append("strand");
		sb.append(getSEP());

		// (1) first sample infos / reference
		addSampleHeader(sb, '1', pathnames1.length);
		sb.append(getSEP());

		// (2) second sample infos / actual sample
		addSampleHeader(sb, '2', pathnames1.length);
		sb.append(getSEP());

		sb.append(getSEP());
		sb.append("info");

		/*s
		sb.append(getSEP());
		sb.append("ref");
		*/
		
		// add filtering info
		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append("filter_info");
		}

		if (showReferenceBase) {
			sb.append(getSEP());
			sb.append("refBase");
		}
		
		return sb.toString();
	}

	@Override
	public String convert2String(Result result) {
		final ParallelPileup parallelPileup = result.getParellelPileup();
		final double statistic = result.getStatistic();
		final StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getStart() - 1);
		sb.append(SEP);
		sb.append(parallelPileup.getEnd());
		
		sb.append(SEP);
		sb.append("variant");
		
		sb.append(SEP);
		if (Double.isNaN(statistic)) {
			sb.append("NA");
		} else {
			sb.append(statistic);
		}

		sb.append(SEP);
		sb.append(parallelPileup.getStrand().character());

		// (1) first pileups / actual sample
		addPileups(sb, parallelPileup.getPileups2());
		
		// (2) first pileups / actual sample
		addPileups(sb, parallelPileup.getPileups1());
		
		sb.append(getSEP());
		sb.append(result.getResultInfo().combine());
		
		/*
		sb.append(getSEP());
		sb.append(parallelPileup.getPooledPileup().getRefBase());
		*/
		
		// add filtering info
		if (filterConfig.hasFiters()) {
			sb.append(getSEP());
			sb.append(result.getFilterInfo().combine());
		}
		
		if (showReferenceBase) {
			sb.append(getSEP());
			sb.append(parallelPileup.getPooledPileup().getRefBase());
		}
		
		return sb.toString();		
	}

}
