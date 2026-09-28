public class Blocks {


	public static int count(byte[] fileBytes) {

		int payload = Tftp.BUFFER - Tftp.OFFSET;
		return (fileBytes.length + (payload - 1)) / payload;
	}

	public static byte[] toBytes(int blockNumber) {

		byte[] blockBytes = new byte[2];
		blockBytes[0] = (byte)(blockNumber >> 8);
		blockBytes[1] = (byte)(blockNumber);
		return blockBytes;
	}

	public static boolean check() {

	}

        
}