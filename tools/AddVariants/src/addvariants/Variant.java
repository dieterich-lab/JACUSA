package addvariants;

public class Variant {

	private String contig;
	private int begin;
	private int end;

	private double ratio;
	private char base;
	
	public Variant() {
		contig = new String();
		begin = -1;
		end = -1;
		ratio = -1.0;

		base = 'N';
	}

	public Variant(String contig, int begin, int end, char base, double ratio) {
		this.contig = contig;
		this.begin = begin;
		this.end = end;
		this.ratio = ratio;
		this.base = base;
	}
	
	/**
	 * @return the contig
	 */
	public String getContig() {
		return contig;
	}

	/**
	 * @return the begin
	 */
	public int getStart() {
		return begin;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @return the ratio
	 */
	public double getRatio() {
		return ratio;
	}

	/**
	 * @return the base
	 */
	public char getBase() {
		return base;
	}
	
	public static Variant parseLine(String line) {
		String[] cols = line.split("\t");
		if(cols.length != 5) {
			System.err.println(line);
		}
		return new Variant(cols[0], Integer.parseInt(cols[1]), Integer.parseInt(cols[2]), cols[3].toUpperCase().charAt(0), Double.parseDouble(cols[4]));
	}

}
