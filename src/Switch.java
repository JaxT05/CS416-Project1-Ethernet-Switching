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
        System.out.println(nearestPorts);

        int switchPort = Integer.parseInt(args[1]);
        DatagramSocket incomingSocket = new DatagramSocket(switchPort);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);

        //have a port open and listening
        while (true) {
            incomingSocket.receive(incomingPacket);
            byte[] content = Arrays.copyOf(incomingPacket.getData(), incomingPacket.getLength());
            String message = new String(content);
            String[] frameContents = message.split(":");
            String sourceDeviceID = frameContents[0];
            String destinationDeviceID = frameContents[1];

            String sourceDeviceConfig = findNeighbor(sourceDeviceID, nearestNeighbors);
            String destinationDeviceConfig = findNeighbor(destinationDeviceID, addressTable);

            if (!addressTable.containsKey(sourceDeviceID)) {
                addressTable.put(sourceDeviceID, sourceDeviceConfig);
                printAddressTable(ID, addressTable);
            }
            if (addressTable.containsKey(destinationDeviceID)) {
                forwardFrame(destinationDeviceConfig, message);
            } else {
                floodPorts(nearestPorts, switchPort, message);
            }
        }
    }

    public static void forwardFrame(String destinationDeviceConfig, String message) throws Exception {
        String[] destinationDeviceConfigArray = destinationDeviceConfig.split(" ");
        InetAddress destinationIP = InetAddress.getByName(destinationDeviceConfigArray[0]);
        int destinationPort = Integer.parseInt(destinationDeviceConfigArray[1]);
        DatagramSocket outgoingSocket = new DatagramSocket(destinationPort);
        DatagramPacket forward = new DatagramPacket(
                message.getBytes(),
                message.getBytes().length,
                destinationIP,
                destinationPort
        );
        outgoingSocket.send(forward);
    }

    public static void floodPorts(ArrayList<String> portList, Integer sourcePort, String message) throws Exception {
        for (String port : portList) {
            String[] portArray = port.split(" ");
            InetAddress destinationIP = InetAddress.getByName(portArray[0]);
            int destinationPort = Integer.parseInt(portArray[1]);
            if (!port.equalsIgnoreCase(String.valueOf(sourcePort))) {
                DatagramSocket outgoingSocket = new DatagramSocket(Integer.parseInt(port));
                DatagramPacket forward = new DatagramPacket(
                        message.getBytes(),
                        message.getBytes().length,
                        destinationIP,
                        destinationPort
                );
                outgoingSocket.send(forward);
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