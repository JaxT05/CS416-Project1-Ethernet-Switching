import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Parser {
    static String configFile = "src/config";
    static HashMap<String, String> ConfigInfo = new HashMap<>();
    static HashMap<String, String> PathInfo = new HashMap<>();

    public static HashMap<String, String> getConfigInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                StringBuilder ConfigValue = new StringBuilder();
                StringBuilder PathValue = new StringBuilder();
                String bValues = line.split("_")[0];
                String [] values = bValues.split(",");
                String ID = values[0];
                String PORT = values[1];
                String IP =  values[2];
                String bPATH = line.split("_")[1];
                String [] EachLine = bPATH.split("]");
                String eachLineValue = "";
                for (int i = 0; i < EachLine.length; i++) {
                    eachLineValue = EachLine[i];
                }
                String pID = "";
                String[] pValue;
                String pPORT = "";
                String pIP = "";

                if( eachLineValue.toString().contains("-")) {
                    String[] withinEachLine = eachLineValue.split("-");
                    for  (int i = 0; i < withinEachLine.length; i++) {
                        pValue = withinEachLine[i].split("'");
                        pID = pValue[0];
                        pPORT = pValue[1];
                        pIP = pValue[2];
                        PathValue.append(pID).append(":");
                        PathValue.append(pIP).append(" ");
                        PathValue.append(pPORT).append(";");
                    }
                }else {
                    pValue = eachLineValue.split("'");
                    pID = pValue[0];
                    pIP = pValue[1];
                    pPORT = pValue[2];
                }

//                Since the device doesn't need to view its own IP, these are commented out

//                ConfigValue.append(ID).append(":");
//                ConfigValue.append(IP).append(" ");
//                ConfigValue.append(PORT).append(";");
                PathValue.append(pID).append(":");
                PathValue.append(pIP).append(" ");
                PathValue.append(pPORT).append(";");
                PathInfo.put(pID, PathValue.toString());
                String Carol = PathInfo.get(pID);
                ConfigValue.append(Carol);
                ConfigInfo.put(ID, ConfigValue.toString());
            }
            reader.close();
            return ConfigInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}