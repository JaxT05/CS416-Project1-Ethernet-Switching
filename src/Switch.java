import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Switch {
    public static void main(String[] args) throws Exception {
        HashMap<String, String> addressTable = new HashMap<>();
        ArrayList<String> nearestPorts = new ArrayList<>();
        Scanner inputReader = new Scanner(System.in);
        System.out.println("What is the ID of this switch?");
        String ID = inputReader.nextLine();
        String neighbors = Parser.getConfigInfo().get(ID);
//        System.out.println(neighbors);

        Map<String, String> nearestNeighbors = Parser.getNeighbors(neighbors);

        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }

        int switchPort = Integer.parseInt(args[1]);
        DatagramSocket incomingSocket = new DatagramSocket(switchPort);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);
        //have a port open and listening
        while (true) {
            incomingSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();

            String[] frameContents = frame.split(":");
            System.out.println(Arrays.toString(frameContents));
            String sourceDeviceID = frameContents[0];
            String destinationDeviceID = frameContents[1];

            String sourceDeviceConfig = findNeighbor(sourceDeviceID, nearestNeighbors);
            String destinationDeviceConfig = findNeighbor(destinationDeviceID, addressTable);

            if (!addressTable.containsKey(sourceDeviceID)) {
                addressTable.put(sourceDeviceID, sourceDeviceConfig);
                printAddressTable(ID, addressTable);
            }
            if (addressTable.containsKey(destinationDeviceID)) {
                forwardFrame(destinationDeviceConfig, frame);
            } else {
                floodPorts(nearestPorts, sourceDeviceConfig, frame);
            }
        }
    }

    public static void forwardFrame(String destinationDeviceConfig, String frame) throws Exception {
        String[] destinationDeviceConfigArray = destinationDeviceConfig.split(" ");
        InetAddress destinationIP = InetAddress.getByName(destinationDeviceConfigArray[0]);
        int destinationPort = Integer.parseInt(destinationDeviceConfigArray[1]);
        System.out.println(destinationPort);
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

    public static void floodPorts(ArrayList<String> portList, String sourceDeviceConfig, String frame) throws Exception {
        String[] sourceDeviceConfigArray = sourceDeviceConfig.split(" ");
        int sourcePort = Integer.parseInt(sourceDeviceConfigArray[1]);
        for (String port : portList) {
            String[] portArray = port.split(" ");
//            System.out.println("port array:" + Arrays.toString(portArray));
            InetAddress destinationIP = InetAddress.getByName(portArray[0]);
            int destinationPort = Integer.parseInt(portArray[1]);
//            System.out.println(destinationPort);
//            System.out.println(sourcePort);
            if (destinationPort != sourcePort) {
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
        }
    }

    public static void printAddressTable(String ID, HashMap<String, String> addressTable) {
        System.out.printf("--Table for %s--\n", ID);
        System.out.println(addressTable);
    }

    public static String findNeighbor(String sourceDeviceID, Map<String, String> nearestNeighbors) {
        String neighborInformation = "";
        if (nearestNeighbors.containsKey(sourceDeviceID)) {
            neighborInformation = nearestNeighbors.get(sourceDeviceID);
        }
        return neighborInformation;
    }
}