import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.UnsupportedEncodingException;

/**
 * This class describes a tftp.
 */
public class Tftp {

    public static final String ENCODING = "ASCII";
    public static final String EOF_STR = "\0";
    public static final byte EOF = 0;

    public static final int LIMIT = 131072;
    public static final int BUFFER = 516;
    public static final int OFFSET = 4;
    public static final int PAYLOAD = BUFFER - OFFSET;
    public static final int PORT = 69;
    public static final int TIMEOUT = 20000;
    public static final int PAUSE = 1000;
    public static final int ATTEMPTS = 5;


    /**
     * @brief   Sets the Path of a local host's directory.
     * @detail  Creates a Path object with the specified directory name, then
     *          returns the Path if it exists. Else, creates a new directory
     *          before returning the Path.
     *
     * @param   directoryName   The String name of the host's directory folder.
     *
     * @return  The Path object that was set for this local host.
     *
     * @throws  IOException
     */
    public static Path setLocalPath(String directoryName) throws IOException {

        Path localPath = Paths.get(directoryName);
        if (Files.exists(localPath)) {
            return localPath;
        } else {
            try {
                Files.createDirectories(localPath);
                return localPath;
            } catch (IOException e) {
                throw new IOException("Could not resolve a local directory.");
            }
        }
    }

    /**
     * @brief   Constructs a DatagramPacket format for a TFTP request.
     * @detail  Writes bytes over a stream to form a request packet, given the
     *          specified file name and the OpCode to specify either RRQ or WRQ.
     *          
     * @see     Tftp.OpCode
     * 
     * @param   fileName    The file name
     * @param   opCode      The code
     * @param   portNumber  The port
     * @return  The datagram packet.
     * @throws  IOException
     */
    public static DatagramPacket request(String fileName, OpCode opCode, int portNumber) throws IOException {

        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedOutputStream stream = new BufferedOutputStream(out)
        ) {
            stream.write(opCode.toBytes());
            stream.write(fileName.getBytes(ENCODING));
            stream.flush();
            byte[] requestBytes = out.toByteArray();
            return new DatagramPacket(requestBytes, requestBytes.length, portNumber);
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Encoding error: " + e.getMessage());
        }
    }

    /**
     * { function_description }
     *
     * @param      block        The block
     *
     * @return     The datagram packet.
     *
     * @throws     IOException  { exception_description }
     */
    public static DatagramPacket ackPacket(int blockNumber) throws IOException {

        try (
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(bytes)
        ) {
            out.write(OpCode.ACK.toBytes());
            out.write((byte)blockNumber);
            out.flush();
            byte[] array = bytes.toByteArray();
            return new DatagramPacket(array, array.length);
        }
    }

    /**
     * { function_description }
     *
     * @param      message      The message
     *
     * @return     The datagram packet.
     *
     * @throws     IOException  { exception_description }
     */
    public static DatagramPacket errorPacket(String message, int blockNumber) throws IOException {

        try (
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            BufferedOutputStream stream = new BufferedOutputStream(bytes)
        ) {
            stream.write(OpCode.ERR.toBytes());
            stream.write(Blocks.toBytes(blockNumber));
            stream.write(message.getBytes(ENCODING));
            stream.write(EOF);
            stream.flush();
            byte[] errorBytes = bytes.toByteArray();
            return new DatagramPacket(errorBytes, errorBytes.length);
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Encoding error: " e.getMessage());
        }
    }

    /**
     * Gets the file name.
     *
     * @param      requestBytes  The request bytes
     *
     * @return     The file name.
     *
     * @throws     IOException   { exception_description }
     */
    public static String getFileName(byte[] requestBytes) throws IOException {

        try {
            String line = new String(requestBytes, ENCODING);
            String[] parts = line.split(NULL_STRING);
            if (parts.length > 1 || parts[0].equals(RRQ_STRING)) {
                return parts[1].strip();
            } else {
                throw new IOException("RRQ packet was malformed.");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Encoding error: " + e.getMessage());
        }
    }

    /**
     * Gets the file path.
     *
     * @param      directory    The directory
     * @param      fileName     The file name
     *
     * @return     The file path.
     *
     * @throws     IOException  { exception_description }
     */
    public static Path getFilePath(Path directory, String fileName) throws IOException {

        Path filePath = directory.resolve(fileName);
        if (Files.exists(filePath)) {
            return filePath;
        } else {
            return null;
        }
    }

    /**
     * Gets the file bytes.
     *
     * @param      filePath     The file path
     *
     * @return     The file bytes.
     *
     * @throws     IOException  { exception_description }
     */
    public static byte[] getFileBytes(Path filePath) throws IOException {

        byte[] fileBytes = Files.readAllBytes(filePath);
        if (fileBytes.length < LIMIT) {
            return fileBytes;
        } else {
            return null;
        }
    }

    /**
     * Gets the packets.
     *
     * @param      fileBytes    The file bytes
     *
     * @return     The packets.
     *
     * @throws     IOException  { exception_description }
     */
    public static DatagramPacket[] getPackets(byte[] fileBytes) throws IOException {

        int totalBytes = fileBytes.length;
        DatagramPacket[] packets = new DatagramPacket[Blocks.count(fileBytes)];

        int blockNumber = 1;
        int sentBytes = 0;
        while (sentBytes < totalBytes) {

            int toSend = Math.min(totalBytes - sentBytes, PAYLOAD);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            stream.write(OpCode.DATA.toBytes());
            stream.write(Blocks.toBytes(blockNumber));
            stream.write(fileBytes, sentBytes, toSend);

            packets[blockNumber] = new DatagramPacket(stream.toByteArray(), sentBytes);

            sentBytes += toSend;
            blockNumber++;
        }
        return packets;
    }
}
