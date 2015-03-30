package jacusa.pileup;

import jacusa.pileup.DefaultPileup.STRAND;

public final class DefaultParallelPileup implements ParallelPileup {

	private String contig;
	private int start;
	private int end;
	private STRAND strand;
	
	private Pileup pileup1;
	private Pileup pileup2;
	private Pileup pileupP;
	
	private Pileup[] pileups1;
	private Pileup[] pileups2;
	private Pileup[] pileupsP;

	public DefaultParallelPileup() {
		contig = new String();
		start = -1;
		end = -1;
		strand = STRAND.UNKNOWN;
	}

	@Override
	public ParallelPileup copy() {
		return new DefaultParallelPileup(this);
	}

	public DefaultParallelPileup(final ParallelPileup parallelPileup) {
		this.contig 	= parallelPileup.getContig();
		this.start 		= parallelPileup.getStart();
		this.end 		= parallelPileup.getEnd();
		this.strand 	= parallelPileup.getStrand();

		this.pileup1 = new DefaultPileup(parallelPileup.getPooledPileup1());
		this.pileup2 = new DefaultPileup(parallelPileup.getPooledPileup2());
		this.pileupP = new DefaultPileup(parallelPileup.getPooledPileup());

		int n1 = parallelPileup.getPileups1().length;
		int n2 = parallelPileup.getPileups2().length;
		pileupsP = new Pileup[n1 + n2];

		pileups1 = new Pileup[n1];
		for (int i = 0; i < n1; ++i) {
			pileups1[i] = new DefaultPileup(parallelPileup.getPileups1()[i]);
			pileupsP[i] = pileups1[i];
		}
		pileups2 = new Pileup[n2];
		for (int i = 0; i < n2; ++i) {
			pileups2[i] = new DefaultPileup(parallelPileup.getPileups2()[i]);
			pileupsP[i + n1] = pileups2[i];
		}
	}

	public DefaultParallelPileup(final int n1, final int n2) {
		this();
		pileups1 = new Pileup[n1];
		pileups2 = new Pileup[n2];
		pileupsP = new Pileup[n1 + n2];
	}

	public DefaultParallelPileup(final Pileup[] pileups1, final Pileup[] pileups2) {
		pileupsP = new Pileup[pileups1.length + pileups2.length];
		
		this.pileups1 = new Pileup[pileups1.length];
		System.arraycopy(pileups1, 0, this.pileups1, 0, pileups1.length);
		this.pileups2 = new Pileup[pileups2.length];
		System.arraycopy(pileups2, 0, this.pileups2, 0, pileups2.length);

		System.arraycopy(pileups1, 0, pileupsP, 0, pileups1.length);
		System.arraycopy(pileups2,0, pileupsP, pileups1.length, pileups2.length);
		
		pileup1 = null;
		pileup2 = null;
		pileupP = null;
	}

	@Override
	public Pileup[] getPileups1() {
		return pileups1;
	}

	@Override
	public Pileup[] getPileups2() {
		return pileups2;
	}

	@Override
	public Pileup[] getPileupsP() {
		if (pileupsP == null) {
			pileupsP = new Pileup[getN1() + getN2()];

			if (getN1() > 0) {
				System.arraycopy(pileups1, 0, pileupsP, 0, getN1());
			}
			
			if (getN2() > 0) {
				System.arraycopy(pileups2, 0, pileupsP, getN1(), getN2());
			}
		}

		return pileupsP;
	}

	@Override
	public void setPileups1(final Pileup[] pileupsA) {
		this.pileups1 = pileupsA;
		
		pileup1 = null;
		pileupP = null;
		pileupsP = null;
	}

	@Override
	public void setPileups2(final Pileup[] pileupsB) {
		this.pileups2 = pileupsB;
		
		pileup2 = null;
		pileupP = null;
		pileupsP = null;
	}
	
	@Override
	public void reset() {
		pileup1 = null;
		pileup2 = null;
		pileupP = null;
	}

		@Override
	public int getN1() {
		return pileups1.length;
	}

	@Override
	public int getN2() {
		return pileups2.length;
	}

	@Override
	public int getN() {
		return getN1() + getN2();
	}
	
	@Override
	public String getContig() {
		return contig;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}
	
	@Override
	public STRAND getStrand() {
		return strand;
	}

	@Override
	public void setContig(String contig) {
		this.contig = contig;
	}
	
	@Override
	public void setStart(int start) {
		this.start = start;
	}

	@Override
	public void setEnd(int end) {
		this.end = end;
	}
	
	@Override
	public void setStrand(STRAND strand) {
		this.strand = strand;
	}

	@Override
	public STRAND getStrand1() {
		return getPooledPileup1().getStrand();
	}

	@Override
	public STRAND getStrand2() {
		return getPooledPileup2().getStrand();
	}

	@Override
	public boolean isValid() {
		return getN1() > 0 && getN2() > 0;
	}

