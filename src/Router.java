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
        String [] vIPs = Parser.getAllVIP(configArray[0]);
        Map <String, String> nearestNeighbors = Parser.getNeighbors(configArray[1]);
        System.out.println(Arrays.toString(vIPs));

        Map<String, String> forwardingTable = RouterTabling.returnForwardingTable(ID);
//        System.out.println(forwardingTable);

        ArrayList<String> nearestPorts = new ArrayList<>();
        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            System.out.println(neighborConfig);
            nearestPorts.add(neighborConfig);
        }
        System.out.println(nearestNeighbors);

        int routerPort = Integer.parseInt(args[1]);

        DatagramSocket incomingSocket = new DatagramSocket(routerPort);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);
        //have a port open and listening
        while (true) {
            incomingSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();
            printFrame(frame);
            String[] frameContents = frame.split(":");
            String destinationDeviceID;

            //from my understanding, the router should read the vIP subnet of the destination, look for it in the table,
            // and use the associated port to send it out

            //if the router is directly connected to the destination subnet, set the destination MAC to the actual destination
            // otherwise, set the destination MAC to

            String destinationIP = frameContents[5].split(".")[0];

            if (forwardingTable.containsKey(destinationIP)) {
                if (forwardingTable.get(destinationIP).split(".")[1] == destinationIP) {
                    destinationDeviceID = forwardingTable.get(destinationIP).split(".")[1];
                }
                else {
                    destinationDeviceID = frameContents[5].split(".")[1];
                }
                System.out.println(destinationDeviceID);
                String [] newFrameContents = swapAddress(ID, destinationDeviceID, frameContents);
                String destinationDeviceConfig = findNeighbor(destinationDeviceID, nearestNeighbors);
                frame = String.join(":", newFrameContents);
                printFrame(frame);
                forwardFrame(destinationDeviceConfig, frame);
            }
            else {
                System.out.println("Frame ignored.");
            }
        }
    }

    public static void printFrame (String frame) {
        System.out.println(frame);
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
