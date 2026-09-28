public enum OpCode {

    RRQ(1),
    WRQ(2),
    DATA(3),
    ACK(4),
    ERR(5);

    private final byte code;

    OpCode(byte code) {
        this.code = code;
    }

    public byte getOpCode() {
        return this.code;
    }

    private OpCode toOpCode(byte codeByte) {

        return OpCode(codeByte);
    }

    public boolean check(byte n) {

        return this.code == n;
    }

    public static OpCode getOpCode(byte packetBytes) throws IOException {

        if (packetBytes == null || packetBytes.length < 2) {
            throw new IOException("Incorrect length of packet bytes.");
        } else if (packetBytes[0] != 0 || packetBytes[1] == 0) {
            throw new IOException("Incorrect format of OpCode in packet bytes.");
        } else {
            return packetBytes[1].;
        }
    }


    public static byte[] toBytes() {

        byte[] array = {0, this.code};
        return array;
    }

}