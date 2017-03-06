package jacusa.filter.factory;

import java.util.HashSet;
import java.util.Set;


import net.sf.samtools.CigarOperator;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.util.WindowCoordinates;

public abstract class AbstractFilterFactory<T> {

	public final static char SEP = ':';

	private char c;
	protected String desc;

	private boolean filterByRecord;
	private Set<CigarOperator> cigarOperators;

	public AbstractFilterFactory(final char c, final String desc, final boolean filterByRecord, final Set<CigarOperator> cigarOperators) {
		this.c 				= c;
		this.desc 			= desc;

		this.filterByRecord = filterByRecord;
		this.cigarOperators = cigarOperators;
	}

	public AbstractFilterFactory(final char c, final String desc, final Set<CigarOperator> cigarOperators) {
		this(c, desc, false, cigarOperators);
	}
	
	public AbstractFilterFactory(final char c, final String desc) {
		this(c, desc, false, new HashSet<CigarOperator>());
	}

	public abstract AbstractFilterStorage<T> createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters);
	public abstract AbstractStorageFilter<T> createStorageFilter();

	public char getC() {
		return c;
	}

	public String getDesc() {
		return desc;
	}

	public void processCLI(final String line) throws IllegalArgumentException {
		// implement to change behavior via CLI
	}

	public boolean hasFilterByCigar() {
		return getCigarOperators().size() > 0;
	}
	
	public boolean hasFilterByRecord() {
		return filterByRecord;
	}
	
	public Set<CigarOperator> getCigarOperators() {
		return cigarOperators;
	}

}