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

        /*
        Algorithm checks whether neighbor ID has the same port subnet.
        If true neighbor and source must share wire and will use that to reach destination
         */
        // Forwarding table ("Destination subnet", "Next Hop ID")
        String allDevicesConfig = configArray[0];
        String[] deviceBlocks = allDevicesConfig.split(" ");
        routerVectors.setLength(0);

        for (String block : deviceBlocks) {
            String[] pieces = block.split(",");
            if (pieces.length < 4) continue;

            String neighborID = pieces[0];

            for (int i = 3; i < pieces.length; i++) {
                String neighborSubnet = pieces[i].split("[:.]")[0];

                if (Arrays.toString(vIPs).contains(neighborSubnet)) {

                    String subnetInfo = neighborSubnet + "." +  neighborID;
                    forwardingTable.put(neighborSubnet, subnetInfo);

                    int cost = 1;
                    String routerVectorInfo = neighborID + "-" + cost + "-" + neighborSubnet + "-" + neighborID;
                    routerVectors.append(routerVectorInfo);
                    routerVectors.append(",");
                }
            }
        }
        routerVectors.deleteCharAt(routerVectors.length() - 1);
//        routerVectors.append(ID).append("-").append(0).append("-local-").append(ID);
        routingTable.put(ID, routerVectors.toString());
//        System.out.println("Routing Table");
//        System.out.println(routingTable);
//        System.out.println("Initial Forwarding Table");
//        System.out.println(forwardingTable);

        //open up listening port on this device
