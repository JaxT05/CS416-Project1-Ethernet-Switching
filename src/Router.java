import java.net.*;
import java.util.*;

public class Router {
    public static void main(String[] args) throws Exception {
        Scanner inputReader = new Scanner(System.in);
        System.out.println("What is the ID of this router?");
        String ID = inputReader.nextLine();
        String config = Parser.getConfigInfo().get(ID);

        String[] configArray = config.split(">");
        String[] vIPs = Parser.getAllVIP(configArray[3]);
        Map<String, String> nearestNeighbors = Parser.getNeighbors(configArray[4]);
        //build forwarding table based off of neighbors
        Map<String, String> forwardingTable = new HashMap<>();
        StringBuilder routerVectors = new StringBuilder();
        Map<String, String> routingTable = new HashMap<>();

        //Initialize routing table with cost to neighbor
        for(Map.Entry <String, String> neighbor: nearestNeighbors.entrySet()){
            String neighborID = neighbor.getKey();
            int cost = 1;
            String routerVectorInfo = neighborID + "-" + cost;
            routerVectors.append(routerVectorInfo + ",");
        }
        //Put source router ID in the table
        routerVectors.append(ID + "-" + 0);
        routingTable.put(ID, routerVectors.toString());
        System.out.println("Routing Table");
        System.out.println(routingTable);
        // Forwarding table ("Destination subnet", "Next Hop ID")
        String allDevicesConfig = configArray[0];
        String[] deviceBlocks = allDevicesConfig.split(" ");

        /*
        Algorithm checks whether neighbor ID has the same port subnet.
        If true neighbor and source must share wire and will use that to reach destination
         */
        //initializes forwarding table
        for (String block : deviceBlocks) {
            String[] pieces = block.split(",");
            if (pieces.length < 4) continue;

            String neighborID = pieces[0];

            for (int i = 3; i < pieces.length; i++) {
                String neighborPortRaw = pieces[i];

                String neighborSubnet = neighborPortRaw.split("[:.]")[0];

                if (Arrays.toString(vIPs).contains(neighborSubnet)) {

                    forwardingTable.put(neighborSubnet, neighborID);
                }
            }
        }
        System.out.println("Initial Forwarding Table");
        System.out.println(forwardingTable);

        ArrayList<String> nearestPorts = new ArrayList<>();
        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }

        InetAddress realIP = InetAddress.getByName(configArray[1]);
        int routerPort = Integer.parseInt(configArray[2]);

        DatagramSocket incomingSocket = new DatagramSocket(routerPort);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);

        flooding(ID, routingTable, nearestNeighbors);
        //have a port open and listening
        while (true) {
            incomingSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();
            String[] frameContents = frame.split(":");
            System.out.print("Incoming Packet: ");
            printFrame(frameContents);
            String destinationDeviceID;

            if (Objects.equals(frameContents[0], ">")) {
                System.out.println("Routing packet received");
                distanceVectorRouting(ID, frameContents, forwardingTable, routingTable, nearestNeighbors);
            } else {
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
    /*

     */
    public static void distanceVectorRouting (String ID, String[] routingPacketContents, Map<String, String> forwardingTable, Map<String, String> routingTable, Map<String, String> nearestNeighbors){
        //Router flooding can only send to other routers
        //Check if the port to be flooded is a router
        //packet format
        //vector list format => Key: "Subnet" -> Value: "NextHopID,TotalCost"
//        String[] parts = routingPacket.split(":");

        String routingData = routingPacketContents[1];
        String[] routingDataArray = routingData.split(";");
        boolean changed = false;

        for(String routingDataPiece : routingDataArray) {
            String [] routingTableEntry = routingDataPiece.split("=");
            String tableEntryKey = routingTableEntry[0];
            String tableEntryValue = routingTableEntry[1];
            if (!routingTable.containsKey(tableEntryKey)) {
                routingTable.put(tableEntryKey, tableEntryValue);
                changed = true;
            } else {

            }
        }
        System.out.println(routingTable);
        if (changed) {
            flooding(ID, routingTable,nearestNeighbors);
        }

//        for(String route: routes){
//            String[] routeDetails = route.split(",");
//            String neighborSubnet = routeDetails[0];
//            int neighborCost = Integer.parseInt(routeDetails[1]);
//            //dummy data
//            int currentCost = 0;
//            if(neighborCost < currentCost){
//                //update Table
//                //send to neighbor routers
//                flooding(sourceID, routingTable, nearestNeighbors);
//            }
//        }
//        return null;
    }

    public static String routingUpdatePacket(String ID, Map<String, String> forwardingTable) {
        StringBuilder payload = new StringBuilder(">:");

        for (Map.Entry<String, String> entry : forwardingTable.entrySet()) {
            payload.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }

        return payload.toString();
    }

    public static void flooding(String sourceDeviceID, Map<String, String> routingTable, Map<String, String> nearestNeighbors){

        String frame = routingUpdatePacket(sourceDeviceID, routingTable);

        ArrayList<String> nearestPorts = new ArrayList<>();
        for (String neighbor : nearestNeighbors.keySet()) {
            String neighborConfig = nearestNeighbors.get(neighbor);
            nearestPorts.add(neighborConfig);
        }

        for (String portInfo: nearestPorts ) {
            try {
                String[] portArray = portInfo.split(" ");
                InetAddress destinationIP = InetAddress.getByName(portArray[0]);
                int destinationPort = Integer.parseInt(portArray[1]);

                DatagramSocket outgoingSocket = new DatagramSocket();
                DatagramPacket forward = new DatagramPacket(
                        frame.getBytes(),
                        frame.getBytes().length,
                        destinationIP,
                        destinationPort
                );
                outgoingSocket.send(forward);
                outgoingSocket.close();
            } catch (Exception e) {
                System.out.println("Error sending routing update: " + e.getMessage());
            }
        }
    }
}