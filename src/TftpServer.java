import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TftpServer extends Thread {

    private static final String PATH = "server";
    private InetAddress ip;
    private Path directory;

    public TftpServer(InetAddress ip) throws IOException {

        this.ip = ip;
        this.directory = Tftp.setLocalPath(PATH);
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("\n\tWelcome to TftpServer\n");

            try {
                System.out.println("\tResolving TftpServer's IP address...");
                InetAddress ip = InetAddress.getLocalHost();

                System.out.println("\tInitializing TftpServer's directory...");
                TftpServer server = new TftpServer(ip);

                System.out.println("\tStarting TftpServer's main thread...");
                System.out.println("\tUsage:\n\t$ java TftpClient " + ip.getHostAddress() + "\n");

                server.start();
                server.join();

            } catch (IOException e) {
                System.out.println("\tCould not initialise TftpServer's directory.");
            } catch (InterruptedException e) {
                System.out.println("\tCould not complete process without interruption.");
            } finally {
                System.out.println("\n\tTftpServer has closed...\n");
            }
        } else {
            System.out.println("\tToo many arguments were provided.");
            System.out.println("\tUsage:\n\t$ java TftpServer");
        }
    }

    @Override
    public void run() {

        try (
            DatagramSocket serverSocket = new DatagramSocket(Tftp.PORT)
        ) {
            serverSocket.setSoTimeout(Tftp.TIMEOUT);

            byte[] bytes = new byte[Tftp.BUFFER];
            DatagramPacket rrq = new DatagramPacket(bytes, bytes.length);
            serverSocket.receive(rrq);
            handleRequest(rrq);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleRequest(DatagramPacket rrq) throws IOException {

        try (
            DatagramSocket client = new DatagramSocket()
        ) {
            client.setSoTimeout(Tftp.TIMEOUT);
            InetAddress ip = rrq.getAddress();
            int port = rrq.getPort();
            String fileName = getFileName(rrq.getData());
            Path filePath = getFilePath(this.directory, fileName);

            if (filePath != null) {
                System.out.println("\tPath: " + filePath);
                byte[] fileBytes = getFileBytes(filePath);
                if (fileBytes != null) {
                    System.out.println("\tLength: " + fileBytes.length);
                    DatagramPacket[] packets = Tftp.getPackets(fileBytes, ip, port);
                    transfer(client, packets);
                } else {
                    client.send(Tftp.errorPacket("File Too Large", ip, port));
                    throw new IOException("File size exceeded " + Tftp.LIMIT);
                }
            } else {
                client.send(Tftp.errorPacket("File Not Found", ip, port));
                throw new IOException("Could not locate " + fileName);
            }
        }
    }

    private void transfer(DatagramSocket socket, DatagramPacket[] packets) throws IOException {
        
        byte[] ackBytes = new byte[Tftp.OFFSET];
        DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length);

        int totalBlocks = packets.length;  
        int attempts = 0;
        int currentBlock = 0;
        while (currentBlock < totalBlocks) { 

            if (attempts > Tftp.ATTEMPTS) {
                socket.send(Tftp.errorPacket())
                throw new IOException("Too many attempts to send packet " + currentBlock);
            } 

            socket.setSoTimeout(Tftp.PAUSE);        
            socket.receive(ackPacket);

            int nextBlock = currentBlock + 1;

            if (Blocks.check(ackPacket.getData(), nextBlock)) {
                System.out.print("\rSending next packet..." + nextBlock);
                socket.send(packets[nextBlock]);
                currentBlock++;
            } else {
                System.out.print("\rResending packet..." + currentBlock);
                socket.send(packets[currentBlock]);
                attempts++;
            }    
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

}
