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

	public static boolean check(byte[] packetBytes, int blockNumber) throws IOException {

		if (packetBytes == null || packetBytes.length < 2) {
			throw new IOException("Block was not 2-bytes long.");
		} else {
			int upper = (packetBytes[1] & 0xFF) << 8;
			int lower = (packetBytes[1] & 0xFF);
			if ((upper | lower) == blockNumber) {
				return true;
			} else {
				return false;
			}
		}
	}	
}