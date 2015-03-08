package jacusa.util;

import jacusa.pileup.DefaultPileup.STRAND;

public class Location {

	public String contig;
	public int genomicPosition;
	public STRAND strand;

	public Location(String contig, int genomicPosition, STRAND strand) {
		this.contig = contig;
		this.genomicPosition = genomicPosition;
		this.strand = strand;
	}

	public Location(Location location) {
		this(location.contig, location.genomicPosition, location.strand);
	}

	public void set(Location location) {
		contig = location.contig;
		genomicPosition = location.genomicPosition;
		strand = location.strand;
	}

	public String toString() {
		return contig + "_" + genomicPosition + "-" + (genomicPosition + 1);
	}

}