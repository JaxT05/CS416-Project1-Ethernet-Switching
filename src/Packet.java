import java.io.IOException;
import java.net.*;

public class Packet{
    //datatype for packet that'll carry 4 parameters
    public void udpPacket(String message, int port, String ip) throws IOException {
        DatagramSocket client = new DatagramSocket();
        InetAddress addr = InetAddress.getByName(ip);
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, port);

        client.send(packet);
    }
}
