import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Parser{
    static String ID = "";
    static HashMap<String, String> configInfo = new HashMap<>();
    static HashMap<String, String> findPartner = new HashMap<>();
    static String configFile = "C:\\Users\\YMMSW\\IdeaProjects\\CS416-Project1-Ethernet-Switching\\src\\config.txt";
    static void main() {}

    static String getConfigInfo() {
        try{BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (line.endsWith(",")) {
                    ID = parts[0];
                    String ports = parts[1];
                    String IP = parts[2];
                    configInfo.put(ID, ports);
                }
            }
            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return configInfo.get(ID);
    }

    static String getPath(){
        try{BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (line.endsWith(".")) {
                    ID = parts[0];
                    String neighbor = parts[1];
                    findPartner.put(ID, neighbor);
                }
            }
            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    return findPartner.get(ID);
    }
    }
