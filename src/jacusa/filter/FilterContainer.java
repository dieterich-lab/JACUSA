package jacusa.filter;

import net.sf.samtools.SAMRecord;
import jacusa.filter.storage.AbstractFilterStorage;

public class FilterContainer {

	private int genomicWindowStart;
	private FilterConfig filterConfig;
	private AbstractFilterStorage<?>[] filters; 

	public FilterContainer(final FilterConfig filterConfig, final AbstractFilterStorage<?>[] filters) {
		genomicWindowStart = -1;
		this.filterConfig = filterConfig;
		this.filters = filters;
	}

	public void clear() {
		for (AbstractFilterStorage<?> filter : filters) {
			filter.clearContainer();
		}
	}

	public void setGenomicWindowStart(final int genomicWindowStart) {
		this.genomicWindowStart = genomicWindowStart;
	}

	public int getGenomicWindowStart() {
		return genomicWindowStart;
	}

	public void processRecord(final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filters) {
			filter.processRecord(genomicWindowStart, record);
		}
	}

	public AbstractFilterStorage<?> get(int i) {
		return filters[i];
	}
	
	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

}