import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

/*
HOST CLASS MODIFICATIONS
---------------------------------------
i. Virtual source MAC address
ii. Virtual destination MAC address
iii. Virtual source IP address
iv. Virtual destination IP address
v. Short message

Example: A:R1:net1.A:net3.D:hello!
*/

public class Host {
    public static void main(String[] args) throws Exception {
        //run the parser to figure out neighbors'
        Scanner scanner = new Scanner(System.in);
        System.out.print("ID: ");
        String ID = scanner.nextLine().trim();
        String hostNeighbors = Parser.getConfigInfo().get(ID);
        String hostConfig = Parser.getConfigInfo().get(ID);
        String[] configArray = hostConfig.split(">");
        String gateIP = configArray[1];

        int port = Integer.parseInt(args[1]);
        ArrayList<String> nearestPorts = new ArrayList<>();
        Map<String, String> nearestNeighbors = Parser.getNeighbors(configArray[2]);
        System.out.println(nearestNeighbors);
        DatagramSocket hostSocket = new DatagramSocket(port);

        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }

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

            System.out.print("Destination: ");
            String destination = scanner.nextLine().trim();
            String nextDest = nearestPorts.getFirst();

            String virtualSourceAddr = getSourceAddr(ID);

            //if the destination is not in the subnet send it to the router
            if(!isSameNetwork(virtualSourceAddr, destination)){

            }

            String newFrame = ID+ ":" + gateIP + ":" + virtualSourceAddr + ":" +destination  + ":" + message;
            String frame = ID+ ":"+ destination  + ":" + message;
            System.out.println("Frame: " + newFrame);
            if(!hostNeighbors.contains(destination)){
                sendFrame(hostSocket, nextDest, newFrame);
            }
        }
        hostSocket.close();
    }

    public static void receiveFrame(DatagramSocket hostSocket, String ID) throws IOException {
        while (true) {
            DatagramPacket incomingPacket = new DatagramPacket(new byte [1024], 1024);
            hostSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();

            String[] frameContents = frame.split(":");
            String sourceDeviceID = frameContents[1];
            String destinationDeviceID = frameContents[2];
            String message = frameContents[3];
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
//        System.out.println(Arrays.toString(nextDestArray));
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

    public static boolean isSameNetwork(String s1, String s2){
        String net1 = s1.split("\\.")[0];
        String net2 = s2.split("\\.")[0];
        return net1.equals(net2);
    }

    public static String getSourceAddr(String id){
        String sourceAddress = "";

        if(id.contains("A") || id.contains("B")){
            sourceAddress = "net1." + id;
        }
        if (id.contains("C") || id.contains("D")){
            sourceAddress = "net3." + id;
        }
        return sourceAddress;
    }
}