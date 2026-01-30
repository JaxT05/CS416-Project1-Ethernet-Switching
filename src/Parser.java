import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Parser {
    static String ID = "";
    static HashMap<String, Config> configInfo = new HashMap<>();
    static HashMap<String, String> findPartner = new HashMap<>();
    static String configFile = "C:\\Users\\YMMSW\\IdeaProjects\\CS416-Project1-Ethernet-Switching\\src\\config1.txt";

    static HashMap<String, Config> getConfigInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (line.endsWith(",")) {
                    ID = parts[0];
                    String ports = parts[1];
                    String IP = parts[2];
                    configInfo.put(ID, new Config(ports, IP));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configInfo;
    }

    static HashMap<String, String> getPath() {
      try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            StringBuilder newFile = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                newFile.append(line);
            }
            String newLine = newFile.toString();
            String[] lineArray = newLine.split(">");
            String lineOne = lineArray[1];
            String[] parts = lineOne.split("}", lineOne.length());
            String EachLine = "";
            String findNearby = "";
            for (int i = 1; i < parts.length -1; i++) {
                EachLine = parts[i];
                String[] pseudoID = EachLine.split("@");
                String ID = pseudoID[0];
                String [] NearBY =  pseudoID[1].split(";");
                findNearby =  Arrays.toString(NearBY);
                findPartner.put(ID, findNearby);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return findPartner;
    }
}