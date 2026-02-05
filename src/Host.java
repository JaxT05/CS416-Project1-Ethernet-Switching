import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Host {
    public static void main(String[] args) throws IOException {
        while (true) {
            //run the parser to figure out neighbors'
            Scanner scanner = new Scanner(System.in);
            System.out.print("ID: ");
            String ID = scanner.nextLine().trim();
            ArrayList<String> nearestPorts = new ArrayList<>();
            String neighbors = Parser.getConfigInfo().get(ID);
     //   System.out.println(neighbors);

            Map<String, String> nearestNeighbors = Parser.getNeighbors(neighbors);

//            for (String neighbor : nearestNeighbors.keySet()) {
//                String neighborConfig = nearestNeighbors.get(neighbor);
//                nearestPorts.add(neighborConfig);
//            }
//            System.out.println(nearestPorts);


            System.out.print("Message: ");
            String message = scanner.nextLine().trim();

            System.out.println("Destination: ");
            String destination = scanner.nextLine().trim();

            DatagramSocket client = new DatagramSocket();
            InetAddress addr = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            byte[] buf = message.getBytes();
            Packet packet = new Packet();
//            client.send(packet.udpPacket(buf, port, addr));
        }
    }
}
