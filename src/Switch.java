import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Switch {
    public static void main(String[] args) throws Exception {
        HashMap<String, String> addressTable = new HashMap<>();
        HashMap<String, String> nearestNeighbors = new HashMap<>();
        ArrayList<String> nearestPorts = new ArrayList<>();

        Scanner inputReader = new Scanner(System.in);
        System.out.println("What is the ID of this switch?");
        String ID = inputReader.nextLine();

        HashMap<String, String> switchNeighbors = Parser.getPath();
        System.out.printf("Neighbors: %s\n", switchNeighbors);

//        //take in the neighbors and create new ports based on each
//        for (String neighbor : nearestNeighbors.keySet()) {
//            String neighborConfig = nearestNeighbors.get(neighbor);
//            nearestPorts.add(neighborConfig);
//        }
//        // MAC Address; Port it's received from (ex: 10.1.2.3 3000)
//        /**
//         *  A switch has multiple virtual ports, and
//         *  each virtual port should be “named” using the IP address
//         *  and the port number of the neighbor switch or host.
//         *  As an example, the left virtual port of S2
//         *  should be named using S1’s IP address and S1’s port number.
//         */
//
//        //have a port open and listening
//        int switchPort = Integer.parseInt(args[1]);
//        DatagramSocket incomingSocket = new DatagramSocket(switchPort);
//        DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
//        String destinationDevice = "";
//        int destinationPort;
//        InetAddress destinationIP;
//
//
////// >> TEST: print out neighbors
//
//        // if dest device is recognizable in the addressTable array, send frame to that port
//        for (String address : addressTable.keySet()) {
//            if (address.equalsIgnoreCase(destinationDevice)) {
//                forwardFrame(destinationDevice, address, request);
//            }
//            // otherwise, add the address to the table and flood every port
//            else {
//                floodPorts(nearestPorts, switchPort, request);
//                addressTable.put(destinationDevice);
//                // print the table once an address is added
//                printAddressTable(ID, addressTable);
//            }
//        }
    }
//    public static void forwardFrame(String destinationDevice, String address, DatagramPacket request) throws Exception {
//        InetAddress destinationIP = InetAddress.getByName("");
//        int destinationPort = 3000;
//        DatagramSocket outgoingSocket = new DatagramSocket(destinationPort);
//        DatagramPacket forward = new DatagramPacket(
//                input.getBytes(),
//                input.getBytes().length,
//                destinationIP,
//                destinationPort
//        );
//        outgoingSocket.send(forward);
//    }
//    public static void floodPorts(ArrayList<String> portList, Integer sourcePort, DatagramPacket request) throws Exception {
//        for (String port : portList) {
//            if (!port.equalsIgnoreCase(String.valueOf(sourcePort))) {
//                DatagramSocket outgoingSocket = new DatagramSocket(Integer.parseInt(port));
//                DatagramPacket forward = new DatagramPacket();
//                outgoingSocket.send(forward);
//            }
//        }
//    }
//    public static void printAddressTable(String ID, HashMap<String, String> addressTable) {
//        System.out.printf("--Table for %s--\n", ID);
//        System.out.println(addressTable);
//    }
}