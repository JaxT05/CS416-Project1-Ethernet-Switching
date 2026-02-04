import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Scanner;

public class Host {
    public static void main(String[] args) throws IOException {
        while (true) {
            //run the parser to figure out neighbors'
            Scanner scanner = new Scanner(System.in);
            System.out.print("ID: ");
            String id = scanner.nextLine().trim();
            Config hostConfig = Device.getConfig().get(id);

            String hostNeighbors = Device.findNeighbors().get(id);
            System.out.printf("Config: %s\nNeighbors: %s\n", hostConfig, hostNeighbors);

            System.out.print("Message: ");
            String message = scanner.nextLine().trim();

            System.out.println("Destination: ");
            String destination = scanner.nextLine().trim();

            DatagramSocket client = new DatagramSocket();
            InetAddress addr = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            byte[] buf = message.getBytes();
            Packet packet = new Packet();
            client.send(packet.udpPacket(buf, port, addr));
        }
    }
}
