import java.io.IOException;
import java.net.*;

public class Packet{
    //datatype for packet that'll carry 4 parameters
    public DatagramPacket udpPacket(byte[] buf, int port, InetAddress ip) {
        return new DatagramPacket(buf, buf.length, ip, port);
    }
}
