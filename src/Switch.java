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

        Map<String, String> nearestNeighbors = getNeighbors(neighbors);

        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }
        System.out.println(nearestPorts);

        int switchPort = Integer.parseInt(args[1]);
        DatagramSocket incomingSocket = new DatagramSocket(switchPort);
        DatagramPacket request = new DatagramPacket(new byte[1024], 1024);

        //have a port open and listening
        while (true) {
            incomingSocket.receive(request);
            InetAddress destinationIP;
            int destinationPort;
            byte[] content = Arrays.copyOf(request.getData(), request.getLength());
            String message = new String(content);

            // if dest device is recognizable in the addressTable array, send frame to that port
            for (String address : addressTable.keySet()) {
                if (address.equalsIgnoreCase(destinationDevice)) {
                    forwardFrame(destinationDevice, address, message);
                }
                // otherwise, add the address to the table and flood every port
                else {
                    floodPorts(nearestPorts, switchPort, message);
                    addressTable.put(destinationDevice);
                    // print the table once an address is added
                    printAddressTable(ID, addressTable);
                }
            }

        }
    }
    public static void forwardFrame(String destinationDevice, String address, String message) throws Exception {
        InetAddress destinationIP = InetAddress.getByName("");
        int destinationPort = 3000;
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
            if (!port.equalsIgnoreCase(String.valueOf(sourcePort))) {
                DatagramSocket outgoingSocket = new DatagramSocket(Integer.parseInt(port));
                DatagramPacket forward = new DatagramPacket(
                        message.getBytes(),
                        message.getBytes().length,
                        destinationIP,
                        port
                );
                outgoingSocket.send(forward);
            }
        }
    }
    public static void printAddressTable(String ID, HashMap<String, String> addressTable) {
        System.out.printf("--Table for %s--\n", ID);
        System.out.println(addressTable);
    }
    public static Map<String, String> getNeighbors (String neighbors) {
        Map<String, String> nearestNeighbors = new HashMap<>();
        String[] neighborConfigs = neighbors.split(";");
        for (String neighborConfig : neighborConfigs) {
            String[] neighbor = neighborConfig.split(":");
            if (neighbor.length == 2) {
                nearestNeighbors.put(neighbor[0].trim(), neighbor[1].trim());
            }
        }
        System.out.println(nearestNeighbors);
        return nearestNeighbors;
    }
}