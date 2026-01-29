import java.util.Map;
import java.util.Scanner;

public class Device {
    static Parser printMate = new Parser();
    static Scanner userInput = new Scanner(System.in);
    static void main() {
        System.out.println("What is the ID?");
        String newestID = userInput.nextLine();
        String testNeighbor = findNeighbors().get("S1");
        System.out.println("Your Neighbors are: " + testNeighbor);
        Config testConfig = getConfig().get(newestID);
        System.out.println("Your Config information is" + testConfig);
    }
    static public void DeviceClass (String ID, String ports, String IP){

    }
    public static Map<String, Config> getConfig(){
        return Parser.getConfigInfo();
    }

    public static Map<String, String> findNeighbors(){

        return Parser.getPath();
    }
}