import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Switch {
    public static void main(String[] args) throws Exception {
        HashMap<String, String> addressTable = new HashMap<>();
        ArrayList<String> nearestNeighbors = new ArrayList<String>();
        ArrayList<String> portList = new ArrayList<>();
        String destinationDevice;

        Scanner inputReader = new Scanner(System.in);
        System.out.println("What is the ID for Config Info?");
        String ID = inputReader.nextLine();
        Config switchConfig = Device.getConfig().get(ID);

        String switchNeighbors = Device.findNeighbors().get(ID);
        System.out.printf("Config: %s\nNeighbors: %s\n",switchConfig, switchNeighbors);

        //have a port open and listening
        int switchPort = Integer.parseInt(args[1]);
        DatagramSocket switchSocket = new DatagramSocket(switchPort);
        DatagramPacket request = new DatagramPacket(new byte[1024], 1024);

        //take in the array of neighbors and create new ports based on each

        // MAC Address; Port it's received from (ex: 10.1.2.3 3000)
// >> TEST: print out neighbors
        /**
         *  A switch has multiple virtual ports, and
         *  each virtual port should be “named” using the IP address
         *  and the port number of the neighbor switch or host.
         *  As an example, the left virtual port of S2
         *  should be named using S1’s IP address and S1’s port number.
         */

        // if dest device is recognizable in the addressTable array, send frame to that port

        // otherwise, add the address to the table and flood every port
        for (String address : addressTable) {
            if (address.equalsIgnoreCase(destinationDevice)) {
                forwardFrame();
            }
            else {
                // print the table once an address is added
                floodPorts(portList, sourcePort, switchSocket);
                addressTable.put(destinationDevice);
                printAddressTable(addressTable);
            }
        }
    }

    public static void forwardFrame(String destinationDevice, DatagramSocket switchSocket) {
        DatagramPacket reply = new DatagramPacket();
        switchSocket.send(reply);
    }

    public static void floodPorts(ArrayList<String> portList, String sourcePort, DatagramSocket switchSocket) {
        for (String port : portList) {
            if (!port.equalsIgnoreCase(sourcePort)) {
                DatagramPacket reply = new DatagramPacket();
                switchSocket.send(reply);
            }
        }
    }

    public static void printAddressTable(HashMap<String, String> addressTable) {
        System.out.println(addressTable);
    }
}
