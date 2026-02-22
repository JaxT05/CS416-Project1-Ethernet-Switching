import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class Host {
    public static void main(String[] args) throws Exception {
        //run the parser to figure out neighbors'
        Scanner scanner = new Scanner(System.in);
        System.out.print("ID: ");
        String ID = scanner.nextLine().trim();

        String config = Parser.getConfigInfo().get(ID);
        String[] configArray = config.split(">");
        String vIP = Parser.getAllVIP(configArray[0])[0];
        String hostSubnet = vIP.split("\\.")[0];

        String gIP = Parser.getAllVIP(configArray[1])[0];
        Map<String, String> nearestNeighbors = Parser.getNeighbors(configArray[2]);


        int port = Integer.parseInt(args[1]);
        ArrayList<String> nearestPorts = new ArrayList<>();
        DatagramSocket hostSocket = new DatagramSocket(port);

        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }
//        System.out.println(nearestPorts);

        //creates a singular separate thread instead of establishing a pool of threads
        new Thread(() -> {
            try {
                receiveFrame(hostSocket, ID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        while (true) {
            System.out.print("Message: ");
            String message = scanner.nextLine().trim();
            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down.");
                break;
            }

            System.out.print("Destination IP: ");
            String destinationIP = scanner.nextLine().trim();
            String[] destinationIPArray = destinationIP.split("\\.");
            String destinationSubnet = destinationIPArray[0];
            String destinationMAC = "";

            if (destinationSubnet.equalsIgnoreCase(hostSubnet)) {
                destinationMAC = destinationIPArray[1];
            }
            else {
                destinationMAC = gIP.split("\\.")[1];
            }

            String nextDest = nearestPorts.getFirst();

            String frame = ID + ":" + destinationMAC + ":" + vIP + ":" + destinationIP  + ":" + message;
            sendFrame(hostSocket, nextDest, frame);
        }
        hostSocket.close();
    }

    public static void receiveFrame(DatagramSocket hostSocket, String ID) throws IOException {
        while (true) {
            DatagramPacket incomingPacket = new DatagramPacket(new byte [1024], 1024);
            hostSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();
            String[] frameContents = frame.split(":");
            String sourceDeviceID = frameContents[3].split("\\.")[1];
            String destinationDeviceID = frameContents[2];
            String message = frameContents[frameContents.length-1];
            if (destinationDeviceID.equalsIgnoreCase(ID)) {
                System.out.printf("%s, Source: %s\n", message, sourceDeviceID);
            }
            else {
                System.out.println("MAC Address Mismatch");
            }
        }
    }

    public static void sendFrame(DatagramSocket hostSocket, String nextDest, String frame) throws Exception {
        String[] nextDestArray = nextDest.split(" ");
        InetAddress nextDestIP = InetAddress.getByName(nextDestArray[0]);
        int nextDestPort = Integer.parseInt(nextDestArray[1]);
        DatagramPacket outgoingPacket = new DatagramPacket(
                frame.getBytes(),
                frame.getBytes().length,
                nextDestIP,
                nextDestPort
        );
        hostSocket.send(outgoingPacket);
    }
}
