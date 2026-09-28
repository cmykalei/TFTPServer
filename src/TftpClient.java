import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;

public class TftpClient extends Thread {

    private static final String PATH = "client";
    private volatile boolean running = true;

    private InetAddress ip;
    private String fileName;
    private Path directory;

    private byte[] TID;

    public TftpClient(InetAddress ip, String fileName) throws IOException {

        this.ip = ip;
        this.fileName = fileName;
        this.directory = Tftp.setLocalPath(PATH);
    }

    public static void main(String[] args) {

        if (args.length == 2) {
            try {
            	System.out.println("\n\tStarting TftpClient...\n");
                TftpClient client = new TftpClient(InetAddress.getByName(args[0]), args[1]);
                client.start();
                client.join();
            } catch (IOException e) {
            	System.out.println("\tCould not initialise TftpClient's directory.");
            } catch (InterruptedException e) {
                System.out.println("\tClient process was interrupted.");
            }  finally {
				System.out.println("\tClosing TftpClient...");
        	}
        } else {
            System.out.println("\tIncorrect amount of arguments were entered.");
            System.out.println("\tUsage:\n\t$ java TftpClient <Server IP> <File Name>\n");
        }
    }

    @Override
    public void run() {

        try (
        	DatagramSocket socket = new DatagramSocket()
        ) {
            socket.setSoTimeout(Tftp.TIMEOUT);
            DatagramPacket rrq = Tftp.request(fileName,  Tftp.PORT);
            Path target = directory.resolve(fileName);

            System.out.println("\tSending request...");
            socket.send(rrq);

            System.out.println("\tWaiting for download...\n");
            downloadFile(socket, target);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private byte[] setTid(DatagramPacket packet) throws IOException {

        int port = packet.getPort();
        byte[] portBytes = new byte[OFFSET];
        byte[] addressBytes = packet.getAddress().getAddress();
           
        try (
            ByteArrayOutputStream stream = new ByteArrayOutputStream()
        ) {            
            portBytes[0] = (byte)(port >> 8);
            portBytes[1] = (byte)(port);

            stream.write(portBytes);
            stream.write(":".getBytes(ENCODING));
            stream.write(addressBytes);

            return stream.toByteArray();

        } catch (UnsupportedEncodingException e) {
            throw new IOException("Error while encoding the String to bytes.");
        }
    }

    private boolean checkTid(DatagramPacket packet, byte[] tid)  {

    	byte[] packet = packet.getData();
    	if (packet.length != tid.length) {
    		return false;
    	} else {
    		for (int i = 0; i < packet.length; i++) {
    			if (packet[i] != tid[i]) {
	    			return false;
	    		}
    		}
    		return true;
    	}
    }

    public void downloadFile(DatagramSocket socket, Path target) throws IOException {

        try (
        	FileOutputStream stream = new FileOutputStream(target.toFile());
            BufferedOutputStream download = new BufferedOutputStream(stream)
        ) {
            byte[] buffer = new byte[Tftp.BUFFER];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);
            this.TID = setTid(packet.getData());

            socket.send(Tftp.ackPacket());
            while (running) {

            	socket.receive(packet);

                byte[] ackBytes = packet.getData();
                OpCode type = OpCode.getByte(ackBytes);

                int portNumber = packet.getPort();
                int length = ackBytes.length - Tftp.OFFSET;
                
                try {
                	if (type.check(OpCode.DATA)) {
                		System.out.print("\rDownloading packet " + block);
                		download.write(bytes, Tftp.OFFSET, length);
                		block++;
                	} else if (type == Tftp.ERR) {
                		System.out.print("\rCould not download packet " + block);
                		block--;
                	} else if (type == Tftp.ACK) {
                		System.out.print("\rDownload complete at packet " + block);
                		running = false;
                	}
                } finally {
                	System.out.print("\rSending ACK for packet " + block + "\n");
                	socket.send(Tftp.ackPacket(block, ip, port));
                }
            }
        }
    }
}
