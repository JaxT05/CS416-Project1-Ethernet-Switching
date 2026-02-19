import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Parser {
    static String configFile = "src/config";
    static HashMap<String, String> ConfigInfo = new HashMap<>();
    static Scanner scanner = new Scanner(System.in);
    static String ID;

    public static Map<String,String> returnNeighbors (String ID) {
        String neighborConfig = getConfigInfo().get(ID);
        return getNeighbors(neighborConfig);
    }

    public static HashMap<String, String> getConfigInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("Hosts")) {
                    ID = reader.readLine().trim();
                    String IP = reader.readLine().trim();
                    String PORT = reader.readLine().trim();
                    String GatIP = reader.readLine().trim();
                    String VirIP = reader.readLine().trim();
                    String NID = reader.readLine().trim();
                    StringBuilder Configure = new StringBuilder();
                    //Configure.append("Hosts: ").append(ID).append(" IP: ").append(IP).append(" PORT: ").append(PORT).append(" Gateway IP: ").append(GatIP).append(" Virtual IP: ").append(VirIP);
                    if (reader.readLine().trim().equals("Neighbors")) {
                        String[] NIDArray = NID.split(",");
                        String ANID = NIDArray[0];
                        String NPORT = NIDArray[1];
                        String NIP = NIDArray[2];
                        Configure.append(ANID).append(": ").append(NPORT).append("; ").append(NIP).append("; ");
                    }
                    ConfigInfo.put(ID, String.valueOf(Configure));
                } else if (line.equals("Routers")) {
                    ID = reader.readLine().trim();
                    String PORT = reader.readLine().trim();
                    String IP = reader.readLine().trim();
                    String GatIP = reader.readLine().trim();
                    String GatIP2 = reader.readLine().trim();
                    String VirIP = reader.readLine().trim();
                    String NID = reader.readLine().trim();
                    StringBuilder Configure = new StringBuilder();
                    //Configure.append("Hosts: ").append(ID).append(" IP: ").append(IP).append(" PORT: ").append(PORT).append(" Gateway IP: ").append(GatIP).append(" Gateway IP: ").append(GatIP2).append(" Virtual IP: ").append(VirIP);
                    if (reader.readLine().trim().equals("Neighbors")) {
                        String[] prepArray = NID.split(" ");
                        for (int i = 0; i < prepArray.length; i++) {
                            String[] NIDArray = prepArray[i].split(",");
                            String ANID = NIDArray[0];
                            String NPORT = NIDArray[1];
                            String NIP = NIDArray[2];
                            Configure.append(ANID).append(": ").append(NPORT).append("; ").append(NIP).append("; ");
                        }
                    }
                    ConfigInfo.put(ID, String.valueOf(Configure));
                } else if (line.equals("Switch")) {
                    ID = reader.readLine().trim();
                    String IP = reader.readLine().trim();
                    String PORT = reader.readLine().trim();
                    String NID = reader.readLine().trim();

                    StringBuilder Configure = new StringBuilder();
                    //Configure.append(",").append("Hosts: ").append(ID).append(" IP: ").append(IP).append(" PORT: ").append(PORT);

                    if (reader.readLine().trim().equals("Neighbors")) {
                        String[] prepArray = NID.split(" ");
                        for (int i = 0; i < prepArray.length; i++) {
                            String[] NIDArray = prepArray[i].split(",");
                            String ANID = NIDArray[0];
                            String NPORT = NIDArray[1];
                            String NIP = NIDArray[2];
                            Configure.append(ANID).append(": ").append(NPORT).append("; ").append(NIP).append("; ");
                        }
                        ConfigInfo.put(ID, String.valueOf(Configure));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ConfigInfo;
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
        return nearestNeighbors;
    }



}