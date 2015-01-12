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
	private String desc;

	private Set<CigarOperator> cigarOperators;

	public AbstractFilterFactory(final char c, final String desc, final Set<CigarOperator> cigarOperators) {
		this.c 				= c;
		this.desc 			= desc;

		this.cigarOperators = cigarOperators;
	}
	
	public AbstractFilterFactory(final char c, final String desc) {
		this(c, desc, new HashSet<CigarOperator>());
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

	public Set<CigarOperator> getCigarOperators() {
		return cigarOperators;
	}

}