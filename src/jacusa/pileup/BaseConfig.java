package jacusa.pileup;

import java.util.Arrays;

public class BaseConfig {

	// dictionary to convert byte to int -> index to ALL, or valid
	public static final int[] BYTE_BASE2INT_BASE = new int[86 + 1];
	public static final char[] BYTE_BASE2CHAR_BASE = new char[86 + 1];
	static {
		for (int i = 0; i < BYTE_BASE2INT_BASE.length; ++i) {
			BYTE_BASE2INT_BASE[i] = -1;
			BYTE_BASE2CHAR_BASE[i] = '*';
		}
		BYTE_BASE2INT_BASE[65] = 0; // 65 A
		BYTE_BASE2INT_BASE[67] = 1; // 67 C
		BYTE_BASE2INT_BASE[71] = 2; // 71 G
		BYTE_BASE2INT_BASE[86] = 3; // 86 T
		BYTE_BASE2INT_BASE[78] = 4; // 78 N
		
		BYTE_BASE2CHAR_BASE[65] = 'A'; // 65 A
		BYTE_BASE2CHAR_BASE[67] = 'C'; // 67 C
		BYTE_BASE2CHAR_BASE[71] = 'G'; // 71 G
		BYTE_BASE2CHAR_BASE[86] = 'T'; // 86 T
		BYTE_BASE2CHAR_BASE[78] = 'N'; // 78 N
	}

	public static final char STRAND_FORWARD_CHAR = '+';
	public static final char STRAND_REVERSE_CHAR = '-';
	public static final char STRAND_UNKNOWN_CHAR = '.';

	public static final char[] ALL = { 'A', 'C', 'G', 'T', 'N' };

	// this bases are the one that are used for computation
	public static final char[] VALID = { 'A', 'C', 'G', 'T' };
	public static final char[] VALID_COMPLEMENTED = { 'T', 'G', 'C', 'A' };

	private int[] VALIDbyte2int;

	private int[] basesI;
	private char[] bases;
	private int[] byte2int;
	// complement
	private int[] complementByte2int;

	public BaseConfig(char[] bases) {
		VALIDbyte2int = Byte2baseI(VALID);
		setBases(bases);
	}

	public int[] getByte2Int() {
		return byte2int;
	}

	public int[] getComplementByte2Int() {
		return complementByte2int;
	}

	// rearrange depending on given bases
	public int[] Byte2baseI(final char[] bases) {
		final int[] byte2int = new int[BYTE_BASE2INT_BASE.length];
		Arrays.fill(byte2int, -1);

		for (int i = 0; i < bases.length; ++i) {
			byte2int[(int)bases[i]] = i;
		}
		/* 
		 * info:
		byte2int[65] = 0; // 65 A
		byte2int[67] = 1; // 67 C
		byte2int[71] = 2; // 71 G
		byte2int[86] = 3; // 86 T
		byte2int[78] = 4; // 78 N
		*/
		return byte2int;
	}

	private char getComplementBase(char base) {
		int baseI = VALIDbyte2int[(byte)base];
		if (baseI < 0) {
			return 'N';
		}
		return VALID_COMPLEMENTED[baseI];
	}

	public int getComplementBaseI(byte base) {
		return complementByte2int[base];
	}
	
	// complement
	public int[] complementByte2baseI(final char[] bases) {
			final int[] byte2int = new int[BYTE_BASE2INT_BASE.length];
			Arrays.fill(byte2int, -1);

			for (int i = 0; i < bases.length; ++i) {
				char base = getComplementBase(bases[i]);
				byte2int[(int)base] = getBaseI((byte)bases[i]);
			}
			return byte2int;
		}


	public int getBaseI(byte base) {
		return byte2int[base];
	}

	public char[] getBases() {
		return bases;
	}

	public int getBaseLength() {
		return bases.length;
	}
	
	public void setBases(char[] bases) {
		this.bases = bases;
		byte2int = Byte2baseI(bases);
		complementByte2int = complementByte2baseI(bases);

		basesI = new int[bases.length];
		for (int i = 0; i < bases.length; ++i) {
			basesI[i] = i;
		}
	}

	public int[] getBasesI() {
		return basesI;
	}

	public int getK() {
		return bases.length;
	}

}