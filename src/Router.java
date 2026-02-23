import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;


public class Router {
    public static void main(String[] args) throws Exception {
        Scanner inputReader = new Scanner(System.in);
        System.out.println("What is the ID of this router?");
        String ID = inputReader.nextLine();
        String config = Parser.getConfigInfo().get(ID);

        String[] configArray = config.split(">");
        String[] vIPs = Parser.getAllVIP(configArray[2]);
        Map<String, String> nearestNeighbors = Parser.getNeighbors(configArray[3]);
        Map<String, String> forwardingTable = RouterTabling.returnForwardingTable(ID);

        System.out.println(Arrays.toString(configArray));

        ArrayList<String> nearestPorts = new ArrayList<>();
        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }

        InetAddress realIP = InetAddress.getByName(configArray[0]);
        int routerPort = Integer.parseInt(configArray[1]);

        DatagramSocket incomingSocket = new DatagramSocket(routerPort);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);
        //have a port open and listening
        while (true) {
            incomingSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();
            String[] frameContents = frame.split(":");
            System.out.print("Incoming Packet: ");
            printFrame(frameContents);
            String destinationDeviceID;


            String[] destinationIP = frameContents[4].split("\\.");
            String destinationSubnet = destinationIP[0];
            String sourceSubnet = frameContents[3].split("\\.")[0];
            String destinationID = destinationIP[1];

            if (!destinationSubnet.equalsIgnoreCase(sourceSubnet)) {
                if (forwardingTable.containsKey(destinationSubnet)) {
                    if (!forwardingTable.get(destinationSubnet).split("\\.")[0].equalsIgnoreCase(destinationSubnet)) {
                        destinationDeviceID = forwardingTable.get(destinationSubnet).split("\\.")[1];
                    } else {
                        destinationDeviceID = destinationID;
                    }
                    String[] newFrameContents = swapAddress(ID, destinationDeviceID, frameContents);
                    System.out.print("Outgoing Packet: ");
                    printFrame(newFrameContents);
                    String destinationDeviceConfig = findNeighbor(forwardingTable.get(destinationSubnet).split("\\.")[1], nearestNeighbors);
                    frame = String.join(":", newFrameContents);
                    forwardFrame(destinationDeviceConfig, frame);
                    System.out.println();
                }
            } else {
                System.out.printf("Frame ignored.\n\n");
            }
        }
    }

    public static void printFrame (String[] frame) {
        for (int i = 1; i < frame.length; i ++) {
            System.out.printf("%s", frame[i]);
            if (frame[i] != frame[frame.length-1]) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }
    public static String[] swapAddress(String ID, String destinationID, String [] frameContents) {
        frameContents[1] = ID;
        frameContents[2] = destinationID;
        return frameContents;
    }
    public static void forwardFrame(String destinationDeviceConfig, String frame) throws Exception {
        String[] destinationDeviceConfigArray = destinationDeviceConfig.split(" ");
        InetAddress destinationIP = InetAddress.getByName(destinationDeviceConfigArray[0]);
        int destinationPort = Integer.parseInt(destinationDeviceConfigArray[1]);
        DatagramSocket outgoingSocket = new DatagramSocket();
        DatagramPacket forward = new DatagramPacket(
                frame.getBytes(),
                frame.getBytes().length,
                destinationIP,
                destinationPort
        );
        outgoingSocket.send(forward);
        outgoingSocket.close();
    }
    public static String findNeighbor(String sourceDeviceID, Map<String, String> nearestNeighbors) {
        String neighborInformation = "";
        if (nearestNeighbors.containsKey(sourceDeviceID)) {
            neighborInformation = nearestNeighbors.get(sourceDeviceID);
        }
        return neighborInformation;
    }
}