	@Override
	public Pileup getPooledPileup1() {
		if(pileup1 == null && pileups1[0] != null) {
			pileup1 = new DefaultPileup(pileups1[0].getContig(), pileups1[0].getPosition(), pileups1[0].getStrand(), pileups1[0].getCounts().getBaseCount().length);
			for(int i = 0; i < pileups1.length; ++i) {
				pileup1.addPileup(pileups1[i]);
			}
		}
		return pileup1;
	}
	
	@Override
	public Pileup getPooledPileup2() {
		if(pileup2 == null && pileups2[0] != null) {
			pileup2 = new DefaultPileup(pileups2[0].getContig(), pileups2[0].getPosition(), pileups2[0].getStrand(), pileups2[0].getCounts().getBaseCount().length);
			for(int i = 0; i < pileups2.length; ++i) {
				pileup2.addPileup(pileups2[i]);
			}
		}
		return pileup2;
	}

	@Override
	public Pileup getPooledPileup() {
		if(pileupP == null && getPooledPileup1() != null) {
			pileupP = new DefaultPileup(getPooledPileup1().getCounts().getBaseCount().length);
			pileupP.setContig(getPooledPileup1().getContig());
			pileupP.setPosition(getPooledPileup1().getPosition());

			pileupP.addPileup(getPooledPileup1());
			pileupP.addPileup(getPooledPileup2());
		}

		return pileupP;
	}

	@Override
	public int[] getVariantBaseIs() {
		int n = 0;
		int[] alleles = getPooledPileup().getAlleles();
		for (int baseI : alleles) {
			int count1 = getPooledPileup1().getCounts().getBaseCount(baseI);
			int count2 = getPooledPileup2().getCounts().getBaseCount(baseI);

			if (count1 == 0 && count2 > 0 || count2 == 0 && count1 > 0) {
				++n;
			}
		}

		int[] variantBaseIs = new int[n];
		int j = 0;
		for (int baseI : alleles) {
			int count1 = getPooledPileup1().getCounts().getBaseCount(baseI);
			int count2 = getPooledPileup2().getCounts().getBaseCount(baseI);

			if (count1 == 0 && count2 > 0 || count2 == 0 && count1 > 0) {
				variantBaseIs[j] = baseI;
				++j;
			}
		}

		return variantBaseIs;
	}

	public static boolean isHoHo(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length == 1 && 
				parallelPileup.getPooledPileup1().getAlleles().length == 1 && parallelPileup.getPooledPileup2().getAlleles().length == 1;		
	}
	
	public static boolean isHeHe(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length == 2 && 
				parallelPileup.getPooledPileup1().getAlleles().length == 2 && parallelPileup.getPooledPileup2().getAlleles().length == 2;
	}

	public static boolean isHoHe(ParallelPileup parallelPileup) {
		return parallelPileup.getPooledPileup().getAlleles().length == 2 && 
				(parallelPileup.getPooledPileup1().getAlleles().length == 1 && parallelPileup.getPooledPileup2().getAlleles().length == 2 ||
						parallelPileup.getPooledPileup1().getAlleles().length == 2 && parallelPileup.getPooledPileup2().getAlleles().length == 1 );
	}
	
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();

		addPileup(sb, "1", getPooledPileup1());
		for (int pileupI = 0; pileupI < getPileups1().length; ++pileupI) {
			addPileup(sb, "1" + pileupI, getPileups1()[pileupI]);
		}

		addPileup(sb, "2", getPooledPileup2());
		for (int pileupI = 0; pileupI < getPileups2().length; ++pileupI) {
			addPileup(sb, "2" + pileupI, getPileups2()[pileupI]);
		}

		addPileup(sb, "P", getPooledPileup());
		for (int pileupI = 0; pileupI < getPileupsP().length; ++pileupI) {
			addPileup(sb, "P" + pileupI, getPileupsP()[pileupI]);
		}

		return sb.toString();
	}
	
	protected void addPileup(StringBuilder sb, String sample, Pileup pileup) {
		sb.append(sample);
		sb.append('\t');
		boolean flag = false;
		for (int count : pileup.getCounts().getBaseCount()) {
			if (flag) {
				sb.append('\t');
			}
			flag = true;
			sb.append(count);
		}
		sb.append('|');
		flag = false;
		for (int count : pileup.getCounts().getBaseCount()) {
			if (flag) {
				sb.append('\t');
			}
			flag = true;
			double d = (int)(100d * (double)count / (double)pileup.getCoverage());
			sb.append(d / 100d);
		}
		sb.append('\n');
	}

	public static ParallelPileup Pool(ParallelPileup pp){
		ParallelPileup ret = new DefaultParallelPileup(1, 1);
		Pileup[] pileup1 = {pp.getPooledPileup1()};
		ret.setPileups1(pileup1);
		Pileup[] pileup2 = {pp.getPooledPileup2()};
		ret.setPileups2(pileup2);
		
		return ret;
	}

	
	
}