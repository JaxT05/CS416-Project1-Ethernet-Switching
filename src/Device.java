import java.util.Map;
import java.util.Scanner;

public class Device {
    static Parser printMate = new Parser();
    static Scanner userInput = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("What is the ID for Config Info?");
        String newestIDConfig = userInput.nextLine();
        String testConfig = getConfig().get(newestIDConfig);
        System.out.println("Your Config information is " + testConfig);
    }
    static public void DeviceClass(String ID, String ports, String IP) {
    }

    public static Map<String, String> getConfig() {
        return Parser.getConfigInfo();

    }
}