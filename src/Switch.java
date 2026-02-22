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
        String config = Parser.getConfigInfo().get(ID);

        Map<String, String> nearestNeighbors = Parser.getNeighbors(config);

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
            String sourceDeviceID;
            String destinationDeviceID;
            String neighborID;
            String addressTableID;

            if (frameContents.length == 6) {
                sourceDeviceID = frameContents[1];
                addressTableID = frameContents[3].split("\\.")[1];
                neighborID = frameContents[1];
                destinationDeviceID = frameContents[2];
                frameContents[0] = ID;
                frame = String.join(":", frameContents);
            }
            else {
                neighborID = frameContents[0];
                sourceDeviceID = frameContents[0];
                addressTableID = frameContents[2].split("\\.")[1];
                destinationDeviceID = frameContents[1];
                frame = ID +":"+ frame;
            }

            String sourceDeviceConfig = findNeighbor(neighborID, nearestNeighbors);
            String destinationDeviceConfig = findNeighbor(destinationDeviceID, addressTable);

            if (!addressTable.containsKey(addressTableID)) {
                addressTable.put(addressTableID, sourceDeviceConfig);
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
            InetAddress destinationIP = InetAddress.getByName(portArray[0]);
            int destinationPort = Integer.parseInt(portArray[1]);
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
        System.out.printf("----Table for %s----\n", ID);
        for (Map.Entry<String, String> address : addressTable.entrySet()) {
            System.out.println((address.getKey() + ", "  + address.getValue()));
        }
    }

    public static String findNeighbor(String sourceDeviceID, Map<String, String> nearestNeighbors) {
        String neighborInformation = "";
        if (nearestNeighbors.containsKey(sourceDeviceID)) {
            neighborInformation = nearestNeighbors.get(sourceDeviceID);
        }
        return neighborInformation;
    }
}