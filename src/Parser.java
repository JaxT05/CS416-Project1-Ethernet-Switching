import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Parser {
    static String configFile = "C:\\Users\\YMMSW\\IdeaProjects\\TestRun\\src\\config";
    static HashMap<String,Config> ConfigInfo = new HashMap<String,Config>();

    static HashMap<String, Config> getConfigInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String [] values = line.split(",");
                String ID = values[0];
                String PORT = values[1];
                String IP =  values[2];
                String PATH =  values[3];
                ConfigInfo.put(ID,new Config(PORT,IP, PATH));

            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ConfigInfo;
    }
}