//        InetAddress realIP = InetAddress.getByName(configArray[1]);
        int routerPort = Integer.parseInt(configArray[2]);

        DatagramSocket incomingSocket = new DatagramSocket(routerPort);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);

        //send out initial routing table
        floodFrame(ID, routingTable, nearestNeighbors);

        while (true) {
            incomingSocket.receive(incomingPacket);
            String frame = new String(incomingPacket.getData(), 0, incomingPacket.getLength()).trim();
            String[] frameContents = frame.split(":");
            String destinationDeviceID;

            if (Objects.equals(frameContents[0], ">")) {
//                System.out.println("Routing packet received");
                distanceVectorRouting(ID, frameContents, forwardingTable, routingTable, nearestNeighbors);
//                System.out.println(forwardingTable);
            } else {
                System.out.print("Incoming Packet: ");
                printFrame(frameContents);
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
                    System.out.print("Frame ignored.\n\n");
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

    public static void distanceVectorRouting (String ID, String[] routingPacketContents, Map<String, String> forwardingTable, Map<String, String> routingTable, Map<String, String> nearestNeighbors){
        //vector list format => Key: "Subnet" -> Value: "NextHopID,TotalCost"
        String neighborData = routingPacketContents[1];
        String routingData = routingPacketContents[2];
        String[] routingDataArray = routingData.split(";");
        boolean changed = false;

        //Routing data piece [R3=R1-1-net5,R2-1-net6,R5-1-net8,S2-1-net2,R3-0-local]
        for(String routingDataPiece : routingDataArray) {
            String [] newRoutingTableEntry = routingDataPiece.split("=");
            String newTableEntryKey = newRoutingTableEntry[0];
            String newTableEntryValue = newRoutingTableEntry[1];

            String[] distanceVectorArray = newTableEntryValue.trim().split(",");

            if (!routingTable.containsKey(newTableEntryKey)) {
                routingTable.put(newTableEntryKey, newTableEntryValue);
                changed = true;
            }

            HashMap<String, String> newTableVectorInfo = new HashMap<>();
            HashMap<String, String> mainTableVectorInfo = new HashMap<>();

            String mainDistanceVectors = routingTable.get(ID);
//            System.out.println(mainDistanceVectors);
            String[] mainDistanceVectorArrayInfo = mainDistanceVectors.split(",");
            ArrayList<String> mainDistanceVectorArray = new ArrayList<>();
            for (String vectorArray : mainDistanceVectorArrayInfo) {
                mainDistanceVectorArray.add(vectorArray);
            }
            for (String mainVectorInfo : mainDistanceVectorArray) {
                String[] entryArray = mainVectorInfo.split("-");
                String vectorID = entryArray[0];
                int vectorCost = Integer.parseInt(entryArray[1]);
                String vectorSubnet = entryArray[2];
                String vectorNextHop = entryArray[3];
                mainTableVectorInfo.put(vectorID, vectorCost + " " + vectorSubnet + " " + vectorNextHop);
            }
            for (String newVectorInfo : distanceVectorArray) {
                String[] entryArray = newVectorInfo.split("-");
                String vectorID = entryArray[0];
                int vectorCost = Integer.parseInt(entryArray[1]);
                String vectorSubnet = entryArray[2];
                String vectorNextHop = entryArray[3];
                newTableVectorInfo.put(vectorID, vectorCost + " " + vectorSubnet + " " + vectorNextHop);
            }
            if (newTableVectorInfo.containsKey(ID)) {
                newTableVectorInfo.remove(ID);
            }
            for (String vector : newTableVectorInfo.keySet()) {

                if (mainTableVectorInfo.containsKey(vector)) {
//                    System.out.println(vector + ":" + mainTableVectorInfo.get(vector));
                    String newVectorInfo = newTableVectorInfo.get(vector);
                    String[] newVectorInfoArray = newVectorInfo.split(" ");
                    int newVectorCost = Integer.parseInt(newVectorInfoArray[0]) + 3;
                    String newVectorSubnet = newVectorInfoArray[1];

                    String mainVectorInfo = mainTableVectorInfo.get(vector);
                    String[] mainVectorInfoArray = mainVectorInfo.split(" ");
                    int mainVectorCost = Integer.parseInt(mainVectorInfoArray[0]);
                    String mainVectorSubnet = mainVectorInfoArray[1];

                    if (newVectorCost < mainVectorCost) {
                        String newVector = vector + "-" + newVectorCost + "-" + newVectorSubnet + "-" + neighborData;
//                        System.out.println("Running Comparison: " + newVector + " vs " + vector + "-" + mainVectorCost + "-" + mainVectorSubnet + "-" + mainVectorInfoArray[2]);
                        newTableVectorInfo.put(vector, newVector);
                        mainDistanceVectorArray.removeIf(mainDistanceVector -> mainDistanceVector.contains(vector));
//                        System.out.println(mainDistanceVectorArray);
                        mainDistanceVectorArray.add(newVector);
                        StringBuilder newMainDistanceVector = new StringBuilder();
                        for (String vectorArray : mainDistanceVectorArray) {
                            newMainDistanceVector.append(vectorArray).append(",");
                        }
                        newMainDistanceVector.deleteCharAt(newMainDistanceVector.length() - 1);
//                        System.out.println(newMainDistanceVector);
//                        routingTable.put(ID, newMainDistanceVector.toString());
//                        System.out.println(routingTable.get(ID));
                        changed = true;
                    }
                } else {
//                    System.out.println("Adding Vector: ");
                    String newVectorInfo = newTableVectorInfo.get(vector);
                    String[] newVectorInfoArray = newVectorInfo.split(" ");
                    int newVectorCost = Integer.parseInt(newVectorInfoArray[0]) + 1;
                    String newVectorSubnet = newVectorInfoArray[1];
                    String newVector = "," + vector + "-" + newVectorCost + "-" + newVectorSubnet + "-" + neighborData;
                    mainDistanceVectors = mainDistanceVectors.concat(newVector);
                    routingTable.put(ID, mainDistanceVectors);
//                    System.out.println(routingTable.get(ID));
                    changed = true;
                }
            }
        }
        if (changed) {
//            System.out.println("changed");
            //change forwarding table
            String tableConfig = routingTable.get(ID);
            HashMap<String, String> routingTableMap = new HashMap<>();
            String[] tableConfigArray = tableConfig.split(",");
            for (String tableConfigInfo : tableConfigArray) {
                String[] entryArray = tableConfigInfo.split("-");
//                System.out.println(Arrays.toString(entryArray));
                String vectorID = entryArray[0];
                int vectorCost = Integer.parseInt(entryArray[1]);
                String vectorSubnet = entryArray[2];
                String nextHop = entryArray[3];
                routingTableMap.put(vectorID, vectorCost + " " + vectorSubnet + " " + nextHop);
            }
            routingTableMap.remove(ID);
//            System.out.println(routingTableMap);
            for (String vector : routingTableMap.keySet()) {
                    String vectorInfo = routingTableMap.get(vector);
                    String[] vectorArray = vectorInfo.split(" ");
                    String subnet = vectorArray[1];
                    String nextHop = vectorArray[2];
                    String nextHopSubnet = "";
                    if (!forwardingTable.containsKey(ID)) {
                        for (String id : forwardingTable.keySet()) {
                            if (forwardingTable.get(id).contains(nextHop)) {
                                nextHopSubnet = id;
                            }
                        }
                        if (!Objects.equals(vector, nextHop)) {
                            forwardingTable.put(subnet, nextHopSubnet + "." + nextHop);
                        }
                    }
            }
//            System.out.println("Sending packet.");
            floodFrame(ID, routingTable,nearestNeighbors);
        }
    }

    public static String routingUpdatePacket(String ID, Map<String, String> forwardingTable) {
        StringBuilder payload = new StringBuilder(">:" + ID + ":");
        for (Map.Entry<String, String> entry : forwardingTable.entrySet()) {
            payload.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return payload.toString();
    }

    public static void floodFrame(String sourceDeviceID, Map<String, String> routingTable, Map<String, String> nearestNeighbors){
        String frame = routingUpdatePacket(sourceDeviceID, routingTable);
        //Check if the port to be flooded is a router
        ArrayList<String> nearestPorts = new ArrayList<>();
        for (String neighbor : nearestNeighbors.keySet()) {
            if (neighbor.contains("R")) {
                String neighborConfig = nearestNeighbors.get(neighbor);
                nearestPorts.add(neighborConfig);
            }
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